package cl.reservas.professional;

import cl.reservas.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "professional_profiles")
public class ProfessionalProfile {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", unique = true) private User user;
    @Column(nullable = false, unique = true, length = 80) private String slug;
    @Column(length = 1000) private String bio;
    @Column(length = 30) private String phone;
    @Column(name = "time_zone", nullable = false, length = 60) private String timeZone;
    @Column(nullable = false) private boolean published;
    @ManyToMany
    @JoinTable(name = "professional_specialties",
            joinColumns = @JoinColumn(name = "professional_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    @OrderBy("name ASC")
    private Set<Specialty> specialties = new LinkedHashSet<>();
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private long version;

    protected ProfessionalProfile() {}

    public ProfessionalProfile(User user, String slug, String bio, String phone, String timeZone,
                               boolean published, Set<Specialty> specialties) {
        this.id = UUID.randomUUID();
        this.user = user;
        update(slug, bio, phone, timeZone, published, specialties);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String slug, String bio, String phone, String timeZone,
                       boolean published, Set<Specialty> specialties) {
        this.slug = slug;
        this.bio = bio;
        this.phone = phone;
        this.timeZone = timeZone;
        this.published = published;
        this.specialties.clear();
        this.specialties.addAll(specialties);
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getSlug() { return slug; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getTimeZone() { return timeZone; }
    public boolean isPublished() { return published; }
    public Set<Specialty> getSpecialties() { return Set.copyOf(specialties); }
}
