package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.producer.NotificationProducer;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxStateService outboxStateService;

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    private OutboxEvent event;
    private EmailNotificationMessage emailMessage;
    private WhatsAppNotificationMessage whatsAppMessage;
    private Notification notification;

    @BeforeEach
    void setup() {

        notification = new Notification();

        event = new OutboxEvent();
        event.setId(1L);
        event.setPayload("Payload");
        event.setStatus(OutboxEventStatus.PENDING);
        event.setNotification(notification);

        emailMessage = new EmailNotificationMessage(
                event.getId(),
                "new@gmail.com",
                "Welcome",
                "Welcome to pulse"
        );

        whatsAppMessage = new WhatsAppNotificationMessage(
                event.getId(),
                "12345678900",
                "Welcome to pulse."
        );
    }

    @Nested
    @DisplayName("Publish Email Event")
    class publishEmailEvent{

        @Test
        @DisplayName("Publish Email Event - Success")
        void publishEvent_MarkEmailAsPublished_WhenPublishedSuccessfully() {

            notification.setChannel(NotificationChannel.EMAIL);

            when(objectMapper.readValue(event.getPayload(), EmailNotificationMessage.class))
                    .thenReturn(emailMessage);
            doNothing().when(notificationProducer).publishEmail(emailMessage);
            doNothing().when(outboxStateService).markPublished(event);

            outboxPublisher.publishEvent(event);

            verify(objectMapper, times(1)).readValue(event.getPayload(),
                    EmailNotificationMessage.class);
            verify(notificationProducer, times(1)).publishEmail(emailMessage);
            verify(outboxStateService, times(1)).markPublished(event);
            verify(notificationProducer, never()).publishWhatsApp(any());
        }

        @Test
        @DisplayName("Publish Email Event - Failure to Publish")
        void publishEvent_RecordPublishFailureAttempt_WhenFailedToPublish() {
            notification.setChannel(NotificationChannel.EMAIL);
            Exception exception = new RuntimeException("RabbitMQ unavailable");

            when(objectMapper.readValue(event.getPayload(), EmailNotificationMessage.class))
                    .thenReturn(emailMessage);
            doThrow(exception).when(notificationProducer).publishEmail(emailMessage);
            doNothing().when(outboxStateService).recordFailure(event, exception);


            outboxPublisher.publishEvent(event);

            ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
            verify(outboxStateService, times(1)).recordFailure(
                    eq(event), captor.capture()
            );
            Exception ex = captor.getValue();

            assertEquals("RabbitMQ unavailable", ex.getMessage());

            verify(objectMapper, times(1)).readValue(event.getPayload(),
                    EmailNotificationMessage.class);
            verify(notificationProducer, times(1)).publishEmail(emailMessage);
            verify(notificationProducer, never()).publishWhatsApp(any());
        }

    }

    @Nested
    @DisplayName("Publish WhatsApp Event")
    class publishWhatsAppEvent{

        @Test
        @DisplayName("PublishWhatsApp Event - Success")
        void publishWhatsAppEvent_MarkWhatsAppAsPublished_WhenPublishedSuccessfully() {

            notification.setChannel(NotificationChannel.WHATSAPP);

            when(objectMapper.readValue(event.getPayload(), WhatsAppNotificationMessage.class))
                    .thenReturn(whatsAppMessage);
            doNothing().when(notificationProducer).publishWhatsApp(whatsAppMessage);
            doNothing().when(outboxStateService).markPublished(event);

            outboxPublisher.publishEvent(event);

            verify(objectMapper, times(1)).readValue(event.getPayload(),
                    WhatsAppNotificationMessage.class);
            verify(notificationProducer, times(1)).publishWhatsApp(whatsAppMessage);
            verify(outboxStateService, times(1)).markPublished(event);
            verify(notificationProducer, never()).publishEmail(any());
        }

        @Test
        @DisplayName("Publish WhatsApp Event - Failure to Publish")
        void publishWhatsAppEvent_RecordPublishFailureAttempt_WhenFailedToPublish() {

            notification.setChannel(NotificationChannel.WHATSAPP);
            Exception exception = new RuntimeException("RabbitMQ unavailable");

            when(objectMapper.readValue(event.getPayload(), WhatsAppNotificationMessage.class))
                    .thenReturn(whatsAppMessage);
            doThrow(exception).when(notificationProducer).publishWhatsApp(whatsAppMessage);
            doNothing().when(outboxStateService).recordFailure(event, exception);

            outboxPublisher.publishEvent(event);

            ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
            verify(outboxStateService, times(1)).recordFailure(
                    eq(event), captor.capture()
            );
            Exception ex = captor.getValue();

            assertEquals("RabbitMQ unavailable", ex.getMessage());

            verify(objectMapper, times(1)).readValue(event.getPayload(),
                    WhatsAppNotificationMessage.class);
            verify(notificationProducer, times(1)).publishWhatsApp(whatsAppMessage);
            verify(notificationProducer, never()).publishEmail(any());
        }

    }

}