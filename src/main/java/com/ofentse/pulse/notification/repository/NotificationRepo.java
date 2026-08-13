package com.ofentse.pulse.notification.repository;


import com.ofentse.pulse.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
}
