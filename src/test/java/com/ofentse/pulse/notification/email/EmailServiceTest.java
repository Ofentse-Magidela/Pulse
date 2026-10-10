package com.ofentse.pulse.notification.email;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private EmailService emailService;

    private EmailNotificationMessage message;
    private Notification notification;

    @BeforeEach
    void setup() {

        ReflectionTestUtils.setField(
                emailService,
                "mailUsername",
                "pulse@api.com"
        );
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
    @DisplayName("SendEmail")
    class SendEmail {

        @Test
        @DisplayName("Send Email - Success")
        void sendEmail_DelegateEmailToJavaMailSender() throws MessagingException {

            MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            LocalDateTime before = LocalDateTime.now();

            emailService.sendEmail(message);

            LocalDateTime after = LocalDateTime.now();

            ArgumentCaptor<MimeMessage> emailCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(emailCaptor.capture());
            MimeMessage capturedEmail = emailCaptor.getValue();

            assertEquals("pulse@api.com", capturedEmail.getFrom()[0].toString());
            assertEquals("new@gmail.com", capturedEmail.getAllRecipients()[0].toString());
            assertEquals("Welcome", capturedEmail.getSubject());

            ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepo).save(notificationCaptor.capture());
            Notification capturedNotification = notificationCaptor.getValue();

            assertEquals(1L, capturedNotification.getId());
            assertEquals(NotificationStatus.SENT, capturedNotification.getStatus());
            assertFalse(capturedNotification.getSentAt().isBefore(before));
            assertFalse(after.isBefore(capturedNotification.getSentAt()));

            verify(notificationRepo, times(1)).findById(notification.getId());
        }

        @Test
        @DisplayName("Send Email - Exception Notification Not Found")
        void sendEmail_ThrowsNotificationNotFoundException_WhenNotificationIsNotFound() {

            NotificationNotFoundException exception = assertThrows(
                    NotificationNotFoundException.class,
                    () ->  emailService.sendEmail(message)
            );

            assertNotNull(exception);

            assertEquals("notification", exception.getField());
            assertEquals("Notification with ID: 1 not found.", exception.getMessage());

            verifyNoInteractions(mailSender);
            verify(notificationRepo, times(1)).findById(message.getNotificationId());
            verify(notificationRepo, never()).save(any(Notification.class));

        }

        @Test
        @DisplayName("Send Email - Exception Mail Sender Failure")
        void sendEmail_PropagatesException_WhenMailSenderFails() {

            MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            doThrow(new MailSendException("SMTP server unavailable"))
                    .when(mailSender).send(any(MimeMessage.class));

            Exception thrown = assertThrows(
                    MailSendException.class,
                    () -> emailService.sendEmail(message)
            );

            assertEquals("SMTP server unavailable", thrown.getMessage());
            assertEquals(NotificationStatus.PENDING, notification.getStatus());

            verify(notificationRepo, times(1)).findById(message.getNotificationId());
            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(notificationRepo, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("Send Email - Return when email is already sent")
        void sendEmail_ReturnsWithoutSending_WhenNotificationIsAlreadySent() {

            notification.setStatus(NotificationStatus.SENT);

            when(notificationRepo.findById(message.getNotificationId()))
                    .thenReturn(Optional.of(notification));

            emailService.sendEmail(message);

            verify(notificationRepo, times(1)).findById(message.getNotificationId());
            verify(mailSender, never()).send(any(SimpleMailMessage.class));
            verify(notificationRepo, never()).save(any(Notification.class));
        }
    }
}