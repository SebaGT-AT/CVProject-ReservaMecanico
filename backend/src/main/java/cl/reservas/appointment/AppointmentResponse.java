package cl.reservas.appointment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        AppointmentStatus status,
        Instant startAt,
        Instant endAt,
        String professionalTimeZone,
        UUID serviceId,
        String serviceName,
        int durationMinutes,
        BigDecimal priceAmount,
        String currency,
        String professionalName,
        String professionalSlug,
        String customerName,
        String customerEmail,
        String cancellationReason,
        Instant cancelledAt
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getStatus(), appointment.getStartAt(),
                appointment.getEndAt(), appointment.getProfessionalTimeZone(), appointment.getService().getId(),
                appointment.getServiceName(), appointment.getDurationMinutes(), appointment.getPriceAmount(),
                appointment.getCurrency(), appointment.getProfessional().getUser().getName(),
                appointment.getProfessional().getSlug(), appointment.getCustomer().getName(),
                appointment.getCustomer().getEmail(), appointment.getCancellationReason(), appointment.getCancelledAt());
    }
}
