package com.ofentse.pulse.notification.sms.consumer;

import com.ofentse.pulse.notification.sms.SmsService;
import com.ofentse.pulse.notification.sms.dto.SmsNotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsNotificationConsumerTest {

    @Mock
    private SmsService smsService;

    @InjectMocks
    private SmsNotificationConsumer consumer;

    private SmsNotificationMessage message;

    @BeforeEach
    void setup() {
        message = new SmsNotificationMessage(
                1L,
                "1234567890",
                "Welcome to pulse."
        );
    }

    @Nested
    @DisplayName("Consume Sms")
    class ConsumeSms {

        @Test
        @DisplayName("Consume Sms - Success")
        void consumeSms_DelegatesMessagesToSmsService() {

            consumer.consumeSms(message);

            verify(smsService).sendSms(message);
        }
    }
}