package taskflow.dto.response;

import taskflow.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        String content,
        Long taskId,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
          comment.getId(),
          comment.getContent(),
          comment.getTask().getId(),
          comment.getUser().getId(),
          comment.getCreatedAt(),
          comment.getUpdatedAt()
        );
    }
}
