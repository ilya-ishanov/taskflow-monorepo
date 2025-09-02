package eventconsumerservice.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eventconsumerservice.dto.request.DeadLetterLogDto;
import eventconsumerservice.dto.request.EventDto;
import eventconsumerservice.dto.request.NotificationRequestDto;
import eventconsumerservice.entity.DeadLetterLog;
import eventconsumerservice.entity.EventLog;
import eventconsumerservice.entity.Project;
import eventconsumerservice.messaging.producer.KafkaDlqProducer;
import eventconsumerservice.messaging.producer.KafkaNotificationProducer;
import eventconsumerservice.repository.DeadLetterLogRepository;
import eventconsumerservice.repository.LogEventRepository;
import eventconsumerservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final LogEventRepository logEventRepository;
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final KafkaNotificationProducer kafkaNotificationProducer;
    private final KafkaDlqProducer kafkaDlqProducer;
    private final DeadLetterLogRepository deadLetterLogRepository;

    @KafkaListener(topics = "taskflow.events")
    public void consumeMessage(String json, Acknowledgment ack) throws JsonProcessingException {
        EventDto eventDto = objectMapper.readValue(json, EventDto.class);
        String entityType = eventDto.getEntityType();
        EventLog eventLog = new EventLog();
        boolean handled = false;

        try {
            if (entityType.equals("TASK")) {
                handleTask(eventLog, eventDto);
                handled = true;
            }
        } catch (Exception ex) {
            sendToDeadLetterQueue(eventDto, ex, ack);
            return;
        }

        try {
            if (entityType.equals("PROJECT")) {
                saveOrUpdateProject(eventDto);
                handled = true;
            }
        } catch (Exception ex) {
            sendToDeadLetterQueue(eventDto, ex, ack);
            return;
        }

        try {
            if (entityType.equals("COMMENT")) {
                handleComment(eventDto, ack);
                handled = true;
            }
        } catch (Exception ex) {
            sendToDeadLetterQueue(eventDto, ex, ack);
            return;
        }

        if (handled) {
            ack.acknowledge();
        } else {
            log.warn("Неизвестный entityType: {}", entityType);
            ack.acknowledge();
        }
    }

    private void handleTask(EventLog eventLog, EventDto eventDto) {
        eventLog.setEventType(eventDto.getEventType());
        eventLog.setEntityId(eventDto.getEntityId());
        eventLog.setEntityType(eventDto.getEntityType());
        eventLog.setPayload(eventDto.getPayload());
        eventLog.setCreatedAt(eventDto.getCreatedAt());
        eventLog.setProcessed(false);
        logEventRepository.save(eventLog);
        log.info("Event TASK сохранён в DB");
    }

    private void handleComment(EventDto eventDto, Acknowledgment ack) {
        Map<String, Object> payload = eventDto.getPayload();
        String content = payload.get("content").toString();
        Long taskId = Long.parseLong(payload.get("taskId").toString());
        Long commentAuthorId = Long.parseLong(payload.get("commentAuthorId").toString());
        Long taskAuthorId = Long.parseLong(payload.get("taskAuthorId").toString());

        NotificationRequestDto requestDto = new NotificationRequestDto();
        requestDto.setReceiverId(taskAuthorId);
        requestDto.setTaskId(taskId);
        requestDto.setTitle("Новый комментарий");
        requestDto.setMessage("К вашей задаче добавлен комментарий: " + content);
        requestDto.setTimestamp(LocalDateTime.now());

        kafkaNotificationProducer.sendNotification(requestDto);
        ack.acknowledge();
    }

    private void saveOrUpdateProject(EventDto eventDto) {
        Map<String, Object> payload = eventDto.getPayload();
        Optional<Project> maybeProject = projectRepository.findById(eventDto.getEntityId());
        Project project;

        if (maybeProject.isPresent()) {
            project = maybeProject.get();
            log.info("Обновляем существующий проект с ID={}", project.getId());
        } else {
            project = new Project();
            project.setId(eventDto.getEntityId());
            project.setCreated_at(LocalDateTime.now());
            project.setArchived(false);
            log.info("Создаём новый проект с ID={}", project.getId());
        }

        project.setName(payload.get("name").toString());
        project.setDescription(payload.get("description").toString());
        project.setStatus(payload.get("status").toString());
        projectRepository.save(project);
    }

    private void sendToDeadLetterQueue(EventDto eventDto, Exception ex, Acknowledgment ack) {
        log.error("Ошибка обработки события, отправляем в DLQ", ex);

        DeadLetterLogDto dlqDto = new DeadLetterLogDto();
        dlqDto.setEventType(eventDto.getEventType());
        dlqDto.setEntityId(eventDto.getEntityId());
        dlqDto.setEntityType(eventDto.getEntityType());
        dlqDto.setCreatedAt(LocalDateTime.now());
        dlqDto.setPayload(eventDto.getPayload());
        dlqDto.setReason(ex.getMessage());
        kafkaDlqProducer.sendDeadLetterLog(dlqDto);

        DeadLetterLog dlqEntity = new DeadLetterLog();
        dlqEntity.setEventType(eventDto.getEventType());
        dlqEntity.setEntityId(eventDto.getEntityId());
        dlqEntity.setEntityType(eventDto.getEntityType());
        dlqEntity.setCreatedAt(LocalDateTime.now());
        dlqEntity.setPayload(eventDto.getPayload());
        dlqEntity.setReason(ex.getMessage());
        deadLetterLogRepository.save(dlqEntity);
        ack.acknowledge();
        log.warn("Сообщение {} перенаправлено в DLQ", eventDto);
    }
}
