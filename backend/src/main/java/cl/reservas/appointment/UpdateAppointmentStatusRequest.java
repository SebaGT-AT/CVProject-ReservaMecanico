package cl.reservas.appointment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAppointmentStatusRequest(
        @NotNull AppointmentStatus status,
        @Size(max = 500) String reason
) {}
