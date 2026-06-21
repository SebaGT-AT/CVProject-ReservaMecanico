package cl.reservas.notification;

import cl.reservas.appointment.AppointmentNotificationPublisher;
import cl.reservas.appointment.AppointmentRepository;
import cl.reservas.appointment.AppointmentStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class AppointmentReminderScheduler {
    private final AppointmentRepository appointments;
    private final AppointmentNotificationPublisher notifications;
    private final Clock clock;

    public AppointmentReminderScheduler(AppointmentRepository appointments,
                                        AppointmentNotificationPublisher notifications, Clock clock) {
        this.appointments = appointments;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.notifications.reminder-cron:0 */15 * * * *}")
    @Transactional
    public void enqueueUpcomingReminders() {
        Instant now = clock.instant();
        Instant from = now.plus(Duration.ofHours(23));
        Instant to = now.plus(Duration.ofHours(25));
        appointments.findAllByStatusAndStartAtBetweenOrderByStartAt(AppointmentStatus.CONFIRMED, from, to)
                .forEach(notifications::reminder24Hours);
    }
}
