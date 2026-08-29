package com.ofentse.pulse.notification.controller;

import com.ofentse.pulse.notification.email.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.service.NotificationService;
import com.ofentse.pulse.notification.sms.dto.SmsNotificationDTO;
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
    private SmsNotificationDTO smsDTO;

    @BeforeEach
    void setup() {
        emailDTO = new EmailNotificationDTO(
                "user@gmail.com",
                "Welcome",
                "Hello from Pulse"
        );

        smsDTO = new SmsNotificationDTO(
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
    @DisplayName("POST /notifications/sms")
    class sendSmsNotification {

        @Test
        @DisplayName("Returns 202 Accepted when smsDTO is valid")
        void sendSmsNotification_Returns202Accepted_whenDTOIsValid() throws Exception {

            doNothing().when(notificationService).sendSmsNotification(any(SmsNotificationDTO.class));

            mockMvc.perform(post("/notifications/sms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(smsDTO)))
                    .andExpect(status().isAccepted());

            verify(notificationService).sendSmsNotification(any(SmsNotificationDTO.class));
        }

        @Test
        @DisplayName("Returns 400 Bad Request when 'to' is blank")
        void sendSmsNotification_Returns400BadRequest_whenToIsBlank() throws Exception{
            smsDTO.setTo("  ");

            mockMvc.perform(post("/notifications/sms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(smsDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("Returns 400 Bad Request when 'contents' is blank")
        void sendSmsNotification_Returns400BadRequest_whenContentIsBlank() throws Exception{
            smsDTO.setContent("  ");

            mockMvc.perform(post("/notifications/sms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(smsDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("Returns 400 Bad Request when content exceeds maximum length")
        void sendSmsNotification_Returns400BadRequest_whenContentIsTooLong() throws Exception {
            smsDTO.setContent("a".repeat(10_001));

            mockMvc.perform(post("/notifications/sms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(smsDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }

    }
}