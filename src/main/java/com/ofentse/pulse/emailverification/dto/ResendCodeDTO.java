package com.ofentse.pulse.emailverification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResendCodeDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid Email. Please Provide A Valid Email Format")
    @Size(min = 6, max = 256, message = "Email Must Be A Minimum Of 6 Characters And 256 Max")
    private String email;
}
