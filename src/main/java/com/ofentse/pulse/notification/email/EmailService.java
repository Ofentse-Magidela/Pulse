package com.ofentse.pulse.notification.email;

import com.ofentse.pulse.notification.dto.EmailNotificationDTO;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(EmailNotificationDTO dto) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(System.getenv("MAIL_USERNAME"));
        message.setTo(dto.getTo());
        message.setSubject(dto.getSubject());
        message.setText(dto.getContent());

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
