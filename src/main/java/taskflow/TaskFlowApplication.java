package taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "taskflow.repository") // JPA
@EnableMongoRepositories(basePackages = "taskflow.mongo.repository") // Mongo
@EnableCaching // Redis
public class TaskFlowApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskFlowApplication.class, args);
	}
}
