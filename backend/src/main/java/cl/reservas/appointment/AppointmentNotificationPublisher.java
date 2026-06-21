package cl.reservas.appointment;

public interface AppointmentNotificationPublisher {
    void confirmed(Appointment appointment);
    void cancelled(Appointment appointment);
    void reminder24Hours(Appointment appointment);
}
