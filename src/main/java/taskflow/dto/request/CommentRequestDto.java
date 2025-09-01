package taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {

    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(max = 624, message = "Комментарий не должен превышать 512 символов")
    private String content;

    @NotNull(message = "ID задачи обязателен")
    private Long taskId;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;
}
