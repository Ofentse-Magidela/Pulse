package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.event.OutboxEventCreated;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxWorkerTest {

    @Mock
    private OutboxEventRepo outboxEventRepo;

    @Mock
    private OutboxPublisher outboxPublisher;

    @InjectMocks
    private OutboxWorker outboxWorker;

    private OutboxEvent event1;
    private OutboxEvent event2;

    @BeforeEach
    void setup() {

        event1 = new OutboxEvent();
        event1.setId(1L);
        event1.setStatus(OutboxEventStatus.PENDING);

        event2 = new OutboxEvent();
        event2.setId(2L);
        event2.setStatus(OutboxEventStatus.PENDING);
    }

    @Nested
    @DisplayName("HandleOutboxEventCreated")
    class handleOutboxEventCreated {

        @Test
        @DisplayName("Publish existing pending events")
        void handleOutboxEventCreated_PublishEvents_WhenPendingEventsExist() {

            OutboxEventCreated eventCreated = new OutboxEventCreated();

            when(outboxEventRepo.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                    eq(OutboxEventStatus.PENDING),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(event1, event2));

            outboxWorker.handleOutboxEventCreated(eventCreated);

            verify(outboxPublisher, times(1)).publishEvent(event1);
            verify(outboxPublisher, times(1)).publishEvent(event2);
            verify(outboxPublisher, times(2)).publishEvent(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("Does nothing when there are no pending events")
        void handleOutboxEventCreated_DoesNotPublish_WhenNoPendingEventsExist() {

            OutboxEventCreated eventCreated = new OutboxEventCreated();

            when(outboxEventRepo.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                    eq(OutboxEventStatus.PENDING),
                    any(LocalDateTime.class)
            )).thenReturn(Collections.emptyList());

            outboxWorker.handleOutboxEventCreated(eventCreated);

            verifyNoInteractions(outboxPublisher);
        }
    }

    @Nested
    @DisplayName("RetryPendingEvents")
    class retryPendingEvents{

        @Test
        @DisplayName("RetryPendingEvents - If they exist on a scheduled interval")
        void retryPendingEvents_PublishEvents_WhenPendingEventsExist() {
            when(outboxEventRepo.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                    eq(OutboxEventStatus.PENDING),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(event1, event2));

            outboxWorker.retryPendingEvents();

            verify(outboxPublisher, times(1)).publishEvent(event1);
            verify(outboxPublisher, times(1)).publishEvent(event2);
            verify(outboxPublisher, times(2)).publishEvent(any(OutboxEvent.class));

        }

        @Test
        @DisplayName("RetryPendingEvents - Non existing pending events on a scheduled interval")
        void retryPendingEvents_DoesNotPublish_WhenNoPendingEventsExist() {

            when(outboxEventRepo.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                    eq(OutboxEventStatus.PENDING),
                    any(LocalDateTime.class)
            )).thenReturn(Collections.emptyList());

            outboxWorker.retryPendingEvents();

            verifyNoInteractions(outboxPublisher);
        }
    }

}