package taskflow.mongo.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import taskflow.mongo.entity.LogEntry;
import taskflow.mongo.enums.LogEntryLevel;
import taskflow.mongo.service.LogEntryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logs")
public class LogEntryController {
    private final LogEntryService logEntryService;

    @GetMapping
    public List<LogEntry> getLogs(@RequestParam(required = false) LogEntryLevel level) {
        if (level != null) {
            return logEntryService.getLogsByLevel(level);
        }
        return logEntryService.getAllLogs();
    }
}
