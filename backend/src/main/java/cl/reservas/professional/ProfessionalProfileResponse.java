package cl.reservas.professional;

import java.util.List;
import java.util.UUID;

public record ProfessionalProfileResponse(
        UUID id,
        String name,
        String slug,
        String bio,
        String phone,
        String timeZone,
        boolean published,
        List<SpecialtyResponse> specialties
) {
    static ProfessionalProfileResponse from(ProfessionalProfile profile) {
        return new ProfessionalProfileResponse(profile.getId(), profile.getUser().getName(), profile.getSlug(),
                profile.getBio(), profile.getPhone(), profile.getTimeZone(), profile.isPublished(),
                profile.getSpecialties().stream().map(SpecialtyResponse::from).toList());
    }
}
