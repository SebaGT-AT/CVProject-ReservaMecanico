package cl.reservas.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationOutboxStateService {
    private final NotificationOutboxRepository outbox;
    private final Clock clock;
    private final int batchSize;
    private final int maximumAttempts;

    public NotificationOutboxStateService(NotificationOutboxRepository outbox, Clock clock,
                                          @Value("${app.notifications.batch-size:20}") int batchSize,
                                          @Value("${app.notifications.maximum-attempts:8}") int maximumAttempts) {
        this.outbox = outbox;
        this.clock = clock;
        this.batchSize = batchSize;
        this.maximumAttempts = maximumAttempts;
    }

    @Transactional
    public List<OutboxMessage> claimBatch() {
        Instant now = clock.instant();
        List<NotificationOutbox> claimed = outbox.findClaimable(now, now.minus(Duration.ofMinutes(10)), batchSize);
        claimed.forEach(item -> item.claim(now));
        return claimed.stream().map(OutboxMessage::from).toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(UUID id) {
        outbox.findById(id).ifPresent(item -> item.markSent(clock.instant()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID id, int previousAttempts, Exception exception) {
        Instant now = clock.instant();
        long delaySeconds = Math.min(3600, 30L * (1L << Math.min(previousAttempts, 7)));
        outbox.findById(id).ifPresent(item -> item.markFailed(
                exception.getMessage(), now.plusSeconds(delaySeconds), maximumAttempts, now));
    }
}
