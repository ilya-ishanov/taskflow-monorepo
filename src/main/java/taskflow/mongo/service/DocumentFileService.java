package taskflow.mongo.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import taskflow.exceptions.NotFoundException;
import taskflow.mongo.dto.DocumentDownload;
import taskflow.mongo.dto.response.DocumentFileResponseDto;
import taskflow.exceptions.FileStorageException;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import taskflow.mongo.entity.DocumentFile;
import taskflow.mongo.repository.DocumentFileRepository;
import taskflow.mongo.utils.MongoIdUtils;
import taskflow.mongo.validation.DocumentFileValidator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentFileService {
    private final DocumentFileRepository documentFileRepository;
    private final GridFsTemplate gridFsTemplate;
    private final DocumentFileValidator validator;
    private final TaskHistoryService taskHistoryService;

    public DocumentFile uploadDocumentForTask(MultipartFile file, Long taskId, Long userId) {
        validator.fileSizeValidation(file);

        ObjectId objectId = storeFile(file);
        DocumentFile documentFile = saveMetadata(file, objectId, taskId);
        taskHistoryService.logFileUpload(taskId, userId, documentFile.getFileName(), documentFile.getId().toString());
        printAllIds();
        return documentFile;
    }

    public List<DocumentFileResponseDto> getAllDocuments(Long taskId) {
        return documentFileRepository.findAllByTaskId(taskId)
                .stream()
                .map(DocumentFileResponseDto::from)
                .toList();
    }

    public void deleteByString(String id, Long userId) {
        ObjectId objectId = MongoIdUtils.parse(id);
        deleteDocumentById(objectId, userId);
    }

    public void deleteDocumentById(ObjectId id, Long userId) {
        validator.validateDocumentExists(id);
        DocumentFile documentFile = documentFileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Файл с id %s не найден".formatted(id)));

        taskHistoryService.logFileDelete(documentFile.getTaskId(), userId, documentFile.getFileName(), documentFile.getId().toString());
        documentFileRepository.deleteById(id);
    }

    public DocumentDownload downloadFile(String id) {
        ObjectId objectId = MongoIdUtils.parse(id);
        DocumentFile documentFile = documentFileRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Файл с id %s не найден".formatted(id)));

        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
        if (file == null) {
            throw new NotFoundException("Файл в GridFS с id %s не найден".formatted(id));
        }
        Resource resource = gridFsTemplate.getResource(file);

        return new DocumentDownload(
                documentFile.getFileName(),
                documentFile.getFileType(),
                resource
        );
    }

    // private методы
    private ObjectId storeFile(MultipartFile file) {
        try {
                return gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );
        } catch (IOException e) {
            throw new FileStorageException("Ошибка при сохранении файла в GridFS", e);
        }
    }

    private DocumentFile saveMetadata(MultipartFile file, ObjectId objectId, Long taskId) {
        DocumentFile documentFile = new DocumentFile();
        documentFile.setId(objectId);
        documentFile.setTaskId(taskId);
        documentFile.setFileName(file.getOriginalFilename());
        documentFile.setFileType(file.getContentType());
        documentFile.setUploadTime(LocalDateTime.now());
        documentFile.setSize(file.getSize());
        return documentFileRepository.save(documentFile);
    }

    private void printAllIds() {
        documentFileRepository.findAll().forEach(doc ->
                System.out.println("ObjectId = " + doc.getId())
        );
    }
}
