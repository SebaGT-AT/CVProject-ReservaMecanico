package cl.reservas.professional;

import java.util.UUID;

public record SpecialtyResponse(UUID id, String name, String slug) {
    static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(specialty.getId(), specialty.getName(), specialty.getSlug());
    }
}
