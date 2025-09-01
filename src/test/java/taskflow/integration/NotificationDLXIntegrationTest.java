package taskflow.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import taskflow.dto.rabbit.NotificationRequestDto;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationDLXIntegrationTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @BeforeEach
    public void purgeDLXQueue() {
        amqpAdmin.purgeQueue("task.dlx.notifications", true);
    }

    @Test
    public void consumeMessageShouldRouteToDlxWhenMessageProcessingFails() throws Exception {
        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setTaskId(-1L);
        dto.setTitle("Error Test");
        dto.setStatus("FAIL");
        dto.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend("taskflow.notification.direct.exchange",
                "task.notification.created", dto);

        Thread.sleep(7000);

        Object failedMessage = rabbitTemplate.receiveAndConvert("task.dlx.notifications");
        assertNotNull(failedMessage);
    }
}
