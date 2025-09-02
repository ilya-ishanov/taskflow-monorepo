package eventconsumerservice.entity;

import eventconsumerservice.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "dead_letter_log")
public class DeadLetterLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventType;
    private Long entityId;
    private String entityType;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> payload;
    private Boolean processed;
    private LocalDateTime createdAt;
    private String reason;
}
