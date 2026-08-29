package com.ofentse.pulse.notification.sms.consumer;

import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsDeadLetterConsumerTest {

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private SmsDeadLetterConsumer deadLetterConsumer;

    private SmsNotificationMessage message;
    private Notification notification;

    @BeforeEach
    void setup() {
        message = new SmsNotificationMessage(
                1L,
                "12345678890",
                "Welcome to pulse."
        );

        notification = new Notification();
        notification.setId(1L);
        notification.setStatus(NotificationStatus.PENDING);
    }

    @Nested
    @DisplayName("ConsumeFailedSms")
    class ConsumeDeadLetterSms {

        @Test
        @DisplayName("ConsumeFailedSms - Success")
        void consumeDeadLetterSms_SavesMessageAsFailed() {

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            deadLetterConsumer.consumeDeadLetterSms(message);

            ArgumentCaptor<Notification> smsCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(smsCaptor.capture());
            Notification capturedNotification = smsCaptor.getValue();

            assertEquals(NotificationStatus.FAILED, capturedNotification.getStatus());
            assertEquals(1L, capturedNotification.getId());

            verify(notificationRepo).findById(message.getNotificationId());
        }

        @Test
        @DisplayName("ConsumeFailedSms - Throws Exception When Notification Not Found")
        void ConsumeDeadLetterSms_ThrowsNotificationNotFoundException_WhenNotificationIsNotFound() {
            NotificationNotFoundException exception = assertThrows(
                    NotificationNotFoundException.class,
                    () ->  deadLetterConsumer
                            .consumeDeadLetterSms(message)
            );

            assertNotNull(exception);
            assertEquals("notification", exception.getField());
            assertEquals("Notification with ID: 1 not found.", exception.getMessage());

            verify(notificationRepo, never()).save(notification);
        }

        @Test
        @DisplayName("ConsumeFailedSms - Return when sms has already Failed")
        void ConsumeDeadLetterSms_ReturnsWithoutSaving_WhenNotificationHasAlreadyFailed() {

            notification.setStatus(NotificationStatus.FAILED);
            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            deadLetterConsumer.consumeDeadLetterSms(message);

            verify(notificationRepo).findById(notification.getId());
            verify(notificationRepo, never()).save(notification);
        }
    }

}