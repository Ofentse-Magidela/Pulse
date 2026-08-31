package com.ofentse.pulse.notification.whatsapp.consumer;

import com.ofentse.pulse.notification.whatsapp.WhatsAppService;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
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
class WhatsAppNotificationConsumerTest {

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private WhatsAppNotificationConsumer consumer;

    private WhatsAppNotificationMessage message;

    @BeforeEach
    void setup() {
        message = new WhatsAppNotificationMessage(
                1L,
                "1234567890",
                "Welcome to pulse."
        );
    }

    @Nested
    @DisplayName("Consume WhatsApp")
    class ConsumeWhatsApp {

        @Test
        @DisplayName("Consume WhatsApp - Success")
        void consumeWhatsApp_DelegatesMessagesToWhatsAppService() {

            consumer.consumeWhatsApp(message);

            verify(whatsAppService).sendWhatsApp(message);
        }
    }
}