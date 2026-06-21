package cl.reservas.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.delivery", havingValue = "log", matchIfMissing = true)
public class LoggingAppointmentEmailSender implements AppointmentEmailSender {
    private static final Logger log = LoggerFactory.getLogger(LoggingAppointmentEmailSender.class);

    @Override
    public void send(NotificationEventType eventType, AppointmentNotificationPayload payload) {
        log.info("DEV MAIL appointment event={} appointmentId={} recipientType={} recipient={}",
                eventType, payload.appointmentId(), payload.recipient(), payload.recipientEmail());
    }
}
