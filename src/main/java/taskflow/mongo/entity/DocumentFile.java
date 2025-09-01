package taskflow.mongo.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "documents")
public class DocumentFile {
    @Id
    private ObjectId id;
    private Long taskId;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadTime;
    private Long size;
}
