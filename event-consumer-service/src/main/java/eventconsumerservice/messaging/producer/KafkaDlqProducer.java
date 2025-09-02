package eventconsumerservice.messaging.producer;

import eventconsumerservice.dto.request.DeadLetterLogDto;
import eventconsumerservice.entity.DeadLetterLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaDlqProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeadLetterLog(DeadLetterLogDto dto) {
        kafkaTemplate.send("taskflow.dlq", dto);
    }
}
