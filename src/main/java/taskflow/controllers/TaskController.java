package taskflow.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taskflow.dto.request.TaskRequestDto;
import taskflow.dto.response.TaskResponseDto;
import taskflow.mongo.dto.request.TaskHistoryRequestDto;
import taskflow.service.TaskService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> crateTask(@RequestBody @Valid TaskRequestDto requestDto) {
        TaskResponseDto response = TaskResponseDto
                .from(taskService.createTask(requestDto));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> findAll(Pageable pageable) {
        Page<TaskResponseDto> page = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> findById(@PathVariable Long id) {
        TaskResponseDto response = TaskResponseDto
                .from(taskService.findById(id));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(@PathVariable Long id,
                                                  @RequestBody @Valid TaskRequestDto requestDto) {
        TaskResponseDto response = TaskResponseDto
                .from(taskService.update(id, requestDto));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.ok("Задача успешно удаленна");
    }
}