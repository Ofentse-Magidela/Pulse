package com.ofentse.pulse.notification.producer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
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

    private EmailNotificationMessage emailMessage;
    private WhatsAppNotificationMessage whatsAppMessage;

    @BeforeEach
    void setup() {
        emailMessage = new EmailNotificationMessage(
                1L,
                "new@gmail.com",
                "Welcome",
                "Welcome to pulse."
        );

        whatsAppMessage = new WhatsAppNotificationMessage(
                2L,
                "1234567890",
                "Welcome to pulse."
        );
    }

    @Nested
    @DisplayName("Publish Email to RabbitMQ")
    class publishEmail {

        @Test
        @DisplayName("Publish Email to RabbitMQ - Success")
        void publishEmail_PublishEmailToEmailQueue_whenRabbitIsAvailable() {

            producer.publishEmail(emailMessage);

            verify(rabbitTemplate, times(1)).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailMessage
            );
        }

        @Test
        @DisplayName("Propagates exception when RabbitMQ publish fails")
        void publishEmail_ThrowsException_whenRabbitIsUnavailable() {
            Exception exception = new RuntimeException("RabbitMQ is unavailable");

            doThrow(exception).when(rabbitTemplate).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailMessage
            );

            RuntimeException thrown = assertThrows(
                    RuntimeException.class,
                    () -> producer.publishEmail(emailMessage)
            );

            assertEquals("RabbitMQ is unavailable", thrown.getMessage());

            verify(rabbitTemplate, times(1)).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailMessage
            );
        }
    }

    @Nested
    @DisplayName("Publish WhatsApp to RabbitMQ")
    class publishWhatsApp {

        @Test
        @DisplayName("Publish WhatsApp to RabbitMQ - Success")
        void publishWhatsApp_PublishWhatsAppToWhatsAppQueue_whenRabbitIsAvailable() {

            producer.publishWhatsApp(whatsAppMessage);

            verify(rabbitTemplate, times(1)).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    whatsAppMessage
            );
        }

        @Test
        @DisplayName("Propagates exception when RabbitMQ publish fails")
        void publishWhatsApp_ThrowsException_whenRabbitIsUnavailable() {
            Exception exception = new RuntimeException("RabbitMQ is unavailable");

            doThrow(exception).when(rabbitTemplate).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.WHATSAPP_ROUTING_KEY,
                    whatsAppMessage
            );

            RuntimeException thrown = assertThrows(
                    RuntimeException.class,
                    () -> producer.publishWhatsApp(whatsAppMessage)
            );

            assertEquals("RabbitMQ is unavailable", thrown.getMessage());

            verify(rabbitTemplate, times(1)).convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.WHATSAPP_ROUTING_KEY,
                    whatsAppMessage
            );
        }
    }

}