package cl.reservas.admin;

import cl.reservas.appointment.AppointmentRepository;
import cl.reservas.appointment.AppointmentStatus;
import cl.reservas.common.exception.ConflictException;
import cl.reservas.auth.RefreshSessionRepository;
import cl.reservas.integration.googlecalendar.CalendarSyncOutboxRepository;
import cl.reservas.integration.googlecalendar.GoogleCalendarConnectionRepository;
import cl.reservas.integration.googlecalendar.GoogleConnectionStatus;
import cl.reservas.notification.NotificationOutboxRepository;
import cl.reservas.notification.OutboxStatus;
import cl.reservas.user.Role;
import cl.reservas.user.User;
import cl.reservas.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock UserRepository users;
    @Mock AppointmentRepository appointments;
    @Mock NotificationOutboxRepository notificationOutbox;
    @Mock CalendarSyncOutboxRepository calendarOutbox;
    @Mock GoogleCalendarConnectionRepository googleConnections;
    @Mock AdminAuditEventRepository auditEvents;
    @Mock RefreshSessionRepository sessions;
    private AdminService service;
    private final Instant now = Instant.parse("2026-06-23T12:00:00Z");

    @BeforeEach
    void setUp() {
        service = new AdminService(users, appointments, notificationOutbox, calendarOutbox,
                googleConnections, auditEvents, sessions, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void buildsPlatformOverviewWithoutMixingCurrencies() {
        when(users.count()).thenReturn(120L);
        when(users.countByRole(Role.CUSTOMER)).thenReturn(90L);
        when(users.countByRole(Role.PROFESSIONAL)).thenReturn(28L);
        when(users.countByCreatedAtGreaterThanEqual(any())).thenReturn(12L);
        when(appointments.countByStartAtGreaterThanEqualAndStartAtLessThanAndStatusNot(
                any(), any(), eq(AppointmentStatus.CANCELLED))).thenReturn(8L);
        when(appointments.countByStatusAndStartAtGreaterThanEqual(AppointmentStatus.CONFIRMED, now)).thenReturn(35L);
        when(appointments.countByStatusAndUpdatedAtGreaterThanEqual(eq(AppointmentStatus.CANCELLED), any())).thenReturn(4L);
        when(googleConnections.countByStatus(GoogleConnectionStatus.CONNECTED)).thenReturn(10L);
        when(notificationOutbox.countByStatusIn(anyCollection())).thenReturn(2L);
        when(calendarOutbox.countByStatusIn(anyCollection())).thenReturn(1L);

        AdminOverviewResponse overview = service.overview();

        assertThat(overview.totalUsers()).isEqualTo(120);
        assertThat(overview.appointmentsToday()).isEqualTo(8);
        assertThat(overview.pendingOperationalFailures()).isEqualTo(3);
    }

    @Test
    void administratorCannotDeactivateOwnAccount() {
        User admin = new User("Admin", "admin@example.com", "hash", Role.ADMIN);
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(users.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateStatus(admin.getEmail(), admin.getId(), false))
                .isInstanceOf(ConflictException.class).hasMessageContaining("propia cuenta");
        verifyNoInteractions(auditEvents);
    }

    @Test
    void statusChangeCreatesAuditEvent() {
        User admin = new User("Admin", "admin@example.com", "hash", Role.ADMIN);
        User target = new User("Cliente", "client@example.com", "hash", Role.CUSTOMER);
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(users.findById(target.getId())).thenReturn(Optional.of(target));

        AdminUserResponse response = service.updateStatus(admin.getEmail(), target.getId(), false);

        assertThat(response.active()).isFalse();
        verify(auditEvents).save(any(AdminAuditEvent.class));
        verify(sessions).revokeAllByUserId(target.getId(), now);
    }
}
