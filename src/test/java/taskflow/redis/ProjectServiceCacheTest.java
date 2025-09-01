package taskflow.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import taskflow.service.ProjectService;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectServiceCacheTest {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void getProjectById_shouldCacheProject() {
        Long id = 1L;
        projectService.findByIdProject(id);

        Long ttl = redisTemplate.getExpire("projects::" + id);
        Assertions.assertTrue(ttl > 0 && ttl <= 3600); // TTL 1 час
    }
}
