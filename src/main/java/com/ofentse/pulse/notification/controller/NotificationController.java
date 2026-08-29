package com.ofentse.pulse.notification.controller;

import com.ofentse.pulse.notification.email.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.service.NotificationService;
import com.ofentse.pulse.notification.sms.dto.SmsNotificationDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;
    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/email")
    public ResponseEntity<Void> sendEmailNotification(@RequestBody @Valid EmailNotificationDTO dto) {
        service.sendEmailNotification(dto);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/sms")
    public ResponseEntity<Void> sendSmsNotification(@RequestBody @Valid SmsNotificationDTO dto) {
        service.sendSmsNotification(dto);
        return ResponseEntity.accepted().build();
    }
}
