package taskflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRequestDto {

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 128, message = "Имя не должно превышать 128 символов")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(max = 128, message = "Фамилия не должна превышать 128 символов")
    private String lastName;

    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email обязателен")
    @Size(max = 254, message = "Email не должен превышать 128 символов")
    private String email;

    @NotNull(message = "Активность пользователя должна быть указана")
    private Boolean active;

    private List<Long> projectsIds;
}
