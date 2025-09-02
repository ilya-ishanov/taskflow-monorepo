package eventconsumerservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeadLetterLogDto {
    private String eventType;
    private Long entityId;
    private String entityType;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;
    private String reason;
}


