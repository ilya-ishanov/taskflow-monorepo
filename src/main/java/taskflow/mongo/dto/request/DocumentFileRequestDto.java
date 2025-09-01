package taskflow.mongo.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentFileRequestDto {
    private Long taskId;
    private String fileName;
    private String fileType;
}
