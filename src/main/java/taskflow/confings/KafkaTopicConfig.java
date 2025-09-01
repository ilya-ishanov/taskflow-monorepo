package taskflow.confings;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of("bootstrap.servers", "localhost:9092"));
    }

    @Bean
    public NewTopic logsTopic() {
        return TopicBuilder.name("taskflow.logs")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder.name("taskflow.events")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic errorTopic() {
        return TopicBuilder.name("taskflow.dlq")
                .partitions(1)
                .replicas(1)
                .build();
    }

//    @Bean
//    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> factory) {
//        KafkaTemplate<String, Object> template = new KafkaTemplate<>(factory);
//        template.setMessageConverter(new StringJsonMessageConverter());
//        return template;
//    }
}
