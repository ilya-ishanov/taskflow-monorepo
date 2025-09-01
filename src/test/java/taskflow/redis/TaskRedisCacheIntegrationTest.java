package taskflow.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import taskflow.dto.request.TaskRequestDto;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.entity.User;
import taskflow.enums.Priority;
import taskflow.enums.ProjectStatus;
import taskflow.enums.Status;
import taskflow.repository.ProjectRepository;
import taskflow.repository.TaskRepository;
import taskflow.repository.UserRepository;
import taskflow.service.TaskService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskRedisCacheIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    private Task testTask;
    private Project testProject;
    private User testUser;

    @BeforeAll
    void setup(@Autowired UserRepository userRepository,
               @Autowired ProjectRepository projectRepository,
               @Autowired TaskRepository taskRepository) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(user);

        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Project for caching test");
        project.setOwner(testUser);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        testProject = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Status.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now().plusDays(1));
        task.setAssignedUser(testUser);
        task.setProject(testProject);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        testTask = taskRepository.save(task);
    }

    @Test
    void findById_shouldReturnFromCache_afterFirstCall() {
        Long taskId = testTask.getId();

        Task firstCall = taskService.findById(taskId);
        assertThat(firstCall).isNotNull();

        taskRepository.deleteById(taskId);

        Task secondCall = taskService.findById(taskId);
        assertThat(secondCall).isNotNull();
        assertThat(secondCall.getId()).isEqualTo(taskId);
    }

    @Test
    void update_shouldUpdateCache() {
        Long taskId = testTask.getId();

        TaskRequestDto updateDto = new TaskRequestDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setStatus(Status.IN_PROGRESS);
        updateDto.setPriority(Priority.HIGH);
        updateDto.setDeadline(LocalDateTime.now().plusDays(5));
        updateDto.setUserId(testUser.getId());
        updateDto.setProjectId(testProject.getId());

        Task updatedTask = taskService.update(taskId, updateDto);

        assertThat(updatedTask.getId()).isEqualTo(taskId);
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedTask.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(updatedTask.getPriority()).isEqualTo(Priority.HIGH);

        Object cached = redisTemplate.opsForValue().get("tasks::" + taskId);
        assertThat(cached).isInstanceOf(Task.class);

        Task cachedTask = (Task) cached;
        assertThat(cachedTask.getId()).isEqualTo(taskId);
        assertThat(cachedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(cachedTask.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void delete_shouldEvictFromCache() {
        Long taskId = testTask.getId();

        taskService.delete(taskId);

        Optional<Task> fromDb = taskRepository.findById(taskId);
        assertThat(fromDb).isEmpty();

        Object fromRedis = redisTemplate.opsForValue().get("tasks::" + taskId);
        assertThat(fromRedis).isNull();
    }
}