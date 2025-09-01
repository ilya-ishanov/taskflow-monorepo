package taskflow.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import taskflow.confings.RabbitMQConfig;
import taskflow.mongo.dto.request.TaskHistoryRequestDto;
import taskflow.mongo.entity.TaskHistory;
import taskflow.mongo.repository.TaskHistoryRepository;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RabbitEventConsumer {
    private final TaskHistoryRepository taskHistoryRepository;

    @RabbitListener(queues = RabbitMQConfig.TASK_HISTORY_QUEUE)
    public void consumeTaskEvent(TaskHistoryRequestDto dto, Channel channel, Message message) throws IOException {
        try {
            TaskHistory taskHistory = new TaskHistory();
            taskHistory.setTaskId(dto.getTaskId());
            taskHistory.setTimestamp(dto.getTimestamp());
            taskHistory.setDetails(dto.getDetails());
            taskHistory.setAction(dto.getAction());
            taskHistory.setPerformedBy(dto.getPerformedBy());

            taskHistoryRepository.save(taskHistory);

            //подтверждаем вручную
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //Если ошибка — отправляем в DLX
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
