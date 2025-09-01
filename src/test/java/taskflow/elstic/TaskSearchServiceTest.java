package taskflow.elstic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskflow.elasticsearch.entity.TaskIndex;
import taskflow.elasticsearch.repository.TaskSearchRepository;
import taskflow.elasticsearch.service.TaskSearchService;
import taskflow.entity.Project;
import taskflow.entity.Task;
import taskflow.entity.User;
import taskflow.enums.Priority;
import taskflow.enums.Status;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TaskSearchServiceTest {

    @Mock
    private TaskSearchRepository repository;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private TaskSearchService taskSearchService;

    @Test
    void indexTask_ValidTask_SavesToRepository() {
        // GIVEN: валидный Task с нужными данными
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Status.TODO);
        task.setPriority(Priority.HIGH);
        task.setDeadline(LocalDateTime.now());

        User user = new User();
        user.setId(2L);
        task.setAssignedUser(user);

        Project project = new Project();
        project.setId(3L);
        task.setProject(project);

        // WHEN: индексируем
        taskSearchService.indexTask(task);

        // THEN: должен быть вызван save в репозитории
        verify(repository).save(any(TaskIndex.class));
    }
}