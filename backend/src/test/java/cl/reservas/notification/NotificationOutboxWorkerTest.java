package cl.reservas.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxWorkerTest {
    @Mock NotificationOutboxStateService state;
    @Mock AppointmentEmailSender sender;

    @Test
    void successfulDeliveryMarksMessageAsSent() throws Exception {
        UUID id = UUID.randomUUID();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String payload = mapper.writeValueAsString(payload());
        when(state.claimBatch()).thenReturn(List.of(new OutboxMessage(
                id, NotificationEventType.APPOINTMENT_CONFIRMED, payload, 0)));

        new NotificationOutboxWorker(state, sender, mapper).dispatch();

        verify(sender).send(eq(NotificationEventType.APPOINTMENT_CONFIRMED), any(AppointmentNotificationPayload.class));
        verify(state).markSent(id);
        verify(state, never()).markFailed(any(), anyInt(), any());
    }

    @Test
    void failedDeliveryIsScheduledForRetry() throws Exception {
        UUID id = UUID.randomUUID();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String payload = mapper.writeValueAsString(payload());
        when(state.claimBatch()).thenReturn(List.of(new OutboxMessage(
                id, NotificationEventType.APPOINTMENT_REMINDER_24H, payload, 2)));
        doThrow(new IllegalStateException("SMTP caido")).when(sender)
                .send(eq(NotificationEventType.APPOINTMENT_REMINDER_24H), any());

        new NotificationOutboxWorker(state, sender, mapper).dispatch();

        verify(state).markFailed(eq(id), eq(2), any(IllegalStateException.class));
        verify(state, never()).markSent(any());
    }

    private AppointmentNotificationPayload payload() {
        return new AppointmentNotificationPayload(UUID.randomUUID(), NotificationRecipient.CUSTOMER,
                "Grace", "grace@example.com", "Grace", "Ada", "Consulta", Instant.parse("2026-06-22T13:00:00Z"),
                "America/Santiago", null);
    }
}
