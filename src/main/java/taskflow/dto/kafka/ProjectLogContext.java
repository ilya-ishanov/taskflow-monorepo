package taskflow.dto.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectLogContext {
    private Long projectId;
    private String name;
    private String status;
}
