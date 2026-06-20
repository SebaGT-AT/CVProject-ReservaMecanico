package cl.reservas.professional;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_offerings")
public class ServiceOffering {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "professional_id") private ProfessionalProfile professional;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 500) private String description;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(name = "price_amount", nullable = false, precision = 12, scale = 2) private BigDecimal priceAmount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private long version;

    protected ServiceOffering() {}

    public ServiceOffering(ProfessionalProfile professional, String name, String description,
                           int durationMinutes, BigDecimal priceAmount, String currency, boolean active) {
        this.id = UUID.randomUUID();
        this.professional = professional;
        this.createdAt = Instant.now();
        update(name, description, durationMinutes, priceAmount, currency, active);
    }

    public void update(String name, String description, int durationMinutes,
                       BigDecimal priceAmount, String currency, boolean active) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public void deactivate() { this.active = false; this.updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public ProfessionalProfile getProfessional() { return professional; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
    public boolean isActive() { return active; }
}
