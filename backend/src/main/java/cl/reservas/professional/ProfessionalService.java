package cl.reservas.professional;

import cl.reservas.common.exception.ConflictException;
import cl.reservas.common.exception.NotFoundException;
import cl.reservas.user.User;
import cl.reservas.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Currency;

@Service
public class ProfessionalService {
    private final ProfessionalProfileRepository profiles;
    private final SpecialtyRepository specialties;
    private final ServiceOfferingRepository services;
    private final UserRepository users;

    public ProfessionalService(ProfessionalProfileRepository profiles, SpecialtyRepository specialties,
                               ServiceOfferingRepository services, UserRepository users) {
        this.profiles = profiles;
        this.specialties = specialties;
        this.services = services;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public ProfessionalProfileResponse myProfile(String email) {
        return ProfessionalProfileResponse.from(requireProfile(email));
    }

    @Transactional
    public ProfessionalProfileResponse saveProfile(String email, ProfessionalProfileRequest request) {
        validateTimeZone(request.timeZone());
        if (profiles.existsBySlugAndUser_EmailNotIgnoreCase(request.slug(), email)) {
            throw new ConflictException("La URL publica ya esta en uso");
        }
        Set<Specialty> selected = loadSpecialties(request.specialtyIds());
        ProfessionalProfile profile = profiles.findByUserEmailIgnoreCase(email).orElse(null);
        if (request.published()) {
            if (selected.isEmpty()) throw new IllegalArgumentException("Selecciona al menos una especialidad para publicar");
            if (profile == null || !services.existsByProfessionalIdAndActiveTrue(profile.getId())) {
                throw new IllegalArgumentException("Crea al menos un servicio activo antes de publicar el perfil");
            }
        }
        if (profile == null) {
            User user = users.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            profile = new ProfessionalProfile(user, request.slug(), clean(request.bio()), clean(request.phone()),
                    request.timeZone(), request.published(), selected);
        } else {
            profile.update(request.slug(), clean(request.bio()), clean(request.phone()), request.timeZone(),
                    request.published(), selected);
        }
        return ProfessionalProfileResponse.from(profiles.save(profile));
    }

    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> myServices(String email) {
        ProfessionalProfile profile = requireProfile(email);
        return services.findAllByProfessionalIdOrderByCreatedAtDesc(profile.getId()).stream()
                .map(ServiceOfferingResponse::from).toList();
    }

    @Transactional
    public ServiceOfferingResponse createService(String email, ServiceOfferingRequest request) {
        ProfessionalProfile profile = requireProfile(email);
        validateCurrency(request.currency());
        ServiceOffering service = new ServiceOffering(profile, request.name().trim(), clean(request.description()),
                request.durationMinutes(), request.priceAmount(), request.currency(), request.active());
        return ServiceOfferingResponse.from(services.save(service));
    }

    @Transactional
    public ServiceOfferingResponse updateService(String email, UUID id, ServiceOfferingRequest request) {
        ProfessionalProfile profile = requireProfile(email);
        ServiceOffering service = services.findByIdAndProfessionalId(id, profile.getId())
                .orElseThrow(() -> new NotFoundException("Servicio no encontrado"));
        validateCurrency(request.currency());
        service.update(request.name().trim(), clean(request.description()), request.durationMinutes(),
                request.priceAmount(), request.currency(), request.active());
        if (!request.active()) unpublishIfNoActiveServices(profile, id);
        return ServiceOfferingResponse.from(service);
    }

    @Transactional
    public void deactivateService(String email, UUID id) {
        ProfessionalProfile profile = requireProfile(email);
        ServiceOffering service = services.findByIdAndProfessionalId(id, profile.getId())
                .orElseThrow(() -> new NotFoundException("Servicio no encontrado"));
        service.deactivate();
        unpublishIfNoActiveServices(profile, id);
    }

    @Transactional(readOnly = true)
    public PublicProfessionalResponse publicProfile(String slug) {
        ProfessionalProfile profile = profiles.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new NotFoundException("Profesional no encontrado"));
        List<ServiceOfferingResponse> activeServices = services
                .findAllByProfessionalIdAndActiveTrueOrderByName(profile.getId()).stream()
                .map(ServiceOfferingResponse::from).toList();
        return new PublicProfessionalResponse(profile.getUser().getName(), profile.getSlug(), profile.getBio(),
                profile.getTimeZone(), profile.getSpecialties().stream().map(SpecialtyResponse::from).toList(),
                activeServices);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listSpecialties() {
        return specialties.findAllByActiveTrueOrderByName().stream().map(SpecialtyResponse::from).toList();
    }

    private ProfessionalProfile requireProfile(String email) {
        return profiles.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Completa primero tu perfil profesional"));
    }

    private Set<Specialty> loadSpecialties(Set<UUID> ids) {
        if (ids.isEmpty()) return Set.of();
        List<Specialty> found = specialties.findAllByIdInAndActiveTrue(ids);
        if (found.size() != ids.size()) throw new IllegalArgumentException("Una especialidad no existe o esta inactiva");
        return new LinkedHashSet<>(found);
    }

    private boolean hasAnotherActiveService(UUID professionalId, UUID deactivatedId) {
        return services.findAllByProfessionalIdAndActiveTrueOrderByName(professionalId).stream()
                .anyMatch(service -> !service.getId().equals(deactivatedId));
    }

    private void unpublishIfNoActiveServices(ProfessionalProfile profile, UUID deactivatedId) {
        if (profile.isPublished() && !hasAnotherActiveService(profile.getId(), deactivatedId)) {
            profile.update(profile.getSlug(), profile.getBio(), profile.getPhone(), profile.getTimeZone(),
                    false, profile.getSpecialties());
        }
    }

    private void validateCurrency(String currency) {
        try { Currency.getInstance(currency); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("La moneda no es valida"); }
    }

    private void validateTimeZone(String timeZone) {
        try { ZoneId.of(timeZone); }
        catch (DateTimeException exception) { throw new IllegalArgumentException("La zona horaria no es valida"); }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
