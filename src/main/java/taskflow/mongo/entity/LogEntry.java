package taskflow.mongo.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import taskflow.mongo.enums.LogEntryLevel;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "logs")
@Getter
@Setter
public class LogEntry {
    @Id
    private ObjectId id;
    private LogEntryLevel level;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> context;
}
