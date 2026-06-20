package cl.reservas.professional;

import cl.reservas.common.exception.ConflictException;
import cl.reservas.common.exception.NotFoundException;
import cl.reservas.user.Role;
import cl.reservas.user.User;
import cl.reservas.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {
    @Mock ProfessionalProfileRepository profiles;
    @Mock SpecialtyRepository specialties;
    @Mock ServiceOfferingRepository services;
    @Mock UserRepository users;
    private ProfessionalService service;
    private User professionalUser;

    @BeforeEach
    void setUp() {
        service = new ProfessionalService(profiles, specialties, services, users);
        professionalUser = new User("Ada", "ada@example.com", "encoded", Role.PROFESSIONAL);
    }

    @Test
    void createsDraftProfileWithValidatedSpecialties() {
        UUID specialtyId = UUID.randomUUID();
        Specialty specialty = mock(Specialty.class);
        when(specialty.getId()).thenReturn(specialtyId);
        when(specialty.getName()).thenReturn("Consultoria");
        when(specialty.getSlug()).thenReturn("consultoria");
        when(specialties.findAllByIdInAndActiveTrue(Set.of(specialtyId))).thenReturn(List.of(specialty));
        when(users.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(professionalUser));
        when(profiles.save(any(ProfessionalProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.saveProfile("ada@example.com", new ProfessionalProfileRequest(
                "ada-consultora", "  Una gran profesional  ", null, "America/Santiago", false,
                Set.of(specialtyId)));

        assertThat(response.slug()).isEqualTo("ada-consultora");
        assertThat(response.bio()).isEqualTo("Una gran profesional");
        assertThat(response.specialties()).extracting(SpecialtyResponse::id).containsExactly(specialtyId);
    }

    @Test
    void rejectsDuplicatePublicSlug() {
        when(profiles.existsBySlugAndUser_EmailNotIgnoreCase("ocupado", "ada@example.com")).thenReturn(true);
        var request = new ProfessionalProfileRequest("ocupado", null, null, "America/Santiago", false, Set.of());
        assertThatThrownBy(() -> service.saveProfile("ada@example.com", request))
                .isInstanceOf(ConflictException.class);
        verify(profiles, never()).save(any());
    }

    @Test
    void publishedProfileRequiresAnActiveService() {
        ProfessionalProfile profile = new ProfessionalProfile(professionalUser, "ada", null, null,
                "America/Santiago", false, Set.of());
        when(profiles.findByUserEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(profile));
        when(services.existsByProfessionalIdAndActiveTrue(profile.getId())).thenReturn(false);

        var request = new ProfessionalProfileRequest("ada", null, null, "America/Santiago", true,
                Set.of(UUID.randomUUID()));
        when(specialties.findAllByIdInAndActiveTrue(request.specialtyIds()))
                .thenReturn(List.of(mock(Specialty.class)));

        assertThatThrownBy(() -> service.saveProfile("ada@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("servicio activo");
    }

    @Test
    void createsServiceOwnedByCurrentProfessional() {
        ProfessionalProfile profile = new ProfessionalProfile(professionalUser, "ada", null, null,
                "America/Santiago", false, Set.of());
        when(profiles.findByUserEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(profile));
        when(services.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createService("ada@example.com", new ServiceOfferingRequest(
                "Sesion inicial", "Diagnostico", 60, new BigDecimal("25000"), "CLP", false));

        assertThat(response.name()).isEqualTo("Sesion inicial");
        assertThat(response.active()).isFalse();
        assertThat(response.priceAmount()).isEqualByComparingTo("25000");
    }

    @Test
    void cannotUpdateServiceOwnedByAnotherProfessional() {
        ProfessionalProfile profile = new ProfessionalProfile(professionalUser, "ada", null, null,
                "America/Santiago", false, Set.of());
        UUID serviceId = UUID.randomUUID();
        when(profiles.findByUserEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(profile));
        when(services.findByIdAndProfessionalId(serviceId, profile.getId())).thenReturn(Optional.empty());

        var request = new ServiceOfferingRequest("Servicio", null, 30, BigDecimal.ZERO, "CLP", true);
        assertThatThrownBy(() -> service.updateService("ada@example.com", serviceId, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deactivatingLastServiceAutomaticallyUnpublishesProfile() {
        ProfessionalProfile profile = new ProfessionalProfile(professionalUser, "ada", null, null,
                "America/Santiago", true, Set.of());
        ServiceOffering offering = new ServiceOffering(profile, "Servicio", null, 30,
                new BigDecimal("10000"), "CLP", true);
        when(profiles.findByUserEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(profile));
        when(services.findByIdAndProfessionalId(offering.getId(), profile.getId())).thenReturn(Optional.of(offering));
        when(services.findAllByProfessionalIdAndActiveTrueOrderByName(profile.getId())).thenReturn(List.of());

        service.deactivateService("ada@example.com", offering.getId());

        assertThat(offering.isActive()).isFalse();
        assertThat(profile.isPublished()).isFalse();
    }
}
