package cl.reservas.dashboard;

import cl.reservas.appointment.AppointmentResponse;
import java.time.LocalDate;
import java.util.List;

public record ProfessionalDashboardResponse(
        LocalDate date,
        String timeZone,
        long appointmentsToday,
        long newCustomersThisMonth,
        int availableMinutesToday,
        List<AppointmentResponse> upcomingAppointments
) {}
