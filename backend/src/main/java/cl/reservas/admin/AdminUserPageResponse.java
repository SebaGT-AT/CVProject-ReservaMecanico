package cl.reservas.admin;

import java.util.List;

public record AdminUserPageResponse(List<AdminUserResponse> items, int page, int size,
                                    long totalItems, int totalPages) {
}
