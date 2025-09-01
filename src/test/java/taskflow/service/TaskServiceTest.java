package taskflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskflow.dto.request.TaskRequestDto;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.entity.User;
import taskflow.enums.Priority;
import taskflow.enums.Status;
import taskflow.exceptions.EntityNotFoundException;
import taskflow.repository.ProjectRepository;
import taskflow.repository.TaskRepository;
import taskflow.repository.UserRepository;
import taskflow.validator.Validator;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Validator taskValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Captor
    private ArgumentCaptor<Task> captor;

    // taskValidator без метода setup не отрабатывает
    @BeforeEach
    public void setup() {
        try {
            Field field = TaskService.class.getDeclaredField("taskValidator");
            field.setAccessible(true);
            field.set(taskService, new Validator()); // подставили вручную
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // попробовал Captor
//    @Test
//    public void createTask_validInput_success() {
//        TaskRequestDto dto = new TaskRequestDto();
//        dto.setTitle("title");
//        dto.setDescription("description");
//        dto.setStatus(Status.TODO);
//        dto.setPriority(Priority.MEDIUM);
//        dto.setDeadline(LocalDateTime.now());
//        dto.setProjectId(1L);
//        dto.setUserId(1L);
//
//        User mockUser = new User();
//        mockUser.setId(1L);
//
//        Project mockProject = new Project();
//        mockProject.setId(1L);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
//        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
//        when(taskRepository.save(any(Task.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        Task task = taskService.createTask(dto);
//        verify(taskRepository, times(1)).save(captor.capture());
//
//        Task capturedTask = captor.getValue();
//
//        assertEquals(task.getTitle(), capturedTask.getTitle());
//        assertEquals(task.getDescription(), capturedTask.getDescription());
//        assertEquals(task.getStatus(), capturedTask.getStatus());
//        assertEquals(task.getPriority(), capturedTask.getPriority());
//        assertEquals(task.getAssignedUser(), capturedTask.getAssignedUser());
//        assertEquals(task.getProject(), capturedTask.getProject());
//
//        assertEquals(dto.getTitle(), capturedTask.getTitle());
//        assertEquals(dto.getDescription(), capturedTask.getDescription());
//        assertEquals(dto.getStatus(), capturedTask.getStatus());
//        assertEquals(dto.getPriority(), capturedTask.getPriority());
//        assertEquals(mockUser, capturedTask.getAssignedUser());
//        assertEquals(mockProject, capturedTask.getProject());
//    }

    // negative findById
    @Test
    public void getTask_byNonexistentId_throwException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrowsExactly(EntityNotFoundException.class, () -> taskService.findById(1L));
    }

    // positive findById
    @Test
    public void getTask_byId_returnTask() {
        User mockUser = new User();
        mockUser.setId(1L);

        Project mockProject = new Project();
        mockProject.setId(1L);

        Task task = new Task();
        task.setTitle("title");
        task.setDescription("description");
        task.setStatus(Status.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now());
        task.setProject(mockProject);
        task.setAssignedUser(mockUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        Task result = taskService.findById(1L);

        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());
        assertEquals(task.getStatus(), result.getStatus());
        assertEquals(task.getPriority(), result.getPriority());
        assertEquals(mockUser, result.getAssignedUser());
        assertEquals(mockProject, result.getProject());

        verify(taskRepository, times(1)).findById(1L);
    }

    // positive delete
//    @Test
//    public void deleteTask_existingId_success() {
//        // Мокаем, что задача существует
//        when(taskRepository.existsById(1L)).thenReturn(true);
//        // Вызываем удаление
//        taskService.delete(1L);
//        // Проверяем, что удаление было вызвано 1 раз
//        verify(taskRepository, times(1)).deleteById(1L);
//    }

    // negative delete
//    @Test
//    public void deleteTask_nonexistentId_throwException() {
//        when(taskRepository.existsById(1L)).thenReturn(false);
//        assertThrowsExactly(EntityNotFoundException.class, () -> taskService.delete(1L));
//        verify(taskRepository, times(0)).deleteById(1L);
//    }

    // тестирование валидатора
    @Test
    public void validateEntity_missingEntity_throwException() {
        Validator validator = new Validator();
        assertThrows(EntityNotFoundException.class, () ->
                validator.validateEntityExists(100L, taskRepository, "Tasks")
        );
    }

    // positive update
//    @Test
//    public void updateTask_validInput_success() {
//        TaskRequestDto dto = new TaskRequestDto();
//        dto.setTitle("title");
//        dto.setDescription("description");
//        dto.setStatus(Status.TODO);
//        dto.setPriority(Priority.MEDIUM);
//        dto.setDeadline(LocalDateTime.now());
//        dto.setProjectId(1L);
//        dto.setUserId(1L);
//
//        User mockUser = new User();
//        mockUser.setId(1L);
//
//        Project mockProject = new Project();
//        mockProject.setId(1L);
//
//        Task task = new Task();
//        task.setTitle("title");
//        task.setDescription("description");
//        task.setStatus(Status.TODO);
//        task.setPriority(Priority.MEDIUM);
//        task.setDeadline(LocalDateTime.now());
//        task.setProject(mockProject);
//        task.setAssignedUser(mockUser);
//
//        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
//        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
//
//        Task result = taskService.update(1L, dto);
//
//        assertEquals(dto.getTitle(), result.getTitle());
//        assertEquals(dto.getDescription(), result.getDescription());
//        assertEquals(dto.getStatus(), result.getStatus());
//        assertEquals(dto.getPriority(), result.getPriority());
//        assertEquals(mockUser, result.getAssignedUser());
//        assertEquals(mockProject, result.getProject());
//
//        verify(taskRepository, times(1)).findById(1L);
//    }

    // negative update
//    @Test
//    public void updateTaskUserNotFoundShouldThrowException() {
//        TaskRequestDto dto = new TaskRequestDto();
//        dto.setTitle("title");
//        dto.setDescription("description");
//        dto.setStatus(Status.TODO);
//        dto.setPriority(Priority.MEDIUM);
//        dto.setDeadline(LocalDateTime.now());
//        dto.setProjectId(1L);
//        dto.setUserId(1L);
//
//        User mockUser = new User();
//        mockUser.setId(1L);
//
//        Project mockProject = new Project();
//        mockProject.setId(1L);
//
//        Task task = new Task();
//        task.setTitle("title");
//        task.setDescription("description");
//        task.setStatus(Status.TODO);
//        task.setPriority(Priority.MEDIUM);
//        task.setDeadline(LocalDateTime.now());
//        task.setProject(mockProject);
//        task.setAssignedUser(mockUser);
//
//        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThrowsExactly(EntityNotFoundException.class, () -> taskService.update(1L, dto));
//    }

    // negative update
//    @Test
//    public void updateTask_projectNotFound_throwException() {
//        TaskRequestDto dto = new TaskRequestDto();
//        dto.setTitle("title");
//        dto.setDescription("description");
//        dto.setStatus(Status.TODO);
//        dto.setPriority(Priority.MEDIUM);
//        dto.setDeadline(LocalDateTime.now());
//        dto.setProjectId(1L);
//        dto.setUserId(1L);
//
//        User mockUser = new User();
//        mockUser.setId(1L);
//
//        Project mockProject = new Project();
//        mockProject.setId(1L);
//
//        Task task = new Task();
//        task.setTitle("title");
//        task.setDescription("description");
//        task.setStatus(Status.TODO);
//        task.setPriority(Priority.MEDIUM);
//        task.setDeadline(LocalDateTime.now());
//        task.setProject(mockProject);
//        task.setAssignedUser(mockUser);
//
//        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
//        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThrowsExactly(EntityNotFoundException.class, () -> taskService.update(1L, dto));
//    }


}
