package cl.reservas.appointment;

import cl.reservas.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment_status_history")
public class AppointmentStatusHistory {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    @Enumerated(EnumType.STRING) @Column(name = "from_status", length = 20) private AppointmentStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(name = "to_status", nullable = false, length = 20) private AppointmentStatus toStatus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actor_user_id") private User actor;
    @Column(length = 500) private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AppointmentStatusHistory() {}

    public AppointmentStatusHistory(Appointment appointment, AppointmentStatus fromStatus,
                                    AppointmentStatus toStatus, User actor, String reason) {
        this.id = UUID.randomUUID();
        this.appointment = appointment;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actor = actor;
        this.reason = reason;
        this.createdAt = Instant.now();
    }
}
