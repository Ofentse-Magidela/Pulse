package com.ofentse.pulse.notification.whatsapp.consumer;

import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppDeadLetterConsumerTest {

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private WhatsAppDeadLetterConsumer deadLetterConsumer;

    private WhatsAppNotificationMessage message;
    private Notification notification;

    @BeforeEach
    void setup() {
        message = new WhatsAppNotificationMessage(
                1L,
                "12345678890",
                "Welcome to pulse."
        );

        notification = new Notification();
        notification.setId(1L);
        notification.setStatus(NotificationStatus.PENDING);
    }

    @Nested
    @DisplayName("ConsumeFailedWhatsApp")
    class ConsumeDeadLetterWhatsApp {

        @Test
        @DisplayName("ConsumeFailedWhatsapp - Success")
        void consumeDeadLetterWhatsapp_SavesMessageAsFailed() {

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            deadLetterConsumer.consumeDeadLetterWhatsApp(message);

            ArgumentCaptor<Notification> whatsAppCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(whatsAppCaptor.capture());
            Notification capturedNotification = whatsAppCaptor.getValue();

            assertEquals(NotificationStatus.FAILED, capturedNotification.getStatus());
            assertEquals(1L, capturedNotification.getId());

            verify(notificationRepo).findById(message.getNotificationId());
        }

        @Test
        @DisplayName("ConsumeFailedWhatsapp - Throws Exception When Notification Not Found")
        void ConsumeDeadLetterWhatsapp_ThrowsNotificationNotFoundException_WhenNotificationIsNotFound() {
            NotificationNotFoundException exception = assertThrows(
                    NotificationNotFoundException.class,
                    () ->  deadLetterConsumer
                            .consumeDeadLetterWhatsApp(message)
            );

            assertNotNull(exception);
            assertEquals("notification", exception.getField());
            assertEquals("Notification with ID: 1 not found.", exception.getMessage());

            verify(notificationRepo, never()).save(notification);
        }

        @Test
        @DisplayName("ConsumeFailedWhatsapp - Return when whatsapp has already Failed")
        void ConsumeDeadLetterWhatsapp_ReturnsWithoutSaving_WhenNotificationHasAlreadyFailed() {

            notification.setStatus(NotificationStatus.FAILED);
            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            deadLetterConsumer.consumeDeadLetterWhatsApp(message);

            verify(notificationRepo).findById(notification.getId());
            verify(notificationRepo, never()).save(notification);
        }
    }

}