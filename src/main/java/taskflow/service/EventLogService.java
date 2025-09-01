package taskflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taskflow.dto.kafka.EventResponseDto;
import taskflow.entity.Comment;
import taskflow.entity.EventLog;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.repository.EventLogRepository;
import taskflow.exceptions.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventLogService {
    private final EventLogRepository eventLogRepository;

    public EventLog gentEventLog(Long id) {
        return eventLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EventLog", id));
    }

    public List<EventLog> getAllEventLogs() {
        return eventLogRepository.findAll();
    }

    public void taskEventLog(Task task, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title",  task.getTitle());
        payload.put("description", task.getDescription());
        payload.put("status", task.getStatus());
        eventLogAction(eventType, task.getId(), "TASK", payload);
    }

    public void projectEventLog(Project project, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name",  project.getName());
        payload.put("description", project.getDescription());
        payload.put("status", project.getStatus());
        eventLogAction(eventType, project.getId(), "PROJECT", payload);
    }

    public void commentEventLog(Comment comment, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content",  comment.getContent());
        payload.put("taskId", comment.getTask().getId());
        payload.put("userId", comment.getUser().getId());
        eventLogAction(eventType, comment.getId(), "COMMENT", payload);
    }

    private void eventLogAction(String eventType, Long entityId, String entityType, Map<String, Object> payload) {
        EventLog eventLog = new EventLog();
        eventLog.setEventType(eventType);
        eventLog.setEntityId(entityId);
        eventLog.setEntityType(entityType);
        eventLog.setPayload(payload);
        eventLog.setCreatedAt(LocalDateTime.now());
        eventLogRepository.save(eventLog);
    }
}
