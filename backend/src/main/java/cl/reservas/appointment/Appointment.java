package cl.reservas.appointment;

import cl.reservas.professional.ProfessionalProfile;
import cl.reservas.professional.ServiceOffering;
import cl.reservas.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "professional_id")
    private ProfessionalProfile professional;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "customer_id")
    private User customer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_id")
    private ServiceOffering service;
    @Column(name = "idempotency_key", nullable = false) private UUID idempotencyKey;
    @Column(name = "start_at", nullable = false) private Instant startAt;
    @Column(name = "end_at", nullable = false) private Instant endAt;
    @Column(name = "busy_until", nullable = false) private Instant busyUntil;
    @Column(name = "professional_time_zone", nullable = false, length = 60) private String professionalTimeZone;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AppointmentStatus status;
    @Column(name = "service_name", nullable = false, length = 120) private String serviceName;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(name = "price_amount", nullable = false, precision = 12, scale = 2) private BigDecimal priceAmount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(name = "cancellation_reason", length = 500) private String cancellationReason;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cancelled_by") private User cancelledBy;
    @Column(name = "cancelled_at") private Instant cancelledAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private long version;

    protected Appointment() {}

    public Appointment(ProfessionalProfile professional, User customer, ServiceOffering service,
                       UUID idempotencyKey, Instant startAt, Instant endAt, Instant busyUntil) {
        this.id = UUID.randomUUID();
        this.professional = professional;
        this.customer = customer;
        this.service = service;
        this.idempotencyKey = idempotencyKey;
        this.startAt = startAt;
        this.endAt = endAt;
        this.busyUntil = busyUntil;
        this.professionalTimeZone = professional.getTimeZone();
        this.status = AppointmentStatus.CONFIRMED;
        this.serviceName = service.getName();
        this.durationMinutes = service.getDurationMinutes();
        this.priceAmount = service.getPriceAmount();
        this.currency = service.getCurrency();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void transitionTo(AppointmentStatus next) {
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public void cancel(User actor, String reason) {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledBy = actor;
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();
        this.updatedAt = this.cancelledAt;
    }

    public UUID getId() { return id; }
    public ProfessionalProfile getProfessional() { return professional; }
    public User getCustomer() { return customer; }
    public ServiceOffering getService() { return service; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public Instant getBusyUntil() { return busyUntil; }
    public String getProfessionalTimeZone() { return professionalTimeZone; }
    public AppointmentStatus getStatus() { return status; }
    public String getServiceName() { return serviceName; }
    public int getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
    public String getCancellationReason() { return cancellationReason; }
    public Instant getCancelledAt() { return cancelledAt; }
}
