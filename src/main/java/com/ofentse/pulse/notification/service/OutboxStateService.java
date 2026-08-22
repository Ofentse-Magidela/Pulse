package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxStateService {
    private final OutboxEventRepo outboxRepo;
    public OutboxStateService(OutboxEventRepo outboxRepo) {
        this.outboxRepo = outboxRepo;
    }

    @Transactional
    public void markPublished(OutboxEvent event) {

        event.setStatus(OutboxEventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        event.setFailureReason(null);

        outboxRepo.save(event);
    }

    @Transactional
    public void recordFailure(OutboxEvent event, Exception e) {
        int retryCount = event.getRetryCount() + 1;

        event.setRetryCount(retryCount);
        event.setFailureReason(e.getMessage());

        switch(retryCount) {
            case 1 -> {
                event.setStatus(OutboxEventStatus.PENDING);
                event.setNextRetryAt(LocalDateTime.now().plusMinutes(1));
            }
            case 2 -> {
                event.setStatus(OutboxEventStatus.PENDING);
                event.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
            }
            case 3 -> {
                event.setStatus(OutboxEventStatus.PENDING);
                event.setNextRetryAt(LocalDateTime.now().plusMinutes(15));
            }
            case 4 -> {
                event.setStatus(OutboxEventStatus.PENDING);
                event.setNextRetryAt(LocalDateTime.now().plusMinutes(30));
            }
            case 5 -> event.setStatus(OutboxEventStatus.FAILED);
        }

        outboxRepo.save(event);
    }
}
