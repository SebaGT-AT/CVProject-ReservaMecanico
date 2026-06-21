package cl.reservas.notification;

import cl.reservas.appointment.Appointment;
import cl.reservas.appointment.AppointmentNotificationPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Component
public class OutboxAppointmentNotificationPublisher implements AppointmentNotificationPublisher {
    private final NotificationOutboxRepository outbox;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OutboxAppointmentNotificationPublisher(NotificationOutboxRepository outbox,
                                                   ObjectMapper objectMapper, Clock clock) {
        this.outbox = outbox;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void confirmed(Appointment appointment) {
        enqueueForBoth(appointment, NotificationEventType.APPOINTMENT_CONFIRMED, "confirmed");
    }

    @Override
    @Transactional
    public void cancelled(Appointment appointment) {
        enqueueForBoth(appointment, NotificationEventType.APPOINTMENT_CANCELLED, "cancelled");
    }

    @Override
    @Transactional
    public void reminder24Hours(Appointment appointment) {
        enqueue(appointment, NotificationEventType.APPOINTMENT_REMINDER_24H,
                "appointment:" + appointment.getId() + ":reminder:24h:customer",
                NotificationRecipient.CUSTOMER);
    }

    private void enqueueForBoth(Appointment appointment, NotificationEventType eventType, String keyPart) {
        enqueue(appointment, eventType, "appointment:" + appointment.getId() + ":" + keyPart + ":customer",
                NotificationRecipient.CUSTOMER);
        enqueue(appointment, eventType, "appointment:" + appointment.getId() + ":" + keyPart + ":professional",
                NotificationRecipient.PROFESSIONAL);
    }

    private void enqueue(Appointment appointment, NotificationEventType eventType, String deduplicationKey,
                         NotificationRecipient recipient) {
        boolean customerRecipient = recipient == NotificationRecipient.CUSTOMER;
        AppointmentNotificationPayload payload = new AppointmentNotificationPayload(
                appointment.getId(), recipient,
                customerRecipient ? appointment.getCustomer().getName() : appointment.getProfessional().getUser().getName(),
                customerRecipient ? appointment.getCustomer().getEmail() : appointment.getProfessional().getUser().getEmail(),
                appointment.getCustomer().getName(), appointment.getProfessional().getUser().getName(),
                appointment.getServiceName(), appointment.getStartAt(), appointment.getProfessionalTimeZone(),
                appointment.getCancellationReason());
        try {
            outbox.insertEvent(UUID.randomUUID(), appointment.getId(), eventType.name(), deduplicationKey,
                    objectMapper.writeValueAsString(payload), clock.instant());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar el evento de notificacion", exception);
        }
    }
}
