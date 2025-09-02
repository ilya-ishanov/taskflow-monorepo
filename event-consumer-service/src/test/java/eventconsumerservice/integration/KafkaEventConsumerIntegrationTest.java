package eventconsumerservice.integration;
import eventconsumerservice.dto.request.EventDto;
import eventconsumerservice.entity.DeadLetterLog;
import eventconsumerservice.entity.EventLog;
import eventconsumerservice.repository.DeadLetterLogRepository;
import eventconsumerservice.repository.LogEventRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = { "taskflow.events", "taskflow.dlq", "task.notification.created" })
@ActiveProfiles("test")
class KafkaEventConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private LogEventRepository logEventRepository;

    @Autowired
    private DeadLetterLogRepository deadLetterLogRepository;

    @BeforeEach
    void cleanDB() {
        logEventRepository.deleteAll();
        deadLetterLogRepository.deleteAll();
    }

    @Test
    void shouldProcessTaskEventAndSaveToEventLog() {
        EventDto event = new EventDto(
                "CREATE", 100L, "TASK",
                Map.of("name", "Test Task"),
                LocalDateTime.now()
        );

        kafkaTemplate.send("taskflow.events", event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    List<EventLog> logs = logEventRepository.findAll();
                    assertThat(logs)
                            .withFailMessage("TASK должен быть сохранён в EventLog")
                            .hasSize(1);

                    assertThat(logs.get(0).getEntityType()).isEqualTo("TASK");
                });
    }

    @Test
    void shouldProcessProjectEventAndNotLogToEventLog() {
        EventDto event = new EventDto(
                "UPDATE", 101L, "PROJECT",
                Map.of("name", "ProjectX", "description", "desc", "status", "ACTIVE"),
                LocalDateTime.now()
        );

        kafkaTemplate.send("taskflow.events", event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    List<EventLog> logs = logEventRepository.findAll();
                    assertThat(logs)
                            .withFailMessage("PROJECT не должен сохраняться в EventLog")
                            .isEmpty();
                });
    }

    @Test
    void shouldIgnoreUnknownEntityTypeWithoutErrorsOrDLQ() {
        EventDto event = new EventDto(
                "CREATE", 999L, "SOMETHING_UNKNOWN",
                Map.of("key", "value"),
                LocalDateTime.now()
        );

        kafkaTemplate.send("taskflow.events", event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    List<EventLog> logs = logEventRepository.findAll();
                    List<DeadLetterLog> dlqs = deadLetterLogRepository.findAll();

                    assertThat(logs).isEmpty(); // ничего не сохраняем
                    assertThat(dlqs).isEmpty(); // не кидаем в DLQ
                });
    }
}