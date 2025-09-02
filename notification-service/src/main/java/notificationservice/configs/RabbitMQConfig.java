package notificationservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NOTIFICATION_DIRECT = "taskflow.notification.direct.exchange";
    public static final String EXCHANGE_NOTIFICATION_FANOUT = "taskflow.notification.fanout.exchange";
    public static final String EXCHANGE_NOTIFICATION_TOPIC = "taskflow.notification.topic.exchange";

    public static final String QUEUE_NOTIFICATIONS = "task.notifications";
    public static final String QUEUE_AUDIT_FANOUT = "task.audit.fanout";
    public static final String QUEUE_NOTIFICATIONS_TOPIC = "task.notifications.topic";

    public static final String DLX_EXCHANGE = "taskflow.dlx.exchange";
    public static final String DLX_QUEUE = "task.dlx.notifications";
    public static final String DLX_ROUTING_KEY = "task.notification.dlx";

    // задача 2_1
    // taskflow.direct.exchange связать с task.notifications(Очередь) в Bindings запрос POST /tasks: (запрос)
    @Bean
    public DirectExchange taskNotificationDirectExchange() {
        return new DirectExchange(EXCHANGE_NOTIFICATION_DIRECT);
    }

    @Bean
    public Queue taskNotificationsQueue() {
        return new Queue(QUEUE_NOTIFICATIONS, true);
    }

    @Bean
    public Binding bindTaskNotificationDirect(@Qualifier("taskNotificationsQueue") Queue queue,
                                              @Qualifier("taskNotificationDirectExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(queue).to(directExchange).with("task.notification.created");
    }

    // задача 2_2
    // taskflow.fanout.exchange связать с task.audit.fanout(Очередь)в Bindings PUT /tasks/{id} (запрос)
    @Bean
    public FanoutExchange taskFanoutExchange() {
        return new FanoutExchange(EXCHANGE_NOTIFICATION_FANOUT);
    }

    @Bean
    public Queue taskAuditQueue() {
        return new Queue(QUEUE_AUDIT_FANOUT);
    }

    @Bean
    public Binding bindTaskAuditFanoutQueue(@Qualifier("taskAuditQueue") Queue queue,
                                            @Qualifier("taskFanoutExchange") FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    // задача 2_3
    //taskflow.topic.exchange связь task.notifications.topic (Очередь) в Bindings DELETE /tasks/{id} (запрос)
    @Bean
    public TopicExchange taskTopicExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICATION_TOPIC);
    }

    @Bean
    public Queue notificationTopicQueue() {
        return new Queue(QUEUE_NOTIFICATIONS_TOPIC);
    }

    @Bean
    public Binding bindTaskNotificationsTopic(@Qualifier("notificationTopicQueue") Queue queue,
                                              @Qualifier("taskTopicExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("task.deleted");
    }

    // retry dlx
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLX_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding(@Qualifier("deadLetterQueue") Queue queue,
                                     @Qualifier("deadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DLX_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RabbitTemplate rabbitTemplate
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, DLX_EXCHANGE, DLX_ROUTING_KEY))
                .build());
        return factory;
    }
}
