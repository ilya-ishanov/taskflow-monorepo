package eventconsumerservice.dto.responce;

import eventconsumerservice.entity.DeadLetterLog;
import eventconsumerservice.entity.EventLog;

import java.time.LocalDateTime;
import java.util.Map;

public record DeadLetterLogResponseDto(
        Long id,
        String eventType,
        Long entityId,
        String entityType,
        Map<String, Object> payload,
        Boolean processed,
        LocalDateTime createdAt
) {
    public static DeadLetterLogResponseDto from(DeadLetterLog deadLetterLog) {
        return new DeadLetterLogResponseDto(
                deadLetterLog.getId(),
                deadLetterLog.getEventType(),
                deadLetterLog.getEntityId(),
                deadLetterLog.getEntityType(),
                deadLetterLog.getPayload(),
                deadLetterLog.getProcessed(),
                deadLetterLog.getCreatedAt()
        );
    }
}
