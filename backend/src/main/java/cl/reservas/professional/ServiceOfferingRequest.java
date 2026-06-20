package cl.reservas.professional;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ServiceOfferingRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @Min(10) @Max(720) int durationMinutes,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal priceAmount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "Usa un codigo ISO 4217 de tres letras") String currency,
        boolean active
) {}
