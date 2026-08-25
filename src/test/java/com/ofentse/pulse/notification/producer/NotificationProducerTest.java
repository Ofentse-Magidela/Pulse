package com.ofentse.pulse.notification.producer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationProducer producer;

    private EmailNotificationMessage message;

    @BeforeEach
    void setup() {
        message = new EmailNotificationMessage(
                1L,
                "new@gmail.com",
                "Welcome",
                "Welcome to pulse."
        );
    }

    @Nested
    @DisplayName("Publish Email to RabbitMQ")
    class publishEmail {

        @Test
        @DisplayName("Publish Email to RabbitMQ - Success")
        void publishEmail_PublishEmailToEmailQueue_whenRabbitIsAvailable() {

            producer.publishEmail(message);

            verify(rabbitTemplate, times(1)).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    message
            );
        }

        @Test
        @DisplayName("Propagates exception when RabbitMQ publish fails")
        void publishEmail_ThrowsException_whenRabbitIsUnavailable() {
            Exception exception = new RuntimeException("RabbitMQ is unavailable");

            doThrow(exception).when(rabbitTemplate).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    message
            );

            RuntimeException thrown = assertThrows(
                    RuntimeException.class,
                    () -> producer.publishEmail(message)
            );

            assertEquals("RabbitMQ is unavailable", thrown.getMessage());

            verify(rabbitTemplate, times(1)).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    message
            );
        }
    }
}