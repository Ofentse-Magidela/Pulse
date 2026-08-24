package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxStateServiceTest {

    @Mock
    private OutboxEventRepo outboxEventRepo;

    @InjectMocks
    private OutboxStateService outboxStateService;

    private OutboxEvent event;

    @BeforeEach
    void setup() {
        event = new OutboxEvent();
        event.setId(1L);
        event.setRetryCount(0);
        event.setStatus(OutboxEventStatus.PENDING);
    }

    @Nested
    @DisplayName("MarkEvent As Published")
    class markPublished {

        @Test
        @DisplayName("Save event as Published")
        void markPublished_SaveEventWithStatusPublished_WhenSentOutboxEvent() {
            event.setFailureReason("Previous failure");
            LocalDateTime before = LocalDateTime.now();

            outboxStateService.markPublished(event);

            LocalDateTime after = LocalDateTime.now();

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo, times(1)).save(captor.capture());
            OutboxEvent capturedEvent = captor.getValue();

            assertEquals(OutboxEventStatus.PUBLISHED, capturedEvent.getStatus());
            assertEquals(1L, capturedEvent.getId());
            assertNull(capturedEvent.getFailureReason());

            assertNotNull(capturedEvent.getPublishedAt());
            assertFalse(capturedEvent.getPublishedAt().isBefore(before));
            assertFalse(capturedEvent.getPublishedAt().isAfter(after));
        }
    }

    @Nested
    @DisplayName("Record Failed Event")
    class recordFailure {

        @Test
        @DisplayName("First failure schedules retry after 1 minute")
        void recordFailure_FirstAttempt_SchedulesRetryAfterOneMinute() {
            Exception exception = new RuntimeException("RabbitMQ unavailable");

            LocalDateTime before = LocalDateTime.now().plusMinutes(1);

            outboxStateService.recordFailure(event, exception);

            LocalDateTime after = LocalDateTime.now().plusMinutes(1);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo, times(1)).save(captor.capture());
            OutboxEvent capturedEvent = captor.getValue();

            assertEquals(1, capturedEvent.getRetryCount());
            assertEquals(OutboxEventStatus.PENDING, capturedEvent.getStatus());
            assertEquals("RabbitMQ unavailable", capturedEvent.getFailureReason());

            assertNotNull(capturedEvent.getNextRetryAt());
            assertFalse(capturedEvent.getNextRetryAt().isBefore(before));
            assertFalse(capturedEvent.getNextRetryAt().isAfter(after));
        }

        @Test
        @DisplayName("Second failure schedules retry after 5 minutes")
        void recordFailure_SecondAttempt_SchedulesRetryAfterFiveMinutes() {
            Exception exception = new RuntimeException("RabbitMQ unavailable");
            event.setRetryCount(1);

            LocalDateTime before = LocalDateTime.now().plusMinutes(5);

            outboxStateService.recordFailure(event, exception);

            LocalDateTime after = LocalDateTime.now().plusMinutes(5);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo, times(1)).save(captor.capture());
            OutboxEvent capturedEvent = captor.getValue();

            assertEquals(2, capturedEvent.getRetryCount());
            assertEquals(OutboxEventStatus.PENDING, capturedEvent.getStatus());
            assertEquals("RabbitMQ unavailable", capturedEvent.getFailureReason());

            assertNotNull(capturedEvent.getNextRetryAt());
            assertFalse(capturedEvent.getNextRetryAt().isBefore(before));
            assertFalse(capturedEvent.getNextRetryAt().isAfter(after));
        }

        @Test
        @DisplayName("Third failure schedules retry after 15 minutes")
        void recordFailure_ThirdAttempt_SchedulesRetryAfterFifteenMinutes() {
            Exception exception = new RuntimeException("RabbitMQ unavailable");
            event.setRetryCount(2);

            LocalDateTime before = LocalDateTime.now().plusMinutes(15);

            outboxStateService.recordFailure(event, exception);

            LocalDateTime after = LocalDateTime.now().plusMinutes(15);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo, times(1)).save(captor.capture());
            OutboxEvent capturedEvent = captor.getValue();

            assertEquals(3, capturedEvent.getRetryCount());
            assertEquals(OutboxEventStatus.PENDING, capturedEvent.getStatus());
            assertEquals("RabbitMQ unavailable", capturedEvent.getFailureReason());

            assertNotNull(capturedEvent.getNextRetryAt());
            assertFalse(capturedEvent.getNextRetryAt().isBefore(before));
            assertFalse(capturedEvent.getNextRetryAt().isAfter(after));
        }

        @Test
        @DisplayName("Fourth failure schedules retry after 30 minutes")
        void recordFailure_FourthAttempt_SchedulesRetryAfterThirtyMinutes() {
            Exception exception = new RuntimeException("RabbitMQ unavailable");
            event.setRetryCount(3);

            LocalDateTime before = LocalDateTime.now().plusMinutes(30);

            outboxStateService.recordFailure(event, exception);

            LocalDateTime after = LocalDateTime.now().plusMinutes(30);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo, times(1)).save(captor.capture());
            OutboxEvent capturedEvent = captor.getValue();

            assertEquals(4, capturedEvent.getRetryCount());
            assertEquals(OutboxEventStatus.PENDING, capturedEvent.getStatus());
            assertEquals("RabbitMQ unavailable", capturedEvent.getFailureReason());

            assertNotNull(capturedEvent.getNextRetryAt());
            assertFalse(capturedEvent.getNextRetryAt().isBefore(before));
            assertFalse(capturedEvent.getNextRetryAt().isAfter(after));
        }

        @Test
        @DisplayName("Fifth failure immediately marks event as FAILED")
        void recordFailure_FifthAttempt_MarkEventAsFailed() {
            Exception exception = new RuntimeException("RabbitMQ unavailable");
            event.setRetryCount(4);
            event.setNextRetryAt(LocalDateTime.now().plusMinutes(30));

            outboxStateService.recordFailure(event, exception);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepo, times(1)).save(captor.capture());
            OutboxEvent capturedEvent = captor.getValue();

            assertEquals(5, capturedEvent.getRetryCount());
            assertEquals(OutboxEventStatus.FAILED, capturedEvent.getStatus());
            assertEquals("RabbitMQ unavailable", capturedEvent.getFailureReason());
            assertNull(capturedEvent.getNextRetryAt());
        }
    }
}