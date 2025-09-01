package taskflow.mongo.validation;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import taskflow.exceptions.FileTooLargeException;
import taskflow.exceptions.NotFoundException;
import taskflow.mongo.repository.DocumentFileRepository;

@Component
@RequiredArgsConstructor
public class DocumentFileValidator {
    private final DocumentFileRepository documentFileRepository;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public void validateDocumentExists(ObjectId id) {
        boolean exists = documentFileRepository.existsById(id);
        if (!exists) {
            throw new NotFoundException("Документ с id %s не найден".formatted(id));
        }
    }

    public void fileSizeValidation(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileTooLargeException(
                    "Файл \"%s\" превышает допустимый размер 10 МБ".formatted(file.getOriginalFilename()));
        }
    }

//    public void checkGridFsFileFound(GridFSFile file) {
//        if (file == null) {
//            throw new NotFoundException("Файл в GridFS с id %s не найден".formatted(id));
//        }
//    }
}
