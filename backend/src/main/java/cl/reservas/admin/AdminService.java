package cl.reservas.admin;

import cl.reservas.appointment.AppointmentRepository;
import cl.reservas.appointment.AppointmentStatus;
import cl.reservas.auth.RefreshSessionRepository;
import cl.reservas.common.exception.ConflictException;
import cl.reservas.common.exception.NotFoundException;
import cl.reservas.integration.googlecalendar.*;
import cl.reservas.notification.*;
import cl.reservas.user.Role;
import cl.reservas.user.User;
import cl.reservas.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class AdminService {
    private static final EnumSet<OutboxStatus> FAILURE_STATUSES = EnumSet.of(OutboxStatus.FAILED, OutboxStatus.DEAD);
    private final UserRepository users;
    private final AppointmentRepository appointments;
    private final NotificationOutboxRepository notificationOutbox;
    private final CalendarSyncOutboxRepository calendarOutbox;
    private final GoogleCalendarConnectionRepository googleConnections;
    private final AdminAuditEventRepository auditEvents;
    private final RefreshSessionRepository sessions;
    private final Clock clock;

    public AdminService(UserRepository users, AppointmentRepository appointments,
                        NotificationOutboxRepository notificationOutbox,
                        CalendarSyncOutboxRepository calendarOutbox,
                        GoogleCalendarConnectionRepository googleConnections,
                        AdminAuditEventRepository auditEvents, RefreshSessionRepository sessions, Clock clock) {
        this.users = users;
        this.appointments = appointments;
        this.notificationOutbox = notificationOutbox;
        this.calendarOutbox = calendarOutbox;
        this.googleConnections = googleConnections;
        this.auditEvents = auditEvents;
        this.sessions = sessions;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        Instant now = clock.instant();
        Instant dayStart = now.atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd = dayStart.plus(Duration.ofDays(1));
        Instant thirtyDaysAgo = now.minus(Duration.ofDays(30));
        long failures = notificationOutbox.countByStatusIn(FAILURE_STATUSES)
                + calendarOutbox.countByStatusIn(FAILURE_STATUSES);
        return new AdminOverviewResponse(now, users.count(), users.countByRole(Role.CUSTOMER),
                users.countByRole(Role.PROFESSIONAL), users.countByCreatedAtGreaterThanEqual(thirtyDaysAgo),
                appointments.countByStartAtGreaterThanEqualAndStartAtLessThanAndStatusNot(
                        dayStart, dayEnd, AppointmentStatus.CANCELLED),
                appointments.countByStatusAndStartAtGreaterThanEqual(AppointmentStatus.CONFIRMED, now),
                appointments.countByStatusAndUpdatedAtGreaterThanEqual(AppointmentStatus.CANCELLED, thirtyDaysAgo),
                googleConnections.countByStatus(GoogleConnectionStatus.CONNECTED), failures);
    }

    @Transactional(readOnly = true)
    public AdminUserPageResponse users(String query, Role role, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        var result = users.searchForAdmin(query == null ? "" : query.trim(), role,
                PageRequest.of(safePage, safeSize));
        return new AdminUserPageResponse(result.getContent().stream().map(AdminUserResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public AdminUserResponse updateStatus(String actorEmail, UUID targetId, boolean active) {
        User actor = users.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new NotFoundException("Administrador no encontrado"));
        User target = users.findById(targetId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (actor.getId().equals(target.getId()) && !active) {
            throw new ConflictException("No puedes desactivar tu propia cuenta administrativa");
        }
        if (target.isActive() != active) {
            target.setActive(active);
            if (!active) sessions.revokeAllByUserId(target.getId(), clock.instant());
            auditEvents.save(new AdminAuditEvent(actor, target, active ? "USER_ACTIVATED" : "USER_DEACTIVATED",
                    "Cambio de estado desde consola administrativa", clock.instant()));
        }
        return AdminUserResponse.from(target);
    }

    @Transactional(readOnly = true)
    public AdminOperationsResponse operations() {
        var failures = new ArrayList<OperationalFailureResponse>();
        notificationOutbox.findTop20ByStatusInOrderByNextAttemptAtAsc(FAILURE_STATUSES).forEach(item ->
                failures.add(new OperationalFailureResponse("EMAIL", item.getId(), item.getAggregateId(),
                        item.getEventType().name(), item.getAttempts(), item.getNextAttemptAt(), item.getLastError())));
        calendarOutbox.findTop20ByStatusInOrderByNextAttemptAtAsc(FAILURE_STATUSES).forEach(item ->
                failures.add(new OperationalFailureResponse("GOOGLE_CALENDAR", item.getId(), item.getAppointmentId(),
                        item.getOperation().name(), item.getAttempts(), item.getNextAttemptAt(), item.getLastError())));
        failures.sort(java.util.Comparator.comparing(OperationalFailureResponse::nextAttemptAt));
        long total = notificationOutbox.countByStatusIn(FAILURE_STATUSES)
                + calendarOutbox.countByStatusIn(FAILURE_STATUSES);
        return new AdminOperationsResponse(total, failures.stream().limit(20).toList());
    }
}
