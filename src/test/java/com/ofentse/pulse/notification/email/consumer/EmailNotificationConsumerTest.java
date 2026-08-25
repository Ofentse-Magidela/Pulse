package com.ofentse.pulse.notification.email.consumer;

import com.ofentse.pulse.notification.email.EmailService;
import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailNotificationConsumer consumer;

    private EmailNotificationMessage message;

    @BeforeEach
    void setup() {
        message = new EmailNotificationMessage(
                1L,
                "new@gmail.com",
                "Welcome",
                "Welcome to pulse."
        );
    }

    @Nested
    @DisplayName("Consume Emails")
    class ConsumeEmails {

        @Test
        @DisplayName("Consume Emails - Success")
        void consumeEmails_DelegatesMessagesToEmailService() {

            consumer.consumeEmails(message);

            verify(emailService, times(1)).sendEmail(message);
        }
    }

}