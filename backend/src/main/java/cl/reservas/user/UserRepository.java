package cl.reservas.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByRole(Role role);
    long countByCreatedAtGreaterThanEqual(Instant from);

    @Query("""
            select u from User u
            where (:query = '' or lower(u.name) like lower(concat('%', :query, '%'))
                or lower(u.email) like lower(concat('%', :query, '%')))
              and (:role is null or u.role = :role)
            order by u.createdAt desc
            """)
    Page<User> searchForAdmin(@Param("query") String query, @Param("role") Role role, Pageable pageable);
}
