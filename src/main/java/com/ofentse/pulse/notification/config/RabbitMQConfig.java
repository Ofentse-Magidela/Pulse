package com.ofentse.pulse.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "pulse.notifications";
    public static final String DLX_EXCHANGE = "pulse.notifications.dlx";

    public static final String EMAIL_QUEUE = "pulse.email";
    public static final String EMAIL_ROUTING_KEY =  "email";

    public static final String EMAIL_DLQ = "pulse.email.dlq";
    public static final String EMAIL_DLQ_ROUTING_KEY =  "email";

    public static final String SMS_QUEUE = "pulse.sms";
    public static final String SMS_DLQ = "pulse.sms.dlq";

    public static final String SMS_ROUTING_KEY = "sms";
    public static final String SMS_DLQ_ROUTING_KEY = "sms";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }


    // Email Beans

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
    public Binding emailBinding(@Qualifier("emailQueue")Queue emailQueue, DirectExchange notificationExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(notificationExchange)
                .with(EMAIL_ROUTING_KEY);
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

    //SMS Beans

    @Bean
    public Queue smsQueue() {
        return QueueBuilder
                .durable(SMS_QUEUE)
                .classic()
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(SMS_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding smsBinding(@Qualifier("smsQueue") Queue smsQueue, DirectExchange notificationExchange) {
        return BindingBuilder
                .bind(smsQueue)
                .to(notificationExchange)
                .with(SMS_ROUTING_KEY);
    }

    @Bean
    public Queue smsDeadLetterQueue() {
        return new Queue(SMS_DLQ, true);
    }

    @Bean
    public Binding smsDeadLetterBinding(Queue smsDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder
                .bind(smsDeadLetterQueue)
                .to(deadLetterExchange)
                .with(SMS_DLQ_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
