package notificationservice.repository;

import notificationservice.entity.RedisLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedisLogRepository extends JpaRepository<RedisLog, Long> {
}
