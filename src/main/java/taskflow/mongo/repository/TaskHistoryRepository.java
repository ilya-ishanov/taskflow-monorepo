package taskflow.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import taskflow.mongo.dto.response.TaskHistoryResponseDto;
import taskflow.mongo.entity.TaskHistory;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends MongoRepository<TaskHistory, ObjectId> {
    List<TaskHistory> findAllByTaskId(Long id);
}
