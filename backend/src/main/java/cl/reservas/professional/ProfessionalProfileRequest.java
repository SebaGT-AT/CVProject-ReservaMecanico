package cl.reservas.professional;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;

public record ProfessionalProfileRequest(
        @NotBlank @Size(min = 3, max = 80)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Usa letras minusculas, numeros y guiones")
        String slug,
        @Size(max = 1000) String bio,
        @Size(max = 30) String phone,
        @NotBlank @Size(max = 60) String timeZone,
        boolean published,
        @NotNull @Size(max = 5) Set<UUID> specialtyIds
) {}
