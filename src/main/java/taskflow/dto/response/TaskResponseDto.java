package taskflow.dto.response;

import taskflow.entity.Task;
import taskflow.enums.Priority;
import taskflow.enums.Status;
import java.time.LocalDateTime;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        Status status,
        Priority priority,
        LocalDateTime deadline,
        Long userId,
        Long projectId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponseDto from(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDeadline(),
                task.getAssignedUser().getId(),
                task.getProject().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
