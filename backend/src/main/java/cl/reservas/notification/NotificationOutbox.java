package cl.reservas.notification;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {
    @Id private UUID id;
    @Column(name = "aggregate_type", nullable = false, length = 50) private String aggregateType;
    @Column(name = "aggregate_id", nullable = false) private UUID aggregateId;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, length = 60)
    private NotificationEventType eventType;
    @Column(name = "deduplication_key", nullable = false, unique = true, length = 160)
    private String deduplicationKey;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb") private String payload;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OutboxStatus status;
    @Column(nullable = false) private int attempts;
    @Column(name = "next_attempt_at", nullable = false) private Instant nextAttemptAt;
    @Column(name = "locked_at") private Instant lockedAt;
    @Column(name = "processed_at") private Instant processedAt;
    @Column(name = "last_error", length = 1000) private String lastError;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected NotificationOutbox() {}

    public NotificationOutbox(UUID aggregateId, NotificationEventType eventType,
                              String deduplicationKey, String payload, Instant now) {
        this.id = UUID.randomUUID();
        this.aggregateType = "APPOINTMENT";
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.deduplicationKey = deduplicationKey;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.nextAttemptAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void claim(Instant now) {
        this.status = OutboxStatus.PROCESSING;
        this.lockedAt = now;
        this.updatedAt = now;
    }

    public void markSent(Instant now) {
        this.status = OutboxStatus.SENT;
        this.processedAt = now;
        this.lockedAt = null;
        this.lastError = null;
        this.updatedAt = now;
    }

    public void markFailed(String error, Instant retryAt, int maximumAttempts, Instant now) {
        this.attempts++;
        this.status = attempts >= maximumAttempts ? OutboxStatus.DEAD : OutboxStatus.FAILED;
        this.nextAttemptAt = retryAt;
        this.lockedAt = null;
        this.lastError = error == null ? "Error desconocido" : error.substring(0, Math.min(error.length(), 1000));
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getAggregateId() { return aggregateId; }
    public NotificationEventType getEventType() { return eventType; }
    public String getDeduplicationKey() { return deduplicationKey; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public String getLastError() { return lastError; }
}
