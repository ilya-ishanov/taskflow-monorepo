package taskflow.dto.response;

import taskflow.entity.User;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
