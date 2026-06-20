package cl.reservas.professional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, UUID> {
    @EntityGraph(attributePaths = {"user", "specialties"})
    Optional<ProfessionalProfile> findByUserEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"user", "specialties"})
    Optional<ProfessionalProfile> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlugAndUser_EmailNotIgnoreCase(String slug, String email);
}
