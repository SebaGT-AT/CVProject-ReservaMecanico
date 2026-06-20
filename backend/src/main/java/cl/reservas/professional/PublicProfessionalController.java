package cl.reservas.professional;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PublicProfessionalController {
    private final ProfessionalService professionalService;

    public PublicProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping("/specialties")
    public List<SpecialtyResponse> specialties() {
        return professionalService.listSpecialties();
    }

    @GetMapping("/professionals/{slug}")
    public PublicProfessionalResponse professional(@PathVariable String slug) {
        return professionalService.publicProfile(slug);
    }
}
