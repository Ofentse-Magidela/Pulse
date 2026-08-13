package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.email.EmailService;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final EmailService emailService;
    private final NotificationRepo repo;
    public NotificationService(EmailService emailService, NotificationRepo repo) {
        this.emailService = emailService;
        this.repo = repo;
    }

    public void sendEmailNotification(EmailNotificationDTO dto) {
        Notification notification = new Notification();

        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient(dto.getTo());
        notification.setSubject(dto.getSubject());
        notification.setCreatedAt(LocalDateTime.now());

        try {
            emailService.sendEmail(dto);
        } catch(MailException e) {
            notification.setStatus(NotificationStatus.FAILED);
            repo.save(notification);
            throw e;
        }

        notification.setSentAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.SENT);

        repo.save(notification);
    }
}
