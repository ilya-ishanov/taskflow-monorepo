package taskflow.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.annotations.Document;
import taskflow.enums.ProjectStatus;
import taskflow.enums.Status;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "project-index")
public class ProjectIndex {

    @Id
    private String id;
    private String name;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ProjectStatus status;
}
