package com.ofentse.pulse.emailverification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepo extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByUserEmailAndCodeAndStatus(
            String email, String code, VerificationStatus status);

    Optional<EmailVerification> findByUserEmailAndStatus(
            String email, VerificationStatus status);
}
