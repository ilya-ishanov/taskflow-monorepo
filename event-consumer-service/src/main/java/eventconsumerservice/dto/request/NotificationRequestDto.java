package eventconsumerservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    private Long receiverId;
    private String title;
    private String message;
    private Long taskId;
    private LocalDateTime timestamp;
}
