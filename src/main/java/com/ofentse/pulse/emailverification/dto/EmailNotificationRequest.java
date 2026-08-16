package com.ofentse.pulse.emailverification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class EmailNotificationRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid Email. Please Provide A Valid Email Format")
    @Size(min = 6, max = 256, message = "Email Must Be A Minimum Of 8 Characters And 256 Max")
    private String to;

    @NotBlank(message = "Email subject cannot be blank")
    @Size(min = 1, max = 256, message = "Email Must Be A Minimum Of 1 Character And 256 Max")
    private String subject;

    @NotBlank
    @Size(max = 10_000, message = "Content cannot exceed 10,000 characters")
    private String content;
}

