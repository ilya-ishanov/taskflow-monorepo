package taskflow.messaging.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import taskflow.dto.kafka.LogEventDto;
import taskflow.mongo.entity.LogEntry;
import taskflow.mongo.enums.LogEntryLevel;
import taskflow.mongo.repository.LogEntryRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaLogConsumer {
    private final ObjectMapper objectMapper;
    private final LogEntryRepository logEntryRepository;

    @KafkaListener(topics = "taskflow.logs")
    public <T> void consumeMessage(LogEventDto<T> logEventDto) {
        Map<String, Object> context = objectMapper.convertValue(
                logEventDto.getContext(),
                new TypeReference<Map<String, Object>>() {});

        LogEntry logEntry = new LogEntry();
        logEntry.setId(logEntry.getId());
        logEntry.setMessage(logEventDto.getMessage());
        logEntry.setLevel(LogEntryLevel.INFO);
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setContext(context);

        logEntryRepository.save(logEntry);
        log.info("Лог сохранён в MongoDB");
    }
}
