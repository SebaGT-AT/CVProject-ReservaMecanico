package cl.reservas.admin;

import java.time.Instant;
import java.util.UUID;

public record OperationalFailureResponse(String channel, UUID id, UUID aggregateId, String operation,
                                         int attempts, Instant nextAttemptAt, String lastError) {
}
