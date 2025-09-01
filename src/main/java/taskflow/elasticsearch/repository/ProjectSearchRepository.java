package taskflow.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import taskflow.elasticsearch.entity.ProjectIndex;

@Repository
public interface ProjectSearchRepository extends ElasticsearchRepository<ProjectIndex, String> {
}
