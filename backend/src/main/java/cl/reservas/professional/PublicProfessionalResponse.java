package cl.reservas.professional;

import java.util.List;

public record PublicProfessionalResponse(
        String name,
        String slug,
        String bio,
        String timeZone,
        List<SpecialtyResponse> specialties,
        List<ServiceOfferingResponse> services
) {}
