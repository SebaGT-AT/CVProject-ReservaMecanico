package cl.reservas.dashboard;

import cl.reservas.appointment.AppointmentRepository;
import cl.reservas.appointment.AppointmentStatus;
import cl.reservas.professional.ProfessionalProfile;
import cl.reservas.professional.ProfessionalProfileRepository;
import cl.reservas.scheduling.AvailabilityService;
import cl.reservas.user.Role;
import cl.reservas.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock ProfessionalProfileRepository profiles;
    @Mock AppointmentRepository appointments;
    @Mock AvailabilityService availability;

    @Test
    void calculatesMetricsUsingProfessionalTimeZoneBoundaries() {
        Instant now = Instant.parse("2026-06-22T15:00:00Z");
        User user = new User("Ada", "ada@example.com", "encoded", Role.PROFESSIONAL);
        ProfessionalProfile profile = new ProfessionalProfile(user, "ada", null, null,
                "America/Santiago", true, Set.of());
        EnumSet<AppointmentStatus> active = EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
        EnumSet<AppointmentStatus> todayStatuses = EnumSet.of(AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW);
        Instant dayStart = Instant.parse("2026-06-22T04:00:00Z");
        Instant dayEnd = Instant.parse("2026-06-23T04:00:00Z");
        Instant monthStart = Instant.parse("2026-06-01T04:00:00Z");
        Instant nextMonth = Instant.parse("2026-07-01T04:00:00Z");
        when(profiles.findByUserEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(profile));
        when(appointments.countByProfessionalIdAndStatusInAndStartAtGreaterThanEqualAndStartAtLessThan(
                profile.getId(), todayStatuses, dayStart, dayEnd)).thenReturn(4L);
        when(appointments.countNewCustomers(profile.getId(), monthStart, nextMonth)).thenReturn(3L);
        when(availability.remainingFreeMinutes(profile, java.time.LocalDate.of(2026, 6, 22))).thenReturn(150);
        when(appointments.findTop5ByProfessionalIdAndStatusInAndStartAtGreaterThanEqualOrderByStartAtAsc(
                profile.getId(), active, now)).thenReturn(List.of());
        var service = new DashboardService(profiles, appointments, availability,
                Clock.fixed(now, ZoneOffset.UTC));

        ProfessionalDashboardResponse response = service.professionalSummary("ada@example.com");

        assertThat(response.appointmentsToday()).isEqualTo(4);
        assertThat(response.newCustomersThisMonth()).isEqualTo(3);
        assertThat(response.availableMinutesToday()).isEqualTo(150);
        assertThat(response.timeZone()).isEqualTo("America/Santiago");
    }
}
