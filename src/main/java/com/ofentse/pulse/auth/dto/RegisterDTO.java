package com.ofentse.pulse.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 6, max = 26, message = "Username Must Be Between 6 - 26 Characters")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid Email. Please Provide A Valid Email Format")
    @Size(min = 6, max = 256, message = "Email Must Be A Minimum Of 6 Characters And 256 Max")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 12, max = 26, message = "Password Must Be Between 12 - 26 Characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?])\\S*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;
}
