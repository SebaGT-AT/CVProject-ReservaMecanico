package cl.reservas.appointment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    @EntityGraph(attributePaths = {"professional", "professional.user", "customer"})
    Optional<Appointment> findByCustomerIdAndIdempotencyKey(UUID customerId, UUID idempotencyKey);

    @EntityGraph(attributePaths = {"professional", "professional.user", "customer"})
    List<Appointment> findAllByCustomerIdOrderByStartAtDesc(UUID customerId);

    @EntityGraph(attributePaths = {"professional", "professional.user", "customer"})
    List<Appointment> findAllByProfessionalIdAndStartAtBetweenOrderByStartAtAsc(
            UUID professionalId, Instant from, Instant to);

    Optional<Appointment> findByIdAndCustomerId(UUID id, UUID customerId);
    Optional<Appointment> findByIdAndProfessionalId(UUID id, UUID professionalId);

    @Query("""
            select a from Appointment a
            where a.professional.id = :professionalId
              and a.status in :statuses
              and a.startAt < :rangeEnd and a.busyUntil > :rangeStart
            order by a.startAt
            """)
    List<Appointment> findActiveOverlappingRange(@Param("professionalId") UUID professionalId,
                                                  @Param("rangeStart") Instant rangeStart,
                                                  @Param("rangeEnd") Instant rangeEnd,
                                                  @Param("statuses") Collection<AppointmentStatus> statuses);
}
