package cl.reservas.notification;

import java.time.Instant;
import java.util.UUID;

public record AppointmentNotificationPayload(
        UUID appointmentId,
        NotificationRecipient recipient,
        String recipientName,
        String recipientEmail,
        String customerName,
        String professionalName,
        String serviceName,
        Instant startAt,
        String professionalTimeZone,
        String cancellationReason
) {}
