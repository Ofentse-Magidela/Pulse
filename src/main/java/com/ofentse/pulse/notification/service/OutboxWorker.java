package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.event.OutboxEventCreated;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

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

        log.info("Processing pending events");

        List<OutboxEvent> events =
                outboxEventRepo.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxEventStatus.PENDING,
                        LocalDateTime.now()
                );

        log.info("Queried {} events eligible for publishing", events.size());

        for (OutboxEvent event: events) {
            outboxPublisher.publishEvent(event);
        }
    }

    @Scheduled(fixedDelay = 100000)
    public void retryPendingEvents() {
        log.info("Scheduled retry triggered, querying eligible outbox events");
        processPendingEvents();
    }
}
