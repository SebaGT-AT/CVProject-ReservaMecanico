package cl.reservas.notification;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    DEAD
}
