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
import taskflow.dto.kafka.ProjectLogContext;
import taskflow.dto.kafka.TaskLogContext;
import taskflow.dto.request.ProjectRequestDto;
import taskflow.dto.response.ProjectResponseDto;
import taskflow.elasticsearch.service.ProjectSearchService;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.entity.User;
import taskflow.exceptions.EntityNotFoundException;
import taskflow.messaging.producer.KafkaEventProducer;
import taskflow.messaging.producer.KafkaLogProducer;
import taskflow.mongo.enums.TaskHistoryAction;
import taskflow.repository.ProjectRepository;
import taskflow.repository.UserRepository;
import taskflow.validator.Validator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final Validator validator;
    private final KafkaLogProducer kafkaLogProducer;
    private final KafkaEventProducer kafkaEventProducer;
    private final EventLogService eventLogService;
    private final ProjectSearchService projectSearchService;

    @Transactional
    public Project createProject(ProjectRequestDto requestDto) {
        log.info("Сохранение проекта: {}", requestDto);
        Project project = saveProject(requestDto);
        log.info("Проект сохранен: {}", project);
        logProjectAction(project, "Проект создан");
        eventProjectAction(project, "PROJECT_CREATED");
        eventLogService.projectEventLog(project, "PROJECT_CREATED");
        projectSearchService.projectIndex(project); //elastic
        return project;
    }

    @Cacheable(value = "projects", key = "#id")
    public Project findByIdProject(Long id) {
        log.info("Поиск проекта по id: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Проект", id));
        log.info("Найден проект: {}", project);
        return project;
    }

    @Transactional
    public Project updateProject(Long id, ProjectRequestDto requestDto) {
        log.info("Получение Dto от контроллера : {}", requestDto);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Проект", id));

        Long userId = requestDto.getOwnerId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("Пользователь", userId));

        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setStatus(requestDto.getStatus());
        project.setOwner(user);
        project.setUpdatedAt(LocalDateTime.now());

        List<User> users = userRepository.findAllById(requestDto.getUsers());
        project.setUsers(users);

        projectRepository.save(project);
        log.info("Update: {}", project);
        logProjectAction(project, "Проект обновлен");
        eventProjectAction(project, "PROJECT_UPDATED");
        eventLogService.projectEventLog(project, "PROJECT_UPDATED");
        projectSearchService.projectIndex(project); //elastic
        return project;
    }

    @CacheEvict(value = "projects", key = "#id")
    public void deleteProject(Long id) {
        log.info("Удаление проекта по id: {}", id);
        Project project = projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Проект", id));
        validator.validateEntityExists(id, projectRepository, "Проект");
        projectRepository.deleteById(id);
        logProjectAction(project, "Проект удален");
        eventProjectAction(project, "PROJECT_DELETED");
        eventLogService.projectEventLog(project, "PROJECT_DELETED");

        projectSearchService.deleteIndexProject(project); // ELASTIC
        log.error("Проект успешно удален: id={}", id);
    }

    public Page<ProjectResponseDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(ProjectResponseDto::from);
    }

    private Project saveProject(ProjectRequestDto requestDto) {
        Long userId = requestDto.getOwnerId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("Пользователь", userId));

        Project project = new Project();
        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setStatus(requestDto.getStatus());
        project.setOwner(user);

        List<User> users = userRepository.findAllById(requestDto.getUsers());
        project.setUsers(users);

        return projectRepository.save(project);
    }

    private void logProjectAction(Project project, String message) {
        ProjectLogContext projectLog = new ProjectLogContext();
        projectLog.setProjectId(project.getId());
        projectLog.setName(project.getName());
        projectLog.setStatus(project.getStatus().toString());

        kafkaLogProducer.sendLog("INFO", message, projectLog);
        log.info("Sending project log in kafka: {}", projectLog);
    }

    private void eventProjectAction(Project project, String eventType) {
        kafkaEventProducer.sendProjectEvent(project, eventType);
    }
}
