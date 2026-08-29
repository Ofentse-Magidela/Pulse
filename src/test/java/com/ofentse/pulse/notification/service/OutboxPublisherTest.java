package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.producer.NotificationProducer;
import com.ofentse.pulse.notification.sms.dto.SmsNotificationMessage;
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
    private SmsNotificationMessage smsMessage;
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

        smsMessage = new SmsNotificationMessage(
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
            verify(notificationProducer, never()).publishSms(any());
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
            verify(notificationProducer, never()).publishSms(any());
        }

    }

    @Nested
    @DisplayName("Publish Sms Event")
    class publishSmsEvent{

        @Test
        @DisplayName("PublishSms Event - Success")
        void publishSmsEvent_MarkSmsAsPublished_WhenPublishedSuccessfully() {

            notification.setChannel(NotificationChannel.SMS);

            when(objectMapper.readValue(event.getPayload(), SmsNotificationMessage.class))
                    .thenReturn(smsMessage);
            doNothing().when(notificationProducer).publishSms(smsMessage);
            doNothing().when(outboxStateService).markPublished(event);

            outboxPublisher.publishEvent(event);

            verify(objectMapper, times(1)).readValue(event.getPayload(),
                    SmsNotificationMessage.class);
            verify(notificationProducer, times(1)).publishSms(smsMessage);
            verify(outboxStateService, times(1)).markPublished(event);
            verify(notificationProducer, never()).publishEmail(any());
        }

        @Test
        @DisplayName("Publish Sms Event - Failure to Publish")
        void publishSmsEvent_RecordPublishFailureAttempt_WhenFailedToPublish() {

            notification.setChannel(NotificationChannel.SMS);
            Exception exception = new RuntimeException("RabbitMQ unavailable");

            when(objectMapper.readValue(event.getPayload(), SmsNotificationMessage.class))
                    .thenReturn(smsMessage);
            doThrow(exception).when(notificationProducer).publishSms(smsMessage);
            doNothing().when(outboxStateService).recordFailure(event, exception);

            outboxPublisher.publishEvent(event);

            ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
            verify(outboxStateService, times(1)).recordFailure(
                    eq(event), captor.capture()
            );
            Exception ex = captor.getValue();

            assertEquals("RabbitMQ unavailable", ex.getMessage());

            verify(objectMapper, times(1)).readValue(event.getPayload(),
                    SmsNotificationMessage.class);
            verify(notificationProducer, times(1)).publishSms(smsMessage);
            verify(notificationProducer, never()).publishEmail(any());
        }

    }

}