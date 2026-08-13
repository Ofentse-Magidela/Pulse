package com.ofentse.pulse.notification.controller;

import com.ofentse.pulse.notification.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.service.NotificationService;
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
}
