package cl.reservas.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxStateServiceTest {
    @Mock NotificationOutboxRepository repository;

    @Test
    void claimMarksMessagesAsProcessing() {
        Instant now = Instant.parse("2026-06-21T12:00:00Z");
        NotificationOutbox event = event(now);
        var service = service(now);
        when(repository.findClaimable(eq(now), eq(now.minusSeconds(600)), eq(20))).thenReturn(List.of(event));

        List<OutboxMessage> claimed = service.claimBatch();

        assertThat(claimed).hasSize(1);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    void failureUsesBackoffAndEventuallyCanBeRetried() {
        Instant now = Instant.parse("2026-06-21T12:00:00Z");
        NotificationOutbox event = event(now);
        var service = service(now);
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        service.markFailed(event.getId(), 0, new IllegalStateException("SMTP temporal"));

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getNextAttemptAt()).isEqualTo(now.plusSeconds(30));
        assertThat(event.getLastError()).contains("SMTP temporal");
    }

    private NotificationOutboxStateService service(Instant now) {
        return new NotificationOutboxStateService(repository, Clock.fixed(now, ZoneOffset.UTC), 20, 8);
    }

    private NotificationOutbox event(Instant now) {
        return new NotificationOutbox(UUID.randomUUID(), NotificationEventType.APPOINTMENT_CONFIRMED,
                "dedupe", "{}", now);
    }
}
