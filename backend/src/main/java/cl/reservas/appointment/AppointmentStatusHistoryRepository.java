package cl.reservas.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, UUID> {}
