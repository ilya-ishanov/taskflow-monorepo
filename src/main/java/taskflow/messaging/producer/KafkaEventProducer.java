package taskflow.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import taskflow.dto.kafka.EventDto;
import taskflow.entity.Comment;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.entity.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTaskEvent(Task task, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title",  task.getTitle());
        payload.put("description", task.getDescription());
        payload.put("status", task.getStatus());
        sendEvent(eventType, task.getId().toString(), "TASK", payload);
    }

    public void sendProjectEvent(Project project, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name",  project.getName());
        payload.put("description", project.getDescription());
        payload.put("status", project.getStatus());
        sendEvent(eventType, project.getId().toString(), "PROJECT", payload);
    }

    public void sendUserEvent(User user, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("first name",  user.getFirstName());
        payload.put("last name", user.getLastName());
        payload.put("email", user.getEmail());
        sendEvent(eventType, user.getId().toString(), "USER", payload);
    }

    public void sendCommentEvent(Comment comment, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content",  comment.getContent());
        payload.put("taskId", comment.getTask().getId());
        payload.put("commentAuthorId", comment.getUser().getId());
        payload.put("taskAuthorId", comment.getTask().getAssignedUser().getId());
        sendEvent(eventType, comment.getId().toString(), "COMMENT", payload);
    }

    public void sendEvent(String eventType, String entityId, String entityType, Map<String, Object> payload) {
        EventDto eventDto = new EventDto();
        eventDto.setEventType(eventType);
        eventDto.setEntityId(entityId);
        eventDto.setEntityType(entityType);
        eventDto.setPayload(payload);
        eventDto.setCreatedAt(LocalDateTime.now());

        kafkaTemplate.send("taskflow.events", eventDto);
        log.info("Event отправлен в Kafka: {}", "taskflow.events");
    }
}
