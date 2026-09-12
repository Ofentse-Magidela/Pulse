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
import com.ofentse.pulse.notification.template.NotificationTemplate;
import com.ofentse.pulse.notification.template.NotificationTemplateService;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationDTO;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

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

    @Mock
    private NotificationTemplateService notificationTemplateService;

    @InjectMocks
    private NotificationService notificationService;

    private EmailNotificationDTO emailDTO;
    private WhatsAppNotificationDTO whatsAppDTO;
    private Notification notification;

    @BeforeEach
    void setup() {

        emailDTO = new EmailNotificationDTO(
                "user@gmail.com",
                "TEST_TEMPLATE",
                Map.of("name", "Test User")
        );

        whatsAppDTO = new WhatsAppNotificationDTO(
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

            NotificationTemplate template = new NotificationTemplate();
            template.setName("TEST_TEMPLATE");
            template.setSubject("Test subject");
            template.setBody("Hello {{name}}");

            when(notificationTemplateService.getTemplate("TEST_TEMPLATE")).thenReturn(template);

            when(notificationTemplateService.render(template.getSubject(), emailDTO.getVariables()))
                    .thenReturn("Test subject");

            when(notificationTemplateService.render(template.getBody(), emailDTO.getVariables()))
                    .thenReturn("Hello Test User");

            when(objectMapper.writeValueAsString(any(EmailNotificationMessage.class))).thenReturn("Payload");
            when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
            when(outboxEventRepo.save(any(OutboxEvent.class))).thenReturn(new OutboxEvent());

            notificationService.sendEmailNotification(emailDTO);

            verify(notificationTemplateService).getTemplate("TEST_TEMPLATE");
            verify(notificationTemplateService).render(template.getSubject(), emailDTO.getVariables());
            verify(notificationTemplateService).render(template.getBody(), emailDTO.getVariables());

            ArgumentCaptor<Notification> captor1 = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(captor1.capture());
            Notification savedNotification = captor1.getValue();

            assertEquals("user@gmail.com", savedNotification.getRecipient());
            assertEquals("Test subject", savedNotification.getSubject());
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
    @DisplayName("SendWhatsAppNotification")
    class SendWhatsAppNotification {

        @Test
        @DisplayName("SendWhatsAppNotification - Success")
        void sendWhatsAppNotification_PublishEventAndSavesOutbox_WhenDTOIsValid() {

            notification.setRecipient(whatsAppDTO.getTo());

            when(objectMapper.writeValueAsString(any(WhatsAppNotificationMessage.class))).thenReturn("Payload");
            when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
            when(outboxEventRepo.save(any(OutboxEvent.class))).thenReturn(new OutboxEvent());

            notificationService.sendWhatsAppNotification(whatsAppDTO);

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