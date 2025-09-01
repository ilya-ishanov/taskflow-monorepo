package taskflow.mongo.dto.response;

import org.bson.types.ObjectId;
import taskflow.mongo.entity.TaskHistory;
import taskflow.mongo.enums.TaskHistoryAction;

import java.time.LocalDateTime;
import java.util.Map;

public record TaskHistoryResponseDto(
        ObjectId id,
        Long taskId,
        TaskHistoryAction action,
        Long performedBy,
        LocalDateTime timestamp,
        Map<String, Object>details
) {
    public static TaskHistoryResponseDto from(TaskHistory taskHistory) {
        return new TaskHistoryResponseDto(
                taskHistory.getId(),
                taskHistory.getTaskId(),
                taskHistory.getAction(),
                taskHistory.getPerformedBy(),
                taskHistory.getTimestamp(),
                taskHistory.getDetails()
        );
    }
}
