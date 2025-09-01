package taskflow.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import taskflow.mongo.entity.LogEntry;
import taskflow.mongo.enums.LogEntryLevel;

import java.util.List;

@Repository
public interface LogEntryRepository extends MongoRepository<LogEntry, ObjectId> {
    List<LogEntry> findByLevel(LogEntryLevel level);
}
