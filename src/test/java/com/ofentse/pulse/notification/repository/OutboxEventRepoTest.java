package com.ofentse.pulse.notification.repository;

import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OutboxEventRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OutboxEventRepo outboxEventRepo;

    private LocalDateTime now;

    @BeforeEach
    void setup() {
        now = LocalDateTime.now();
    }

    private OutboxEvent createEvent(OutboxEventStatus status, LocalDateTime nextRetryAt, LocalDateTime createdAt) {
        Notification notification = new Notification();

        notification.setStatus(NotificationStatus.PENDING);
        notification.setRecipient("new@gmail.com");
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setSubject("Welcome");
        notification.setCreatedAt(now);

        entityManager.persist(notification);

        OutboxEvent event = new OutboxEvent();

        event.setNotification(notification);
        event.setStatus(status);
        event.setNextRetryAt(nextRetryAt);
        event.setPayload("Payload");
        event.setCreatedAt(createdAt);
        event.setRetryCount(0);

        return event;
    }

    @Nested
    @DisplayName("findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc")
    class FindTop100PendingEvents {

        @Test
        @DisplayName("Returns list of Pending events based on nextRetryAt threshold")
        void findTop100PendingEvents_ReturnsQualifyingEvents() {

            OutboxEvent event1 = createEvent(OutboxEventStatus.PENDING, now.minusMinutes(5), now.minusMinutes(10));
            OutboxEvent event2 = createEvent(OutboxEventStatus.PENDING, now.minusMinutes(1), now.minusMinutes(5));

            entityManager.persist(event1);
            entityManager.persist(event2);
            entityManager.flush();

            List<OutboxEvent> eventList = outboxEventRepo.
                    findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(OutboxEventStatus.PENDING, now);

            assertEquals(2, eventList.size());
            assertEquals(eventList.get(0), event1);
            assertEquals(eventList.get(1), event2);
        }

        @Test
        @DisplayName("Filters out events scheduled for the future and non Pending events")
        void findTop100PendingEvents_FiltersOutNonQualifyingEvents() {

            OutboxEvent readyEvent = createEvent(OutboxEventStatus.PENDING, now.minusMinutes(2), now.minusMinutes(10));

            OutboxEvent futureEvent = createEvent(OutboxEventStatus.PENDING, now.plusMinutes(10), now.minusMinutes(5));

            OutboxEvent publishedEvent = createEvent(OutboxEventStatus.PUBLISHED, now.minusMinutes(2), now.minusMinutes(5));
            OutboxEvent failedEvent = createEvent(OutboxEventStatus.FAILED, now.minusMinutes(2), now.minusMinutes(5));

            entityManager.persist(readyEvent);
            entityManager.persist(futureEvent);
            entityManager.persist(publishedEvent);
            entityManager.persist(failedEvent);

            List<OutboxEvent> eventList = outboxEventRepo.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                    OutboxEventStatus.PENDING, now
            );

            assertEquals(1, eventList.size());
            assertEquals(OutboxEventStatus.PENDING, eventList.get(0).getStatus());
            assertThat(eventList.get(0).getNextRetryAt()).isBeforeOrEqualTo(now);
        }

        @Test
        @DisplayName("Caps returned results at maximum 100 records")
        void findTop100PendingEvents_LimitResultsTo100() {

            for (int i = 0; i < 105; i++) {
                OutboxEvent event = createEvent(OutboxEventStatus.PENDING, now.minusMinutes(1), now.minusMinutes(i + 1));
                entityManager.persist(event);
            }
            entityManager.flush();

            List<OutboxEvent> eventList = outboxEventRepo
                    .findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                            OutboxEventStatus.PENDING, now
                    );

            assertEquals(100, eventList.size());
        }
    }
}