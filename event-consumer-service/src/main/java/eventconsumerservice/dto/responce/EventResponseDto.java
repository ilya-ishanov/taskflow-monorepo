package eventconsumerservice.dto.responce;

import eventconsumerservice.entity.EventLog;

import java.time.LocalDateTime;
import java.util.Map;

public record EventResponseDto(
        Long id,
        String eventType,
        Long entityId,
        String entityType,
        Map<String, Object> payload,
        Boolean processed,
        LocalDateTime createdAt
) {
    public static EventResponseDto from(EventLog eventLog) {
        return new EventResponseDto(
                eventLog.getId(),
                eventLog.getEventType(),
                eventLog.getEntityId(),
                eventLog.getEntityType(),
                eventLog.getPayload(),
                eventLog.getProcessed(),
                eventLog.getCreatedAt()
        );
    }
}
