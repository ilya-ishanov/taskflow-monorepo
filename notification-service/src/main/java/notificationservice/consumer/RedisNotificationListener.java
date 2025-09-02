package notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import notificationservice.entity.RedisLog;
import notificationservice.repository.RedisLogRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisNotificationListener implements MessageListener {
    private final RedisLogRepository redisLogrepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String notification = new String (message.getBody());
            System.out.println("Redis получил нотификацию " + notification);

            RedisLog redisLog = new RedisLog();
            redisLog.setNotification(notification);
            redisLogrepository.save(redisLog);

        } catch (Exception e) {
            System.err.println("Ошибка при разборе Redis-сообщения: " + e.getMessage());
        }
    }
}
