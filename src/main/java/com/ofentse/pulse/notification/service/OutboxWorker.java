package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.event.OutboxEventCreated;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
public class OutboxWorker {

    private final OutboxEventRepo outboxEventRepo;
    private final OutboxPublisher outboxPublisher;

    public OutboxWorker(OutboxEventRepo outboxEventRepo, OutboxPublisher outboxPublisher) {
        this.outboxEventRepo = outboxEventRepo;
        this.outboxPublisher = outboxPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxEventCreated(OutboxEventCreated eventCreated) {
        processPendingEvents();
    }

    public void processPendingEvents() {


        List<OutboxEvent> events =
                outboxEventRepo.findTop100ByStatusOrderByCreatedAtAsc(
                        OutboxEventStatus.PENDING
                );

        for (OutboxEvent event: events) {
            outboxPublisher.publishEvent(event);
        }
    }
}
