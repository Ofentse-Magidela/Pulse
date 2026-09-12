package com.ofentse.pulse.emailverification;

import com.ofentse.pulse.auth.PulseClient;
import com.ofentse.pulse.auth.User;
import com.ofentse.pulse.emailverification.dto.EmailNotificationRequest;
import com.ofentse.pulse.emailverification.dto.ResendCodeDTO;
import com.ofentse.pulse.emailverification.dto.VerifyEmailDTO;
import com.ofentse.pulse.exception.EmailNotFoundException;
import com.ofentse.pulse.exception.InvalidVerificationCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class  EmailVerificationService {

    private final EmailVerificationRepo repo;
    private final PulseClient pulseClient;
    public EmailVerificationService(EmailVerificationRepo repo, PulseClient pulseClient) {
        this.repo = repo;
        this.pulseClient = pulseClient;
    }

    private final SecureRandom random = new SecureRandom();

    public void createAndSendVerification(User user) {

        EmailVerification verification = new EmailVerification();
        verification.setCode(generateVerificationCode());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verification.setStatus(VerificationStatus.ACTIVE);
        verification.setUser(user);
        repo.save(verification);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String expiresAt = verification.getExpiresAt().format(formatter);

        Map<String, String> variables = Map.of(
                "name", user.getUsername(),
                "verificationCode", verification.getCode(),
                "expiresAt", expiresAt
        );

        EmailNotificationRequest request =
                new EmailNotificationRequest(
                        user.getEmail(),
                        "EMAIL_VERIFICATION",
                        variables
                );

        pulseClient.sendEmail(request);
    }

    @Transactional
    public void validateCode(VerifyEmailDTO dto) {

        LocalDateTime timeNow = LocalDateTime.now();
        EmailVerification verification =
                repo.findByUserEmailAndCodeAndStatus(dto.getEmail(), dto.getCode(), VerificationStatus.ACTIVE)
                .orElseThrow(
                        () -> new InvalidVerificationCode("code", "Invalid email or verification code")
                );

        if (timeNow.isAfter(verification.getExpiresAt()))
            throw new InvalidVerificationCode("code", "Verification code expired");

        verification.getUser().setEmailVerified(true);
        verification.setStatus(VerificationStatus.USED);

        EmailNotificationRequest request =
                new EmailNotificationRequest(
                        dto.getEmail(),
                        "EMAIL_VERIFICATION",
                        Map.of("", "")
                );


        pulseClient.sendEmail(request);
    }

    private String generateVerificationCode() {
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    public void invalidateOldCode(ResendCodeDTO dto) {
        EmailVerification verification = repo.findByUserEmailAndStatus(
                dto.getEmail(), VerificationStatus.ACTIVE).orElseThrow(
                () -> new EmailNotFoundException("code", "Email not registered")
        );

        verification.setStatus(VerificationStatus.INVALIDATED);
        repo.save(verification);
    }
}
