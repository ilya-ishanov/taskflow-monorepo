package taskflow.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskflow.elasticsearch.dto.StatusPriorityCount;
import taskflow.elasticsearch.dto.StatusProjectCount;
import taskflow.elasticsearch.dto.UserIdCommentCount;
import taskflow.elasticsearch.service.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final TaskAnalyticService taskAnalyticService;
    private final ProjectAnalyticService projectAnalyticService;
    private final CommentAnalyticService commentAnalyticsService;

    @GetMapping("/task")
    public List<StatusPriorityCount> getTaskAnalytics() throws IOException {
        return taskAnalyticService.getTaskAnalytics();
    }

    @GetMapping("/project")
    public List<StatusProjectCount> getProjectAnalytics() throws IOException {
        return projectAnalyticService.getProjectAnalytics();
    }

    @GetMapping("/comment")
    public List<UserIdCommentCount> getCommentAnalytics() throws IOException {
        return commentAnalyticsService.getCommentAnalytics();
    }
}
