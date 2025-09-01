package taskflow.dto.response;

import taskflow.entity.Project;
import taskflow.enums.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectResponseDto(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        Long ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponseDto from(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getOwner().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
