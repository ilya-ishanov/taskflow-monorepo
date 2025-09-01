package taskflow.mongo.dto.request;

import lombok.Getter;
import lombok.Setter;
import taskflow.mongo.enums.TaskHistoryAction;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class TaskHistoryRequestDto {
    private Long taskId;
    private TaskHistoryAction action;
    private Long performedBy;
    private LocalDateTime timestamp;
    private Map<String, Object> details;
}
