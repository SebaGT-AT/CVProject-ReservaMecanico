package cl.reservas.appointment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerAppointmentController {
    private final AppointmentService appointments;

    public CustomerAppointmentController(AppointmentService appointments) { this.appointments = appointments; }

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(Authentication authentication,
                                                    @Valid @RequestBody BookAppointmentRequest request) {
        AppointmentBookingResult result = appointments.book(authentication.getName(), request);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.appointment());
    }

    @GetMapping("/mine")
    public List<AppointmentResponse> mine(Authentication authentication) {
        return appointments.customerHistory(authentication.getName());
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(Authentication authentication, @PathVariable UUID id,
                                      @Valid @RequestBody CancelAppointmentRequest request) {
        return appointments.cancelByCustomer(authentication.getName(), id, request);
    }
}
