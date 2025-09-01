package taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taskflow.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
