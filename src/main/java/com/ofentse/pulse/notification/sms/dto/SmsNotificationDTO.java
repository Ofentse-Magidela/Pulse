package com.ofentse.pulse.notification.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class SmsNotificationDTO {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+0-9][0-9\\s-]{7,19}$", message = "Please enter a valid phone number")
    private String to;

    @NotBlank(message = "Message contents cannot be blank")
    @Size(max = 10_000, message = "Content cannot exceed 10,000 characters")
    private String content;
}

