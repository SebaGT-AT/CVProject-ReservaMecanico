package cl.reservas.admin;

import cl.reservas.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_audit_events")
public class AdminAuditEvent {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actor_user_id") private User actor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "target_user_id") private User target;
    @Column(nullable = false, length = 60) private String action;
    @Column(length = 500) private String detail;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;

    protected AdminAuditEvent() {}

    public AdminAuditEvent(User actor, User target, String action, String detail, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.actor = actor;
        this.target = target;
        this.action = action;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }
}
