package taskflow.mongo.dto.response;

import org.bson.types.ObjectId;
import taskflow.mongo.entity.DocumentFile;

import java.time.LocalDateTime;

public record DocumentFileResponseDto(
        ObjectId id,
        Long taskId,
        String fileName,
        String fileType,
        LocalDateTime uploadTime,
        Long size
) {
    public static DocumentFileResponseDto from(DocumentFile documentFile) {
        return new DocumentFileResponseDto(
                documentFile.getId(),
                documentFile.getTaskId(),
                documentFile.getFileName(),
                documentFile.getFileType(),
                documentFile.getUploadTime(),
                documentFile.getSize()
        );
    }
}
