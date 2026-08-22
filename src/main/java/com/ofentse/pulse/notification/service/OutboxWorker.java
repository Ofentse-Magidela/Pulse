package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.event.OutboxEventCreated;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxWorker {

    private final OutboxEventRepo outboxEventRepo;
    private final OutboxPublisher outboxPublisher;

    public OutboxWorker(OutboxEventRepo outboxEventRepo, OutboxPublisher outboxPublisher) {
        this.outboxEventRepo = outboxEventRepo;
        this.outboxPublisher = outboxPublisher;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxEventCreated(OutboxEventCreated eventCreated) {
        processPendingEvents();
    }

    private void processPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepo.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxEventStatus.PENDING,
                        LocalDateTime.now()
                );

        for (OutboxEvent event: events) {
            outboxPublisher.publishEvent(event);
        }
    }

    @Scheduled(fixedDelay = 100000)
    public void retryPendingEvents() {
        processPendingEvents();
    }
}
