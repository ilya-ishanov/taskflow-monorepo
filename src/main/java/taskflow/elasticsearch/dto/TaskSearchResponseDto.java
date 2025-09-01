package taskflow.elasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import taskflow.enums.Priority;
import taskflow.enums.Status;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchResponseDto {
    private String id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private OffsetDateTime deadline;
}
