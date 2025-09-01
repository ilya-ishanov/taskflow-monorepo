package taskflow.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import taskflow.dto.kafka.LogEventDto;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaLogProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void sendLog(String level, String message, T context) {
        LogEventDto<T> dto = new LogEventDto<>();
        dto.setMessage(message);
        dto.setLevel(level);
        dto.setTimestamp(LocalDateTime.now());
        dto.setContext(context);

        kafkaTemplate.send("taskflow.logs", dto);
        log.info("Лог отправлен в Kafka: {}", "taskflow.logs");
    }
}
