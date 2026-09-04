package com.ofentse.pulse.notification.whatsapp;

import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppApiRequest;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WhatsAppApiClient {

    private final RestClient restClient;
    public WhatsAppApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.graph-api-version}")
    private String graphApiVersion;

    public void sendMessage(WhatsAppNotificationMessage message){

        WhatsAppApiRequest request = new WhatsAppApiRequest(
                "whatsapp",
                message.getTo(),
                "text",
                new WhatsAppApiRequest.Text(message.getContent())
        );

        restClient.post()
                .uri("https://graph.facebook.com/{version}/{phoneNumberId}/messages",
                        graphApiVersion, phoneNumberId)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
