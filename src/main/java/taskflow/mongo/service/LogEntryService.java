package taskflow.mongo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taskflow.mongo.entity.LogEntry;
import taskflow.mongo.enums.LogEntryLevel;
import taskflow.mongo.repository.LogEntryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogEntryService {
    private final LogEntryRepository logEntryRepository;

    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }

    public List<LogEntry> getLogsByLevel(LogEntryLevel level) {
        return logEntryRepository.findByLevel(level);
    }
}
