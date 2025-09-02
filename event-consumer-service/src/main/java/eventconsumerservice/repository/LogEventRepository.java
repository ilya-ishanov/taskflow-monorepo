package eventconsumerservice.repository;

import eventconsumerservice.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEventRepository extends JpaRepository<EventLog, Long> {
}
