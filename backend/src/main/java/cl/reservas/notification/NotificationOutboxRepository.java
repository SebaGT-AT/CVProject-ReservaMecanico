package cl.reservas.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {
    boolean existsByDeduplicationKey(String deduplicationKey);

    @Modifying
    @Query(value = """
            INSERT INTO notification_outbox (
                id, aggregate_type, aggregate_id, event_type, deduplication_key, payload,
                status, attempts, next_attempt_at, created_at, updated_at
            ) VALUES (
                :id, 'APPOINTMENT', :aggregateId, :eventType, :deduplicationKey, CAST(:payload AS jsonb),
                'PENDING', 0, :now, :now, :now
            ) ON CONFLICT (deduplication_key) DO NOTHING
            """, nativeQuery = true)
    int insertEvent(@Param("id") UUID id,
                    @Param("aggregateId") UUID aggregateId,
                    @Param("eventType") String eventType,
                    @Param("deduplicationKey") String deduplicationKey,
                    @Param("payload") String payload,
                    @Param("now") Instant now);

    @Query(value = """
            SELECT * FROM notification_outbox
            WHERE (status IN ('PENDING', 'FAILED') AND next_attempt_at <= :now)
               OR (status = 'PROCESSING' AND locked_at < :staleBefore)
            ORDER BY created_at
            FOR UPDATE SKIP LOCKED
            LIMIT :batchSize
            """, nativeQuery = true)
    List<NotificationOutbox> findClaimable(@Param("now") Instant now,
                                            @Param("staleBefore") Instant staleBefore,
                                            @Param("batchSize") int batchSize);
}
