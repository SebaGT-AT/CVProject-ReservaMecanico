package cl.reservas.notification;

import java.util.UUID;

record OutboxMessage(UUID id, NotificationEventType eventType, String payload, int attempts) {
    static OutboxMessage from(NotificationOutbox outbox) {
        return new OutboxMessage(outbox.getId(), outbox.getEventType(), outbox.getPayload(), outbox.getAttempts());
    }
}
