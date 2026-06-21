package cl.reservas.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record BookAppointmentRequest(
        @NotBlank String professionalSlug,
        @NotNull UUID serviceId,
        @NotNull Instant startAt,
        @NotNull UUID idempotencyKey
) {}
