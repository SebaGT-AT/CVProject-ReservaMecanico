package cl.reservas.admin;

import java.time.Instant;

public record AdminOverviewResponse(
        Instant generatedAt,
        long totalUsers,
        long customers,
        long professionals,
        long newUsersLast30Days,
        long appointmentsToday,
        long upcomingConfirmedAppointments,
        long cancellationsLast30Days,
        long connectedGoogleCalendars,
        long pendingOperationalFailures) {
}
