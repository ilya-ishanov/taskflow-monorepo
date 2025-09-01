package taskflow.dto.rabbit;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationRequestDto {
    private Long taskId;
    private String title;
    private String status;
    private LocalDateTime timestamp;
}
