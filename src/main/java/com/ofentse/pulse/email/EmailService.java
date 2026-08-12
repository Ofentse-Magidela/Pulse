package com.ofentse.pulse.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(System.getenv("MAIL_USERNAME"));
        message.setTo(to);
        message.setSubject("Email Verification Code");
        message.setText(code);

        mailSender.send(message);
    }

    public void sendVerifiedEmail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(System.getenv("MAIL_USERNAME"));
        message.setTo(email);
        message.setSubject("Email Verified");
        message.setText("Your email was verified successfully");

        mailSender.send(message);
    }
}
