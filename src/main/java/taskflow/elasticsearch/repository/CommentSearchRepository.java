package taskflow.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import taskflow.elasticsearch.entity.CommentIndex;

@Repository
public interface CommentSearchRepository extends ElasticsearchRepository<CommentIndex, String> {
}
