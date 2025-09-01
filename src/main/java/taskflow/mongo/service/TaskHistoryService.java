package taskflow.mongo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taskflow.mongo.dto.response.TaskHistoryResponseDto;
import taskflow.mongo.entity.TaskHistory;
import taskflow.mongo.enums.TaskHistoryAction;
import taskflow.mongo.repository.TaskHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskHistoryService {
    private final TaskHistoryRepository taskHistoryRepository;

    public List<TaskHistoryResponseDto> getTaskHistory(Long taskId) {
        return taskHistoryRepository.findAllByTaskId(taskId)
                .stream()
                .map(TaskHistoryResponseDto::from)
                .toList();
    }

    public void logFileUpload(Long taskId, Long userId, String fileName, String fileId) {
        Map<String, Object> details = Map.of( "fileName", fileName, "fileId", fileId);
        TaskHistory history = new TaskHistory(taskId, TaskHistoryAction.CREATE, userId, LocalDateTime.now(), details);
        taskHistoryRepository.save(history);
    }

    public void logFileDelete(Long taskId, Long userId, String fileName, String fileId) {
        Map<String, Object> details = Map.of( "fileName", fileName, "fileId", fileId);
        TaskHistory history = new TaskHistory(taskId, TaskHistoryAction.DELETE, userId, LocalDateTime.now(), details);
        taskHistoryRepository.save(history);
    }
}
