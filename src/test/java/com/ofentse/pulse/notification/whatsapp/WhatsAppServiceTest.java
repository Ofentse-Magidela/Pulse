package com.ofentse.pulse.notification.whatsapp;

import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationChannel;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppServiceTest {

    @Mock
    private WhatsAppApiClient whatsAppApiClient;

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private WhatsAppService whatsAppService;

    private WhatsAppNotificationMessage message;
    private Notification notification;

    @BeforeEach
    void setup() {

        message = new WhatsAppNotificationMessage(
                1L,
                "27876543210",
                "Welcome to pulse."
        );

        notification = new Notification();
        notification.setId(1L);
        notification.setChannel(NotificationChannel.WHATSAPP);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRecipient(message.getTo());
        notification.setCreatedAt(LocalDateTime.now().minusMinutes(1));
    }

    @Nested
    @DisplayName("Send WhatsApp")
    class SendWhatsApp {

        @Test
        @DisplayName("SendWhatsApp Message - Success")
        void sendWhatsApp_MessageHandedToClient_WhenRequestIsSuccessful() {

            when(notificationRepo.findById(message.getNotificationId())).thenReturn(Optional.of(notification));

            whatsAppService.sendWhatsApp(message);

            verify(notificationRepo, times(1)).findById(message.getNotificationId());
            verify(whatsAppApiClient, times(1)).sendMessage(any(WhatsAppNotificationMessage.class));

            ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(notificationCaptor.capture());
            Notification savedNotification = notificationCaptor.getValue();

            assertEquals(NotificationStatus.SENT, savedNotification.getStatus());
            assertTrue(notification.getCreatedAt().isBefore(savedNotification.getSentAt()));
        }

        @Test
        @DisplayName("SendWhatsApp - NotificationNotFoundException")
        void sendWhatsApp_ThrowsNotificationNotFoundException() {

            NotificationNotFoundException exception = assertThrows(
                    NotificationNotFoundException.class,
                    () -> whatsAppService.sendWhatsApp(message)
            );

            assertEquals("notification", exception.getField());
            assertEquals("Notification with ID: 1 not found.", exception.getMessage());

            verifyNoInteractions(whatsAppApiClient);
            verify(notificationRepo, times(1)).findById(message.getNotificationId());
            verify(notificationRepo, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("SendWhatsApp - Return when whatsapp is already sent")
        void sendWhatsApp_ReturnsWithoutSending_WhenNotificationIsAlreadySent() {

            notification.setStatus(NotificationStatus.SENT);

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            whatsAppService.sendWhatsApp(message);

            verify(notificationRepo, times(1)).findById(message.getNotificationId());
            verify(whatsAppApiClient, never()).sendMessage(any(WhatsAppNotificationMessage.class));
            verify(notificationRepo, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("SendWhatsApp - Exception WhatsAppApiClient Failure")
        void SendWhatsApp_PropagatesException_WhenWhatsAppApiClientFails() {

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            Exception exception = new RuntimeException("WhatsApp Api Client unavailable");

            doThrow(exception).when(whatsAppApiClient).sendMessage(any(WhatsAppNotificationMessage.class));

            Exception thrown = assertThrows(
                    RuntimeException.class,
                    () -> whatsAppService.sendWhatsApp(message)
            );

            assertEquals("WhatsApp Api Client unavailable", thrown.getMessage());
            assertEquals(NotificationStatus.PENDING, notification.getStatus());

            verify(notificationRepo, times(1)).findById(message.getNotificationId());
            verify(whatsAppApiClient, times(1)).sendMessage(any(WhatsAppNotificationMessage.class));
            verify(notificationRepo, never()).save(any(Notification.class));
        }
    }
}