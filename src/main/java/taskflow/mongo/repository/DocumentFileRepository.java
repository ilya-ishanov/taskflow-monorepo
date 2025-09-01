package taskflow.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import taskflow.mongo.entity.DocumentFile;

import java.util.List;

@Repository
public interface DocumentFileRepository extends MongoRepository<DocumentFile, ObjectId> {
    List<DocumentFile> findAllByTaskId(Long id);
}
