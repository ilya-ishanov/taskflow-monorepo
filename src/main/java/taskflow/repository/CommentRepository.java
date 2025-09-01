package taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taskflow.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
