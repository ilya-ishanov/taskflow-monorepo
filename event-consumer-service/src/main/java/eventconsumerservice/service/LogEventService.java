package eventconsumerservice.service;

import eventconsumerservice.entity.DeadLetterLog;
import eventconsumerservice.entity.EventLog;
import eventconsumerservice.repository.DeadLetterLogRepository;
import eventconsumerservice.repository.LogEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogEventService {
    private final LogEventRepository logEventRepository;
    private final DeadLetterLogRepository deadLetterLogRepository;

    public List<EventLog> getProcessedEvents() {
        return logEventRepository.findAll();
    }

    public List<DeadLetterLog> getAllErrors() {
        return deadLetterLogRepository.findAll();
    }
}
