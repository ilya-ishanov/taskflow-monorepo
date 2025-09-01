package taskflow.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import taskflow.elasticsearch.entity.TaskIndex;

@Repository
public interface TaskSearchRepository extends ElasticsearchRepository<TaskIndex, String> {
}
