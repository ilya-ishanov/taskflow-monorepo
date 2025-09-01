package taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import taskflow.enums.ProjectStatus;

import java.util.List;

@Getter
@Setter
public class ProjectRequestDto {

    @NotBlank(message = "Название проекта не может быть пустым")
    @Size(max = 128, message = "Название проекта не должно превышать 128 символов")
    private String name;

    @Size(max = 256, message = "Описание не должно превышать 256 символов")
    private String description;

    @NotNull(message = "Статус проекта обязателен")
    private ProjectStatus status;

    @NotNull(message = "ID владельца проекта обязателен")
    private Long ownerId;

    @NotEmpty(message = "У проекта должен быть хотя бы один участник")
    private List<Long> users;
}
