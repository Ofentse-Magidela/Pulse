package com.ofentse.pulse.notification.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepo extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByName(String name);
}
