package com.ofentse.pulse.notification.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WhatsAppApiRequest {

    private String messaging_product;
    private String to;
    private String type;
    private Text text;

    @AllArgsConstructor
    @Getter
    public static class Text {
        private String body;
    }
}
