package com.ofentse.pulse.auth;

import com.ofentse.pulse.auth.dto.LoginDTO;
import com.ofentse.pulse.auth.dto.RegisterDTO;
import com.ofentse.pulse.exception.EmailAlreadyExistException;
import com.ofentse.pulse.exception.BadLoginException;
import com.ofentse.pulse.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager auth;
    private final JwtService jwtService;

    public AuthService(UserRepository repository, BCryptPasswordEncoder encoder, AuthenticationManager auth, JwtService jwtService) {
        this.repository = repository;
        this.encoder = encoder;
        this.auth = auth;
        this.jwtService = jwtService;
    }

    public void registerUser(RegisterDTO dto) {
        User user = new User();

        if (repository.existsByEmail(dto.getEmail()))
            throw new EmailAlreadyExistException("Email", "Email already exist");

        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());

        repository.save(user);
    }

    public String loginUser(LoginDTO dto) {

        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    dto.getEmail(), dto.getPassword());
            Authentication authentication = auth.authenticate(token);

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            return jwtService.generateToken(principal.getId());

        } catch (BadCredentialsException e) {
            throw new BadLoginException("login", "Email or password is incorrect");
        }
    }
}
