package taskflow.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import taskflow.enums.Priority;
import taskflow.enums.Status;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "task-index")
public class TaskIndex {

    @Id
    private String id;
    private String title;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Status status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Priority priority;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private OffsetDateTime deadline;
    private Long assignedUserId;
    private Long projectId;
}
