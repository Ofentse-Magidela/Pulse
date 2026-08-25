package com.ofentse.pulse.notification.email.consumer;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailDeadLetterConsumerTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private EmailDeadLetterConsumer deadLetterConsumer;

    private EmailNotificationMessage message;
    private Notification notification;

    @BeforeEach
    void setup() {
         message = new EmailNotificationMessage(
                1L,
                "new@gmail.com",
                "Welcome",
                "Welcome to pulse."
        );

        notification = new Notification();
        notification.setId(1L);
        notification.setStatus(NotificationStatus.PENDING);
    }

    @Nested
    @DisplayName("ConsumeFailedEmails")
    class ConsumeDeadLetterEmails {

        @Test
        @DisplayName("ConsumeFailedEmails - Success")
        void consumeDeadLetterEmails_SavesMessageAsFailed() {

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            deadLetterConsumer.consumeDeadLetterEmails(message);

            ArgumentCaptor<Notification> emailCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(emailCaptor.capture());
            Notification capturedNotification = emailCaptor.getValue();

            assertEquals(NotificationStatus.FAILED, capturedNotification.getStatus());
            assertEquals(1L, capturedNotification.getId());

            verify(notificationRepo, times(1)).findById(message.getNotificationId());
        }

        @Test
        @DisplayName("ConsumeFailedEmails - Throws Exception When Notification Not Found")
        void ConsumeDeadLetterEmails_ThrowsNotificationNotFoundException_WhenNotificationIsNotFound() {
            NotificationNotFoundException exception = assertThrows(
                    NotificationNotFoundException.class,
                    () ->  deadLetterConsumer
                            .consumeDeadLetterEmails(message)
            );

            assertNotNull(exception);
            assertEquals("notification", exception.getField());
            assertEquals("Notification with ID: 1 not found.", exception.getMessage());

            verify(notificationRepo, never()).save(notification);
        }

        @Test
        @DisplayName("ConsumeFailedEmails - Return when email has already Failed")
        void ConsumeDeadLetterEmails_ReturnsWithoutSaving_WhenNotificationHasAlreadyFailed() {

            notification.setStatus(NotificationStatus.FAILED);
            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            deadLetterConsumer.consumeDeadLetterEmails(message);

            verify(notificationRepo, times(1)).findById(notification.getId());
            verify(notificationRepo, never()).save(notification);
        }
    }

}