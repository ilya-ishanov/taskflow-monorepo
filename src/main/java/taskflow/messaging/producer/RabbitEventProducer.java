package taskflow.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import taskflow.confings.RabbitMQConfig;
import taskflow.dto.rabbit.NotificationRequestDto;
import taskflow.mongo.dto.request.TaskHistoryRequestDto;

@Service
@RequiredArgsConstructor
public class RabbitEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendTaskHistoryEvent(TaskHistoryRequestDto dto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TASK_HISTORY_EXCHANGE,
                "task.history.created", dto);
    }

    public void sendTaskCreatedNotification(NotificationRequestDto dto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATION_DIRECT,
                "task.notification.created", dto);
    }

    public void sendTaskUpdateNotification(NotificationRequestDto dto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATION_FANOUT, "", dto);
    }

    public void sendTaskDeleteNotification(NotificationRequestDto dto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATION_TOPIC,
                "task.deleted", dto);
    }

}
