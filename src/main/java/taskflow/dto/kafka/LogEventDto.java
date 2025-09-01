package taskflow.dto.kafka;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LogEventDto<T> {
    private String level;
    private String message;
    private LocalDateTime timestamp;
    private T context;
}
