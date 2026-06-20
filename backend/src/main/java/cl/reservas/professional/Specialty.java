package cl.reservas.professional;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "specialties")
public class Specialty {
    @Id private UUID id;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, unique = true, length = 100) private String slug;
    @Column(nullable = false) private boolean active;

    protected Specialty() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public boolean isActive() { return active; }
}
