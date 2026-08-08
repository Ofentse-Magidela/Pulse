package com.ofentse.pulse.auth;

import com.ofentse.pulse.auth.dto.LoginDTO;
import com.ofentse.pulse.auth.dto.RegisterDTO;
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

}
