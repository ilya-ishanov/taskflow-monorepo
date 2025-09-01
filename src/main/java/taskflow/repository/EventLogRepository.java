package taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taskflow.entity.EventLog;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
}
