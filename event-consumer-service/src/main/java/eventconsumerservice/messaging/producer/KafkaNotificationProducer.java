package eventconsumerservice.messaging.producer;

import eventconsumerservice.dto.request.NotificationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaNotificationProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(NotificationRequestDto dto) {
        kafkaTemplate.send("task.notification.created", dto);
    }
}
