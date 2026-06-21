package cl.reservas.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationOutboxWorker {
    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxWorker.class);
    private final NotificationOutboxStateService state;
    private final AppointmentEmailSender sender;
    private final ObjectMapper objectMapper;

    public NotificationOutboxWorker(NotificationOutboxStateService state, AppointmentEmailSender sender,
                                    ObjectMapper objectMapper) {
        this.state = state;
        this.sender = sender;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.notifications.poll-delay-ms:5000}",
            initialDelayString = "${app.notifications.initial-delay-ms:5000}")
    public void dispatch() {
        for (OutboxMessage message : state.claimBatch()) {
            try {
                AppointmentNotificationPayload payload = objectMapper.readValue(
                        message.payload(), AppointmentNotificationPayload.class);
                sender.send(message.eventType(), payload);
                state.markSent(message.id());
            } catch (Exception exception) {
                log.warn("Notification delivery failed outboxId={} attempt={} error={}",
                        message.id(), message.attempts() + 1, exception.toString());
                log.debug("Notification delivery stacktrace outboxId={}", message.id(), exception);
                state.markFailed(message.id(), message.attempts(), exception);
            }
        }
    }
}
