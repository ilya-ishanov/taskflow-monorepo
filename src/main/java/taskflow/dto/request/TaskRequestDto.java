package taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import taskflow.enums.Priority;
import taskflow.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequestDto {
    @NotBlank(message = "Название не может быть пустым")
    private String title;

    @Size(max = 256, message = "Описание задачи не должно превышать 256 символов")
    private String description;

    @NotNull(message = "Статус обязателен")
    private Status status;

    @NotNull(message = "Дедлайн обязателен")
    private Priority priority;

    @NotNull(message = "ID пользователя обязателен")
    private LocalDateTime deadline;

    @NotNull(message = "ID юзера обязателен")
    private Long userId;

    @NotNull(message = "ID проекта обязателен")
    private Long projectId;
}