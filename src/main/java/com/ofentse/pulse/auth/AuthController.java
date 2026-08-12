package com.ofentse.pulse.auth;

import com.ofentse.pulse.auth.dto.LoginDTO;
import com.ofentse.pulse.auth.dto.RegisterDTO;
import com.ofentse.pulse.auth.service.AuthService;
import com.ofentse.pulse.emailverification.dto.ResendCodeDTO;
import com.ofentse.pulse.emailverification.dto.VerifyEmailDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser (@RequestBody @Valid RegisterDTO registerDTO) {
        service.registerUser(registerDTO);
        return ResponseEntity.status(201).body("Account Created");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser (@RequestBody @Valid LoginDTO loginDTO) {
        String jwtToken = service.loginUser(loginDTO);
        return ResponseEntity.status(200).body(jwtToken);
    }

    @PostMapping("/send-code")
    public ResponseEntity<String> verifyEmail(@RequestBody @Valid VerifyEmailDTO dto) {
        service.verifyEmail(dto);
        return ResponseEntity.ok().body("Email Verified");
    }

    @PostMapping("/resend-code")
    public ResponseEntity<String> resendCode(@RequestBody @Valid ResendCodeDTO dto) {
        service.resendCode(dto);
        return ResponseEntity.ok().body("Code Resent");
    }
}
