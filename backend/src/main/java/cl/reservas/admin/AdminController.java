package cl.reservas.admin;

import cl.reservas.user.Role;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService admin;

    public AdminController(AdminService admin) { this.admin = admin; }

    @GetMapping("/overview")
    public AdminOverviewResponse overview() { return admin.overview(); }

    @GetMapping("/users")
    public AdminUserPageResponse users(@RequestParam(defaultValue = "") String query,
                                       @RequestParam(required = false) Role role,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return admin.users(query, role, page, size);
    }

    @PatchMapping("/users/{id}/status")
    public AdminUserResponse updateStatus(Authentication authentication, @PathVariable UUID id,
                                          @Valid @RequestBody UpdateUserStatusRequest request) {
        return admin.updateStatus(authentication.getName(), id, request.active());
    }

    @GetMapping("/operations/failures")
    public AdminOperationsResponse failures() { return admin.operations(); }
}
