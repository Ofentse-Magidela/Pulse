package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.event.OutboxEventCreated;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepo notificationRepo;

    @Mock
    private OutboxEventRepo outboxEventRepo;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private NotificationService notificationService;
    @Mock
    private ObjectMapper objectMapper;

    private EmailNotificationDTO dto;
    private Notification notification;
    private OutboxEvent outbox;

    @BeforeEach
    void setup() {

        dto = new EmailNotificationDTO(
                "user@gmail.com",
                "Welcome",
                "Hello from Pulse"
        );

        notification = new Notification();
        notification.setId(1L);
        notification.setRecipient(dto.getTo());
        notification.setSubject(dto.getSubject());
        notification.setStatus(NotificationStatus.PENDING);

        outbox = new OutboxEvent();
        outbox.setNotification(notification);
        outbox.setStatus(OutboxEventStatus.PENDING);
    }

    @Nested
    @DisplayName("SendEmailNotification")
    class SendEmailNotification {

        @Test
        @DisplayName("SendEmailNotification - Success")
        void sendEmailNotification_PublishEventAndSavesOutbox_WhenDTOIsValid() {

            when(objectMapper.writeValueAsString(any(EmailNotificationMessage.class))).thenReturn("Payload");
            when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
            when(outboxEventRepo.save(any(OutboxEvent.class))).thenReturn(outbox);

            notificationService.sendEmailNotification(dto);

            ArgumentCaptor<Notification> captor1 = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(captor1.capture());
            Notification savedNotification = captor1.getValue();

            assertNotNull(savedNotification);
            assertEquals("user@gmail.com", savedNotification.getRecipient());
            assertEquals("Welcome", savedNotification.getSubject());
            assertEquals(NotificationStatus.PENDING, savedNotification.getStatus());

            ArgumentCaptor<OutboxEvent> captor2 = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo).save(captor2.capture());
            OutboxEvent outboxEvent= captor2.getValue();

            assertNotNull(outboxEvent);
            assertEquals(OutboxEventStatus.PENDING, outboxEvent.getStatus());
            assertEquals("Payload", outboxEvent.getPayload());

            verify(applicationEventPublisher).publishEvent(any(OutboxEventCreated.class));
        }
    }

}