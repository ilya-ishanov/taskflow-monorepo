package taskflow.dto.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskLogContext {
    private Long taskId;
    private String title;
    private String status;
}
