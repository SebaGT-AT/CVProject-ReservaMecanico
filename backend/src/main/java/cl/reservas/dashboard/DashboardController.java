package cl.reservas.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/professional/dashboard")
@PreAuthorize("hasRole('PROFESSIONAL')")
public class DashboardController {
    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

    @GetMapping
    public ProfessionalDashboardResponse summary(Authentication authentication) {
        return dashboard.professionalSummary(authentication.getName());
    }
}
