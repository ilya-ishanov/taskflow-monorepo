package taskflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskflow.dto.request.UserRequestDto;
import taskflow.entity.Project;
import taskflow.entity.User;
import taskflow.exceptions.EntityNotFoundException;
import taskflow.repository.ProjectRepository;
import taskflow.repository.UserRepository;
import taskflow.validator.Validator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private Validator validator;

    // Positive create user
    @Test
    public void createUser_validInput_success() {
        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Name");
        dto.setLastName("Last");
        dto.setEmail("test@mail.kz");
        dto.setActive(true);
        dto.setProjectsIds(List.of(1L, 2L, 3L));

        List<Project> projects = List.of(new Project(), new Project(), new Project());
        when(projectRepository.findAllById(dto.getProjectsIds())).thenReturn(projects);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.createUser(dto);

        assertEquals(dto.getFirstName(), user.getFirstName());
        assertEquals(dto.getLastName(), user.getLastName());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getActive(), user.getActive());
        assertEquals(projects, user.getProjects());

        verify(userRepository, times(1)).save(any(User.class));
    }

    // Negative UserRequestDto переданы несуществующие ID проектов,
    // метод createUser всё равно создаёт пользователя,
    @Test
    public void createUser_noProjects_emptyProjectList() {
        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setEmail("test@mail.com");
        dto.setActive(true);
        dto.setProjectsIds(List.of(99L, 100L)); // несуществующие проекты

        // возвращаем пустой список проектов
        when(projectRepository.findAllById(dto.getProjectsIds())).thenReturn(List.of());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.createUser(dto);

        assertTrue(user.getProjects().isEmpty());
        verify(userRepository).save(any());
    }

    // Positive findByID
    @Test
    public void getUser_byId_returnUser() {
        User user = new User();
        user.setFirstName("Name");
        user.setLastName("Last");
        user.setEmail("test@mail.com");
        user.setActive(true);
        user.setProjects(List.of(new Project(), new Project()));


        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);
        assertEquals("Name", result.getFirstName());
        assertEquals("Last", result.getLastName());
        assertEquals("test@mail.com", result.getEmail());
        assertTrue(result.getActive());
        assertEquals(2, result.getProjects().size());

        verify(userRepository, times(1)).findById(1L);
    }

    // Negative findByID
    @Test
    public void getUser_byNonexistentId_throwException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrowsExactly(EntityNotFoundException.class, () -> userService.findById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    // Validation, Проверить валидацию данных (например, уникальность email)
    @Test
    public void createUser_duplicateEmail_throwException() {
        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Name");
        dto.setLastName("Last");
        dto.setEmail("test@mail.com");
        dto.setActive(true);
        dto.setProjectsIds(List.of(1L, 2L, 3L));

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        assertThrowsExactly(IllegalStateException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }
}
