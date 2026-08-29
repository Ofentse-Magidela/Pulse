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
import com.ofentse.pulse.notification.sms.dto.SmsNotificationDTO;
import com.ofentse.pulse.notification.sms.dto.SmsNotificationMessage;
import org.junit.jupiter.api.*;
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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationService notificationService;

    private EmailNotificationDTO emailDTO;
    private SmsNotificationDTO smsDTO;
    private Notification notification;

    @BeforeEach
    void setup() {

        emailDTO = new EmailNotificationDTO(
                "user@gmail.com",
                "Welcome",
                "Hello from Pulse"
        );

        smsDTO = new SmsNotificationDTO(
                "1234567890",
                "Welcome to pulse."
        );

        notification = new Notification();
        notification.setId(1L);
    }

    @Nested
    @DisplayName("SendEmailNotification")
    class SendEmailNotification {

        @Test
        @DisplayName("SendEmailNotification - Success")
        void sendEmailNotification_PublishEventAndSavesOutbox_WhenDTOIsValid() {

            when(objectMapper.writeValueAsString(any(EmailNotificationMessage.class))).thenReturn("Payload");
            when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
            when(outboxEventRepo.save(any(OutboxEvent.class))).thenReturn(new OutboxEvent());

            notificationService.sendEmailNotification(emailDTO);

            ArgumentCaptor<Notification> captor1 = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(captor1.capture());
            Notification savedNotification = captor1.getValue();

            assertEquals("user@gmail.com", savedNotification.getRecipient());
            assertEquals("Welcome", savedNotification.getSubject());
            assertEquals(NotificationStatus.PENDING, savedNotification.getStatus());
            assertNotNull(savedNotification.getCreatedAt());

            ArgumentCaptor<OutboxEvent> captor2 = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo).save(captor2.capture());
            OutboxEvent outboxEvent= captor2.getValue();

            assertEquals(OutboxEventStatus.PENDING, outboxEvent.getStatus());
            assertEquals(0, outboxEvent.getRetryCount());
            assertNotNull(outboxEvent.getCreatedAt());
            assertNotNull(outboxEvent.getNextRetryAt());

            verify(applicationEventPublisher).publishEvent(any(OutboxEventCreated.class));
        }
    }

    @Nested
    @DisplayName("SendSmsNotification")
    class SendSmsNotification {

        @Test
        @DisplayName("SendSmsNotification - Success")
        void sendSmsNotification_PublishEventAndSavesOutbox_WhenDTOIsValid() {

            notification.setRecipient(smsDTO.getTo());

            when(objectMapper.writeValueAsString(any(SmsNotificationMessage.class))).thenReturn("Payload");
            when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
            when(outboxEventRepo.save(any(OutboxEvent.class))).thenReturn(new OutboxEvent());

            notificationService.sendSmsNotification(smsDTO);

            ArgumentCaptor<Notification> captor1 = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(captor1.capture());
            Notification savedNotification = captor1.getValue();

            assertEquals("1234567890", savedNotification.getRecipient());
            assertEquals(NotificationStatus.PENDING, savedNotification.getStatus());
            assertNull(savedNotification.getSubject());
            assertNotNull(savedNotification.getCreatedAt());

            ArgumentCaptor<OutboxEvent> captor2 = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo).save(captor2.capture());
            OutboxEvent outboxEvent= captor2.getValue();

            assertEquals(OutboxEventStatus.PENDING, outboxEvent.getStatus());
            assertEquals(0, outboxEvent.getRetryCount());
            assertNotNull(outboxEvent.getCreatedAt());
            assertNotNull(outboxEvent.getNextRetryAt());

            verify(applicationEventPublisher).publishEvent(any(OutboxEventCreated.class));
        }
    }
}