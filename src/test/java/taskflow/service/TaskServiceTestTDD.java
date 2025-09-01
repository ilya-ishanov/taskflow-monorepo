package taskflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskflow.entity.Task;
import taskflow.enums.Priority;
import taskflow.enums.Status;
import taskflow.repository.TaskRepository;


@ExtendWith(MockitoExtension.class)
public class TaskServiceTestTDD {
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private TaskService taskService;

    @Test
    void filter_byStatusAndPriority_returnsMatchingTasks() {
        Status status = Status.TODO;
        Priority priority = Priority.HIGH;

        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setStatus(status);
        task1.setPriority(priority);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setStatus(status);
        task2.setPriority(priority);

        when(taskRepository.findByStatusAndPriority(status, priority))
                .thenReturn(List.of(task1, task2));

        List<Task> result = taskService.filterByStatusAndPriority(status, priority);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getStatus() == status && t.getPriority() == priority));
        verify(taskRepository, times(1)).findByStatusAndPriority(status, priority);
    }
}
