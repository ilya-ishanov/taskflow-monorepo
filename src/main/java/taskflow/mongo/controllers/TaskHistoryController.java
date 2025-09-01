package taskflow.mongo.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import taskflow.mongo.dto.response.TaskHistoryResponseDto;
import taskflow.mongo.service.TaskHistoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskHistoryController {
    private final TaskHistoryService taskHistoryService;

    @GetMapping("/tasks/{taskId}/history")
    public ResponseEntity<List<TaskHistoryResponseDto>> getTaskHistory(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskHistoryService.getTaskHistory(taskId));
    }
}
