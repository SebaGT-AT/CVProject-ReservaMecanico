package cl.reservas.admin;

import cl.reservas.user.Role;
import cl.reservas.user.User;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(UUID id, String name, String email, Role role, boolean active,
                                boolean emailVerified, Instant createdAt) {
    static AdminUserResponse from(User user) {
        return new AdminUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.isActive(), user.isEmailVerified(), user.getCreatedAt());
    }
}
