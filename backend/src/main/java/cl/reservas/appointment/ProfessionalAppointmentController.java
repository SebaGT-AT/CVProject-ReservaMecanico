package cl.reservas.appointment;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professional/appointments")
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ProfessionalAppointmentController {
    private final AppointmentService appointments;

    public ProfessionalAppointmentController(AppointmentService appointments) { this.appointments = appointments; }

    @GetMapping
    public List<AppointmentResponse> appointments(Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return appointments.professionalAppointments(authentication.getName(), from, to);
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponse updateStatus(Authentication authentication, @PathVariable UUID id,
                                             @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        return appointments.updateByProfessional(authentication.getName(), id, request);
    }
}
