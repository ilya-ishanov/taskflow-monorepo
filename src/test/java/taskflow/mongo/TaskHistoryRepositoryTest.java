package taskflow.mongo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import taskflow.mongo.entity.TaskHistory;
import taskflow.mongo.enums.TaskHistoryAction;
import taskflow.mongo.repository.TaskHistoryRepository;

@DataMongoTest
public class TaskHistoryRepositoryTest {
    @Autowired
    private TaskHistoryRepository repository;

    @Test
    void save_validHistory_returnsByTaskId() {
        TaskHistory history = new TaskHistory();
        history.setTaskId(123L);
        history.setAction(TaskHistoryAction.CREATE);
        history.setPerformedBy(1L);
        history.setTimestamp(LocalDateTime.now());

        repository.save(history);

        List<TaskHistory> result = repository.findAllByTaskId(123L);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTaskId()).isEqualTo(123L);
        assertThat(result.get(0).getAction()).isEqualTo(TaskHistoryAction.CREATE);
    }
}
