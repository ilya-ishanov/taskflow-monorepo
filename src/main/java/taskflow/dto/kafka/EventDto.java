package taskflow.dto.kafka;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class EventDto {
    private String eventType;
    private String entityId;
    private String entityType;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;
}
