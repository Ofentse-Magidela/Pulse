package com.ofentse.pulse.auth;

import com.ofentse.pulse.emailverification.dto.EmailNotificationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PulseClient {

    private final RestClient restClient;
    public PulseClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    public void sendEmail(EmailNotificationRequest request) {
        restClient.post()
                .uri("/notifications/email")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
