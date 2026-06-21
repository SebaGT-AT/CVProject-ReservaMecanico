package cl.reservas.notification;

import cl.reservas.appointment.Appointment;
import cl.reservas.professional.ProfessionalProfile;
import cl.reservas.professional.ServiceOffering;
import cl.reservas.user.Role;
import cl.reservas.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OutboxAppointmentNotificationPublisherTest {
    @Mock NotificationOutboxRepository outbox;

    @Test
    void confirmationIsSerializedWithDeterministicDeduplicationKey() throws Exception {
        Instant now = Instant.parse("2026-06-21T12:00:00Z");
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var publisher = new OutboxAppointmentNotificationPublisher(outbox, mapper,
                Clock.fixed(now, ZoneOffset.UTC));
        Appointment appointment = appointment();
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        publisher.confirmed(appointment);

        verify(outbox, times(2)).insertEvent(any(UUID.class), eq(appointment.getId()),
                eq(NotificationEventType.APPOINTMENT_CONFIRMED.name()), keyCaptor.capture(),
                payloadCaptor.capture(), eq(now));
        assertThat(keyCaptor.getAllValues()).containsExactly(
                "appointment:" + appointment.getId() + ":confirmed:customer",
                "appointment:" + appointment.getId() + ":confirmed:professional");
        AppointmentNotificationPayload customerPayload = mapper.readValue(
                payloadCaptor.getAllValues().getFirst(), AppointmentNotificationPayload.class);
        AppointmentNotificationPayload professionalPayload = mapper.readValue(
                payloadCaptor.getAllValues().getLast(), AppointmentNotificationPayload.class);
        assertThat(customerPayload.recipientEmail()).isEqualTo("grace@example.com");
        assertThat(customerPayload.recipient()).isEqualTo(NotificationRecipient.CUSTOMER);
        assertThat(professionalPayload.recipientEmail()).isEqualTo("ada@example.com");
        assertThat(professionalPayload.recipient()).isEqualTo(NotificationRecipient.PROFESSIONAL);
    }

    private Appointment appointment() {
        User customer = new User("Grace", "grace@example.com", "encoded", Role.CUSTOMER);
        User professionalUser = new User("Ada", "ada@example.com", "encoded", Role.PROFESSIONAL);
        ProfessionalProfile profile = new ProfessionalProfile(professionalUser, "ada", null, null,
                "America/Santiago", true, Set.of());
        ServiceOffering service = new ServiceOffering(profile, "Consulta", null, 60,
                new BigDecimal("25000"), "CLP", true);
        Instant start = Instant.parse("2026-06-22T13:00:00Z");
        return new Appointment(profile, customer, service, UUID.randomUUID(), start,
                start.plusSeconds(3600), start.plusSeconds(3600));
    }
}
