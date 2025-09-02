package com.example.NotificationService.integration;

import notificationservice.NotificationServiceApplication;
import notificationservice.dto.NotificationRequestDto;
import notificationservice.entity.Notification;
import notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationListenerIntegrationTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationRepository repository;

    @BeforeEach
    public void clear() {
        repository.deleteAll();
    }

    @Test
    public void consumeMessageShouldSaveToDatabaseWhenMessageIsValid() throws Exception {
        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setTaskId(456L);
        dto.setTitle("Test Notification");
        dto.setStatus("CREATE");
        dto.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend("taskflow.notification.direct.exchange",
                "task.notification.created", dto);

        Thread.sleep(2000);

        List<Notification> notifications = repository.findAll();
        assertEquals(1, notifications.size());
        assertEquals(456L, notifications.get(0).getTaskId());
    }
}
