package notificationservice.consumer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notificationservice.dto.NotificationRequestDto;
import notificationservice.entity.Notification;
import notificationservice.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationRabbitMQListener {
    private final NotificationRepository notificationRepository;

    @PostConstruct
    public void init() {
        log.info("NotificationRabbitMQListener активен и готов слушать очереди");
    }

    @RabbitListener(queues = "task.notifications")
    public void handleNotification(NotificationRequestDto dto) {
        log.info("Received notification: {}", dto);
        if (dto.getTaskId() == null) {
            log.error("Invalid notification received: {}", dto);
            return;
        }

        Notification notification = new Notification();
        notification.setTaskId(dto.getTaskId());
        notification.setTitle(dto.getTitle());
        notification.setStatus(dto.getStatus());
        notification.setTimestamp(dto.getTimestamp());
        notificationRepository.save(notification);
    }

    @RabbitListener(queues = "task.audit.fanout")
    public void handleNotificationForUpdate(NotificationRequestDto dto) {
        log.warn("Update consumer STARTED!");
        log.info("Received notification: {}", dto);
        if (dto.getTaskId() == null) {
            log.error("Invalid notification received: {}", dto);
            return;
        }

        Notification notification = new Notification();
        notification.setTaskId(dto.getTaskId());
        notification.setTitle(dto.getTitle());
        notification.setStatus(dto.getStatus());
        notification.setTimestamp(dto.getTimestamp());
        notificationRepository.save(notification);
    }

    @RabbitListener(queues = "task.notifications.topic")
    public void handleNotificationForDelete(NotificationRequestDto dto) {
        log.info("Received notification: {}", dto);
        if (dto.getTaskId() == null) {
            log.error("Invalid notification received: {}", dto);
            return;
        }

        Notification notification = new Notification();
        notification.setTaskId(dto.getTaskId());
        notification.setTitle(dto.getTitle());
        notification.setStatus(dto.getStatus());
        notification.setTimestamp(dto.getTimestamp());
        notificationRepository.save(notification);
    }

    @RabbitListener(queues = "task.notifications", containerFactory = "rabbitListenerContainerFactory")
    public void handleNotificationWithError(NotificationRequestDto dto) {
        log.info("Получено сообщение: {}", dto);

        // симулируем ошибку
        if (dto.getTaskId() == -1) {
            log.error("Ошибка обработки — taskId == -1");
            throw new RuntimeException("Ошибка обработки сообщения");
        }

        Notification notification = new Notification();
        notification.setTaskId(dto.getTaskId());
        notification.setTitle(dto.getTitle());
        notification.setStatus(dto.getStatus());
        notification.setTimestamp(dto.getTimestamp());

        notificationRepository.save(notification);
        log.info("Успешно сохранено: {}", notification);
    }
}
