package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.producer.NotificationProducer;
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
    private EmailNotificationMessage message;

    @BeforeEach
    void setup() {

        event = new OutboxEvent();
        event.setId(1L);
        event.setPayload("Payload");
        event.setStatus(OutboxEventStatus.PENDING);

        message = new EmailNotificationMessage(
                event.getId(),
                "new@gmail.com",
                "Welcome",
                "Welcome to pulse"
        );

    }

    @Nested
    @DisplayName("Publish Email Event")
    class publishEmailEvent{

        @Test
        @DisplayName("Publish Email Event - Success")
        void publishEvent_MarkEmailAsPublished_WhenPublishedSuccessfully() {

            when(objectMapper.readValue(event.getPayload(), EmailNotificationMessage.class))
                    .thenReturn(message);
            doNothing().when(notificationProducer).publishEmail(message);
            doNothing().when(outboxStateService).markPublished(event);

            outboxPublisher.publishEvent(event);

            verify(objectMapper, times(1)).readValue(event.getPayload(),
                    EmailNotificationMessage.class);
            verify(notificationProducer, times(1)).publishEmail(message);
            verify(outboxStateService, times(1)).markPublished(event);
        }

        @Test
        @DisplayName("Publish Email Event - Failure to Publish")
        void publishEvent_RecordPublishFailureAttempt_WhenFailedToPublish() {

            Exception exception = new RuntimeException("RabbitMQ unavailable");

            when(objectMapper.readValue(event.getPayload(), EmailNotificationMessage.class))
                    .thenReturn(message);
            doThrow(exception).when(notificationProducer).publishEmail(message);
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
            verify(notificationProducer, times(1)).publishEmail(message);
        }

    }

}