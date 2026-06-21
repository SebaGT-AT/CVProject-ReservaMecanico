package cl.reservas.notification;

public interface AppointmentEmailSender {
    void send(NotificationEventType eventType, AppointmentNotificationPayload payload);
}
