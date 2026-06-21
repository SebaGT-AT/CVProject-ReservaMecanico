package cl.reservas.notification;

import cl.reservas.appointment.Appointment;
import cl.reservas.appointment.AppointmentNotificationPublisher;
import cl.reservas.appointment.AppointmentRepository;
import cl.reservas.appointment.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderSchedulerTest {
    @Mock AppointmentRepository appointments;
    @Mock AppointmentNotificationPublisher notifications;

    @Test
    void enqueuesConfirmedAppointmentsInsideReminderWindow() {
        Instant now = Instant.parse("2026-06-21T12:00:00Z");
        Appointment appointment = mock(Appointment.class);
        when(appointments.findAllByStatusAndStartAtBetweenOrderByStartAt(
                AppointmentStatus.CONFIRMED, now.plusSeconds(23 * 3600), now.plusSeconds(25 * 3600)))
                .thenReturn(List.of(appointment));
        var scheduler = new AppointmentReminderScheduler(appointments, notifications,
                Clock.fixed(now, ZoneOffset.UTC));

        scheduler.enqueueUpcomingReminders();

        verify(notifications).reminder24Hours(appointment);
    }
}
