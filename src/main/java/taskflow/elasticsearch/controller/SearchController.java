package taskflow.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskflow.elasticsearch.dto.TaskSearchResponseDto;
import taskflow.elasticsearch.entity.CommentIndex;
import taskflow.elasticsearch.entity.ProjectIndex;
import taskflow.elasticsearch.entity.TaskIndex;
import taskflow.elasticsearch.service.CommentSearchService;
import taskflow.elasticsearch.service.ProjectSearchService;
import taskflow.elasticsearch.service.TaskSearchService;
import taskflow.enums.Priority;
import taskflow.enums.Status;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final TaskSearchService taskSearchService;
    private final ProjectSearchService projectSearchService;
    private final CommentSearchService commentSearchService;

    @GetMapping("/tasks/{query}/{status}/{priority}")
    public List<TaskSearchResponseDto> searchTasks(@PathVariable String query, @PathVariable Status status, @PathVariable Priority priority) throws IOException {
        return taskSearchService.searchTasks(query, status, priority);
    }

    @GetMapping("/projects/{query}")
    public List<ProjectIndex> searchProjects(@PathVariable String query) throws IOException {
        return projectSearchService.searchProjects(query);
    }

    @GetMapping("/comments/{query}/{taskId}")
    public List<CommentIndex> searchComments(@PathVariable String query, @PathVariable String taskId) throws IOException {
       return commentSearchService.searchComments(query, taskId);
    }
}
