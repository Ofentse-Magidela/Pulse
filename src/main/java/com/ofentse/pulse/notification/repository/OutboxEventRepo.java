package com.ofentse.pulse.notification.repository;

import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepo extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            OutboxEventStatus status, LocalDateTime time
    );
}
