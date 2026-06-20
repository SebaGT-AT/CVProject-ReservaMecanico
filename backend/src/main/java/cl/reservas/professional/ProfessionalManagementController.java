package cl.reservas.professional;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professional")
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ProfessionalManagementController {
    private final ProfessionalService professionalService;

    public ProfessionalManagementController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping("/profile")
    public ProfessionalProfileResponse profile(Authentication authentication) {
        return professionalService.myProfile(authentication.getName());
    }

    @PutMapping("/profile")
    public ProfessionalProfileResponse saveProfile(Authentication authentication,
                                                   @Valid @RequestBody ProfessionalProfileRequest request) {
        return professionalService.saveProfile(authentication.getName(), request);
    }

    @GetMapping("/services")
    public List<ServiceOfferingResponse> services(Authentication authentication) {
        return professionalService.myServices(authentication.getName());
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOfferingResponse createService(Authentication authentication,
                                                  @Valid @RequestBody ServiceOfferingRequest request) {
        return professionalService.createService(authentication.getName(), request);
    }

    @PutMapping("/services/{id}")
    public ServiceOfferingResponse updateService(Authentication authentication, @PathVariable UUID id,
                                                  @Valid @RequestBody ServiceOfferingRequest request) {
        return professionalService.updateService(authentication.getName(), id, request);
    }

    @DeleteMapping("/services/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateService(Authentication authentication, @PathVariable UUID id) {
        professionalService.deactivateService(authentication.getName(), id);
    }
}
