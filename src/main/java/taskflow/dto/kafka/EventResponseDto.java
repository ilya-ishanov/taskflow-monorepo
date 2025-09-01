package taskflow.dto.kafka;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import taskflow.confings.MapToJsonConverter;
import taskflow.entity.EventLog;

import java.time.LocalDateTime;
import java.util.Map;

public record EventResponseDto(
        Long id,
        String eventType,
        Long entityId,
        String entityType,
        Map<String, Object> payload,
        LocalDateTime createdAt
) {
    public static EventResponseDto from(EventLog eventLog) {
        return new EventResponseDto(
                eventLog.getId(),
                eventLog.getEventType(),
                eventLog.getEntityId(),
                eventLog.getEntityType(),
                eventLog.getPayload(),
                eventLog.getCreatedAt()
        );
    }
}
