package taskflow.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import taskflow.mongo.enums.TaskHistoryAction;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "task_history")
public class TaskHistory {
    @Id
    private ObjectId id;
    private Long taskId;
    private TaskHistoryAction action;
    private Long performedBy;
    private LocalDateTime timestamp;
    private Map<String, Object> details;

    public TaskHistory(Long taskId, TaskHistoryAction action,
                       Long performedBy, LocalDateTime timestamp,
                       Map<String, Object> details) {
        this.taskId = taskId;
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
        this.details = details;
    }
}
