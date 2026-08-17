package com.ofentse.pulse.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "pulse.notifications";
    public static final String EMAIL_QUEUE = "pulse.email";
    public static final String EMAIL_ROUTING_KEY =  "email";

    public static final String DLX_EXCHANGE = "pulse.notifications.dlx";
    public static final String EMAIL_DLQ = "pulse.email.dlq";
    public static final String EMAIL_DLQ_ROUTING_KEY =  "email";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable(EMAIL_QUEUE)
                .classic()
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(EMAIL_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange notificationExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(notificationExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return new Queue(EMAIL_DLQ, true);
    }

    @Bean
    public Binding emailDeadLetterBinding(Queue emailDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder
                .bind(emailDeadLetterQueue)
                .to(deadLetterExchange)
                .with(EMAIL_DLQ_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
