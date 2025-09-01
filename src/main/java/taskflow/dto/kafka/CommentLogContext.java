package taskflow.dto.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentLogContext {
    private Long commentId;
    private Long taskId;
    private Long userId;
}
