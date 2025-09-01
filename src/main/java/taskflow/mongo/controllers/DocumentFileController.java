package taskflow.mongo.controllers;

import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import taskflow.mongo.dto.DocumentDownload;
import taskflow.mongo.dto.response.DocumentFileResponseDto;
import taskflow.mongo.entity.DocumentFile;
import taskflow.mongo.service.DocumentFileService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentFileController {
    private final DocumentFileService documentFileService;

    @PostMapping("/tasks/{taskId}/documents")
    public ResponseEntity<DocumentFileResponseDto> uploadDocumentForTask(
            @RequestParam("file") MultipartFile file, @PathVariable Long taskId, @RequestParam Long userId) {
        DocumentFile documentFile = documentFileService.uploadDocumentForTask(file, taskId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DocumentFileResponseDto.from(documentFile));
    }

    @GetMapping("/tasks/{taskId}/documents")
    public ResponseEntity<List<DocumentFileResponseDto>> getAllDocuments(@PathVariable Long taskId) {
        return ResponseEntity.ok(documentFileService.getAllDocuments(taskId));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestParam Long userId) {
        documentFileService.deleteByString(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        DocumentDownload document = documentFileService.downloadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.fileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.fileName() + "\"")
                .body(document.resource());
    }

}
