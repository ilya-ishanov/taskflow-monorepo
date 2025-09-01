package taskflow.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import taskflow.service.UserService;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceCacheTest {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void getUserById_shouldCacheWithCorrectTTL() {
        Long id = 1L;
        userService.findById(id);

        Long ttl = redisTemplate.getExpire("users::" + id);
        Assertions.assertTrue(ttl > 0 && ttl <= 86400); // TTL 24ч
    }
}
