package taskflow.mongo.dto;

import org.springframework.core.io.Resource;

public record DocumentDownload(String fileName,
                               String fileType,
                               Resource resource) {
}
