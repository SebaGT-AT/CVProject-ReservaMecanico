package cl.reservas.professional;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceOfferingResponse(
        UUID id,
        String name,
        String description,
        int durationMinutes,
        BigDecimal priceAmount,
        String currency,
        boolean active
) {
    static ServiceOfferingResponse from(ServiceOffering service) {
        return new ServiceOfferingResponse(service.getId(), service.getName(), service.getDescription(),
                service.getDurationMinutes(), service.getPriceAmount(), service.getCurrency(), service.isActive());
    }
}
