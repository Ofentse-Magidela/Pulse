package com.ofentse.pulse.emailverification;

import com.ofentse.pulse.auth.User;
import com.ofentse.pulse.email.EmailService;
import com.ofentse.pulse.emailverification.dto.ResendCodeDTO;
import com.ofentse.pulse.emailverification.dto.VerifyEmailDTO;
import com.ofentse.pulse.exception.EmailNotFoundException;
import com.ofentse.pulse.exception.InvalidVerificationCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepo repo;
    private final EmailService emailService;
    public EmailVerificationService(EmailVerificationRepo repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    private final SecureRandom random = new SecureRandom();

    public void createAndSendVerification(User user) {

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setCode(generateVerificationCode());
        emailVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        emailVerification.setStatus(VerificationStatus.ACTIVE);
        emailVerification.setUser(user);
        repo.save(emailVerification);

        emailService.sendEmail(user.getEmail(), emailVerification.getCode());
    }

    @Transactional
    public void validateCode(VerifyEmailDTO dto) {

        LocalDateTime now = LocalDateTime.now();
        EmailVerification verification = repo.findByUserEmailAndCodeAndStatus(
                dto.getEmail(), dto.getCode(), VerificationStatus.ACTIVE)
                .orElseThrow(
                        () -> new InvalidVerificationCode(
                                        "code",
                                        "Invalid email or verification code"
                        ));

        if (now.isAfter(verification.getExpiresAt()))
            throw new InvalidVerificationCode("code", "Verification code expired");

        verification.getUser().setEmailVerified(true);
        verification.setStatus(VerificationStatus.USED);

        emailService.sendVerifiedEmail(dto.getEmail());

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
