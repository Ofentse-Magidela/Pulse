package com.ofentse.pulse.notification.controller;

import com.ofentse.pulse.notification.email.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.service.NotificationService;
import com.ofentse.pulse.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

    private EmailNotificationDTO dto;

    @BeforeEach
    void setup() {
        dto = new EmailNotificationDTO(
                "user@gmail.com",
                "Welcome",
                "Hello from Pulse"
        );
    }

    @Nested
    @DisplayName("POST /notifications/email")
    class SendEmailNotification {

        @Test
        @DisplayName("Returns 202 Accepted when dto is valid")
        void sendEmailNotification_Returns202Accepted_whenDTOIsValid() throws Exception {

            doNothing().when(notificationService).sendEmailNotification(any(EmailNotificationDTO.class));

            mockMvc.perform(post("/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isAccepted());

            verify(notificationService).sendEmailNotification(any(EmailNotificationDTO.class));
        }

        @Test
        @DisplayName("Returns 400 Bad Request when subject is blank")
        void sendEmailNotification_Returns400BadRequest_whenSubjectIsBlank() throws Exception {
            dto.setSubject("  ");

            mockMvc.perform(post("/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }


    }
}