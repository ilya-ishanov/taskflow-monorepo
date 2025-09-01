package taskflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taskflow.dto.kafka.EventDto;
import taskflow.dto.kafka.TaskLogContext;
import taskflow.dto.rabbit.NotificationRequestDto;
import taskflow.dto.request.TaskRequestDto;
import taskflow.dto.response.TaskResponseDto;
import taskflow.elasticsearch.service.TaskSearchService;
import taskflow.entity.EventLog;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.entity.User;
import taskflow.enums.Priority;
import taskflow.enums.Status;
import taskflow.exceptions.EntityNotFoundException;
import taskflow.messaging.producer.KafkaEventProducer;
import taskflow.messaging.producer.KafkaLogProducer;
import taskflow.messaging.producer.RabbitEventProducer;
import taskflow.messaging.producer.RedisNotificationProducer;
import taskflow.mongo.dto.request.TaskHistoryRequestDto;
import taskflow.mongo.enums.TaskHistoryAction;
import taskflow.repository.EventLogRepository;
import taskflow.repository.ProjectRepository;
import taskflow.repository.TaskRepository;
import taskflow.repository.UserRepository;
import taskflow.validator.Validator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final Validator taskValidator;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RabbitEventProducer rabbitEventProducer;
    private final KafkaLogProducer kafkaLogProducer;
    private final KafkaEventProducer kafkaEventProducer;
    private final EventLogService eventLogService;
    private final TaskSearchService taskSearchService;
    private final RedisNotificationProducer redisNotificationProducer;

    @Transactional
    public Task createTask(TaskRequestDto requestDto) {
        log.info("Создание задачи: {}", requestDto);
        Task task = saveTask(requestDto);
        User user = task.getAssignedUser();

        sendCreateTaskHistoryEvent(task, user, TaskHistoryAction.CREATE);
        notifyTaskCreated(task);
        logTaskAction(task, TaskHistoryAction.CREATE);
        eventTaskAction(task, "TASK_CREATED"); // 1 задача 11 таски
        eventLogService.taskEventLog(task, "TASK_CREATED");  // 2 задача 11 таски
        taskSearchService.indexTask(task); // elastic
        log.info("Задача сохранена: {}", task);
        return task;
    }

    @Cacheable(value = "tasks", key = "#id")
    public Task findById(Long id) {
        log.info("Поиск задачи по id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Таски", id));
        log.info("Найдена задача: {}", task);
        return task;
    }

    @Transactional
    @CachePut(value = "tasks", key = "#id")
    public Task update(Long id, TaskRequestDto requestDto) {
        log.info("Получение Dto от контроллера : {}", requestDto);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Таски", id));

        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setStatus(requestDto.getStatus());
        task.setPriority(requestDto.getPriority());
        task.setUpdatedAt(LocalDateTime.now());

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", requestDto.getUserId()));

        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Проект", requestDto.getProjectId()));

        task.setAssignedUser(user);
        task.setProject(project);

        taskRepository.save(task);
        notifyTaskUpdated(task);
        sendCreateTaskHistoryEvent(task, user, TaskHistoryAction.UPDATE);
        logTaskAction(task, TaskHistoryAction.UPDATE);
        eventTaskAction(task, "TASK_UPDATED");
        eventLogService.taskEventLog(task, "TASK_UPDATED");  // 2 задача 11 таски
        taskSearchService.indexTask(task); // elastic
        redisNotificationProducer.publish("Уведомление отправлено: задача обновлена, ID=" + task.getId()); // redis
        log.info("Update: {}", task);
        return task;
    }

    @CacheEvict(value = "tasks", key = "#id")
    public void delete(Long id) {
        log.info("Удаление задачи по id: {}", id);
        taskValidator.validateEntityExists(id, taskRepository, "Таски");

        Task task = taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Таски", id));
        User user  = task.getProject().getUsers().getFirst();
        sendCreateTaskHistoryEvent(task, user, TaskHistoryAction.DELETE);
        notifyTaskDeleted(task);
        logTaskAction(task, TaskHistoryAction.DELETE);
        eventTaskAction(task, "TASK_DELETED");
        eventLogService.taskEventLog(task, "TASK_DELETED");  // 2 задача 11 таски
        taskRepository.deleteById(id);

        taskSearchService.deleteTaskIndex(task); // ELASTIC
        redisNotificationProducer.publish("Уведомление отправлено: задача удалена, ID=" + task.getId()); // redis
        log.error("Задача успешно удалена: id={}", id);
    }

    public Page<TaskResponseDto> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(TaskResponseDto::from);
    }

    private Task saveTask(TaskRequestDto requestDto) {
        Task task = new Task();
        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setPriority(requestDto.getPriority());
        task.setDeadline(requestDto.getDeadline());
        task.setStatus(requestDto.getStatus());

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", requestDto.getUserId()));
        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Проект", requestDto.getProjectId()));

        task.setAssignedUser(user);
        task.setProject(project);
        task = taskRepository.save(task);
        return task;
    }

    private void sendCreateTaskHistoryEvent(Task task, User user, TaskHistoryAction action) {
        TaskHistoryRequestDto historyDto = new TaskHistoryRequestDto();
        historyDto.setTaskId(task.getId());
        historyDto.setAction(action);
        historyDto.setPerformedBy(user.getId());
        historyDto.setTimestamp(LocalDateTime.now());
        historyDto.setDetails(Map.of(
                "title", task.getTitle(),
                "priority", task.getPriority().toString(),
                "description", task.getDescription(),
                "deadline", task.getDeadline().toString(),
                "status", task.getStatus().toString()
        ));
        log.info("История действия: {} для taskId={}, userId={}", action, task.getId(), user.getId());
        rabbitEventProducer.sendTaskHistoryEvent(historyDto);
    }

    private void notifyTaskCreated(Task task) {
        NotificationRequestDto notificationDto = new NotificationRequestDto();
        notificationDto.setTaskId(task.getId());
        notificationDto.setTitle(task.getTitle());
        notificationDto.setStatus(TaskHistoryAction.CREATE.toString());
        notificationDto.setTimestamp(LocalDateTime.now());
        log.info("Sending notification: {}", notificationDto);
        rabbitEventProducer.sendTaskCreatedNotification(notificationDto);
    }

    private void logTaskAction(Task task, TaskHistoryAction action) {
        TaskLogContext taskLog = new TaskLogContext();
        taskLog.setTaskId(task.getId());
        taskLog.setTitle(task.getTitle());
        taskLog.setStatus(task.getStatus().toString());

        String message = switch (action) {
            case CREATE -> "Задача создана";
            case UPDATE -> "Задача обновлена";
            case DELETE -> "Задача удалена";
        };
        kafkaLogProducer.sendLog("INFO", message, taskLog);
        log.info("Sending log in kafka: {}", taskLog);
    }

    private void eventTaskAction(Task task, String eventType) {
        kafkaEventProducer.sendTaskEvent(task, eventType);
    }

    private void notifyTaskUpdated(Task task) {
        NotificationRequestDto notificationDto = new NotificationRequestDto();
        notificationDto.setTaskId(task.getId());
        notificationDto.setTitle(task.getTitle());
        notificationDto.setStatus(TaskHistoryAction.UPDATE.toString());
        notificationDto.setTimestamp(LocalDateTime.now());
        log.info("Sending notification: {}", notificationDto);
        rabbitEventProducer.sendTaskUpdateNotification(notificationDto);
    }

    private void notifyTaskDeleted(Task task) {
        NotificationRequestDto notificationDto = new NotificationRequestDto();
        notificationDto.setTaskId(task.getId());
        notificationDto.setTitle(task.getTitle());
        notificationDto.setStatus(TaskHistoryAction.DELETE.toString());
        notificationDto.setTimestamp(LocalDateTime.now());
        log.info("Sending notification: {}", notificationDto);
        rabbitEventProducer.sendTaskDeleteNotification(notificationDto);
    }

    public List<Task> filterByStatusAndPriority(Status status, Priority priority) {
        return taskRepository.findByStatusAndPriority(status, priority);
    }
}