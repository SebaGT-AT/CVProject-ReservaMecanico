package cl.reservas.admin;

import java.util.List;

public record AdminOperationsResponse(long totalFailures, List<OperationalFailureResponse> failures) {
}
