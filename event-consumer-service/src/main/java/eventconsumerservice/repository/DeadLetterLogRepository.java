package eventconsumerservice.repository;

import eventconsumerservice.entity.DeadLetterLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterLogRepository extends JpaRepository<DeadLetterLog, Long> {
}
