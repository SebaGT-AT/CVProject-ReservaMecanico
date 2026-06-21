package cl.reservas.dashboard;

import cl.reservas.appointment.*;
import cl.reservas.common.exception.NotFoundException;
import cl.reservas.professional.ProfessionalProfile;
import cl.reservas.professional.ProfessionalProfileRepository;
import cl.reservas.scheduling.AvailabilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.EnumSet;
import java.util.List;

@Service
public class DashboardService {
    private static final EnumSet<AppointmentStatus> ACTIVE_STATUSES =
            EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
    private static final EnumSet<AppointmentStatus> TODAY_STATUSES =
            EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED,
                    AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW);
    private final ProfessionalProfileRepository profiles;
    private final AppointmentRepository appointments;
    private final AvailabilityService availability;
    private final Clock clock;

    public DashboardService(ProfessionalProfileRepository profiles, AppointmentRepository appointments,
                            AvailabilityService availability, Clock clock) {
        this.profiles = profiles;
        this.appointments = appointments;
        this.availability = availability;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProfessionalDashboardResponse professionalSummary(String email) {
        ProfessionalProfile professional = profiles.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Completa primero tu perfil profesional"));
        ZoneId zone = ZoneId.of(professional.getTimeZone());
        ZonedDateTime localNow = clock.instant().atZone(zone);
        LocalDate today = localNow.toLocalDate();
        Instant dayStart = today.atStartOfDay(zone).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant();
        LocalDate monthStartDate = today.withDayOfMonth(1);
        Instant monthStart = monthStartDate.atStartOfDay(zone).toInstant();
        Instant nextMonthStart = monthStartDate.plusMonths(1).atStartOfDay(zone).toInstant();

        long todayCount = appointments.countByProfessionalIdAndStatusInAndStartAtGreaterThanEqualAndStartAtLessThan(
                professional.getId(), TODAY_STATUSES, dayStart, dayEnd);
        long newCustomers = appointments.countNewCustomers(professional.getId(), monthStart, nextMonthStart);
        int freeMinutes = availability.remainingFreeMinutes(professional, today);
        List<AppointmentResponse> upcoming = appointments
                .findTop5ByProfessionalIdAndStatusInAndStartAtGreaterThanEqualOrderByStartAtAsc(
                        professional.getId(), ACTIVE_STATUSES, clock.instant()).stream()
                .map(AppointmentResponse::from).toList();
        return new ProfessionalDashboardResponse(today, zone.getId(), todayCount, newCustomers, freeMinutes, upcoming);
    }
}
