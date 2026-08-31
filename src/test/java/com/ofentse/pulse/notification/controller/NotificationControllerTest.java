package com.ofentse.pulse.notification.controller;

import com.ofentse.pulse.notification.email.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.service.NotificationService;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationDTO;
import com.ofentse.pulse.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtService jwtService;

    private EmailNotificationDTO emailDTO;
    private WhatsAppNotificationDTO whatsAppDTO;

    @BeforeEach
    void setup() {
        emailDTO = new EmailNotificationDTO(
                "user@gmail.com",
                "Welcome",
                "Hello from Pulse"
        );

        whatsAppDTO = new WhatsAppNotificationDTO(
                "1234567890",
                "Welcome to Pulse"
        );
    }

    @Nested
    @DisplayName("POST /notifications/email")
    class SendEmailNotification {

        @Test
        @DisplayName("Returns 202 Accepted when emailDTO is valid")
        void sendEmailNotification_Returns202Accepted_whenDTOIsValid() throws Exception {

            doNothing().when(notificationService).sendEmailNotification(any(EmailNotificationDTO.class));

            mockMvc.perform(post("/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailDTO)))
                    .andExpect(status().isAccepted());

            verify(notificationService).sendEmailNotification(any(EmailNotificationDTO.class));
        }

        @Test
        @DisplayName("Returns 400 Bad Request when subject is blank")
        void sendEmailNotification_Returns400BadRequest_whenSubjectIsBlank() throws Exception {
            emailDTO.setSubject("  ");

            mockMvc.perform(post("/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }
    }

    @Nested
    @DisplayName("POST /notifications/whatsapp")
    class sendWhatsAppNotification {

        @Test
        @DisplayName("Returns 202 Accepted when whatsAppDTO is valid")
        void sendWhatsAppNotification_Returns202Accepted_whenDTOIsValid() throws Exception {

            doNothing().when(notificationService).sendWhatsAppNotification((any(WhatsAppNotificationDTO.class)));

            mockMvc.perform(post("/notifications/whatsapp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(whatsAppDTO)))
                    .andExpect(status().isAccepted());

            verify(notificationService).sendWhatsAppNotification(any(WhatsAppNotificationDTO.class));
        }

        @Test
        @DisplayName("Returns 400 Bad Request when 'to' is blank")
        void sendWhatsAppNotification_Returns400BadRequest_whenToIsBlank() throws Exception{
            whatsAppDTO.setTo("  ");

            mockMvc.perform(post("/notifications/whatsapp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(whatsAppDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("Returns 400 Bad Request when 'contents' is blank")
        void sendWhatsAppNotification_Returns400BadRequest_whenContentIsBlank() throws Exception{
            whatsAppDTO.setContent("  ");

            mockMvc.perform(post("/notifications/whatsapp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(whatsAppDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("Returns 400 Bad Request when content exceeds maximum length")
        void sendWhatsAppNotification_Returns400BadRequest_whenContentIsTooLong() throws Exception {
            whatsAppDTO.setContent("a".repeat(10_001));

            mockMvc.perform(post("/notifications/whatsapp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(whatsAppDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }

    }
}