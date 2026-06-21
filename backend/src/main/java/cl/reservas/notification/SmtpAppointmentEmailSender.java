package cl.reservas.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "app.mail.delivery", havingValue = "smtp")
public class SmtpAppointmentEmailSender implements AppointmentEmailSender {
    private final JavaMailSender mailSender;
    private final String from;

    public SmtpAppointmentEmailSender(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(NotificationEventType eventType, AppointmentNotificationPayload payload) {
        String date = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
                .withLocale(Locale.forLanguageTag("es-CL"))
                .format(payload.startAt().atZone(ZoneId.of(payload.professionalTimeZone())));
        switch (eventType) {
            case APPOINTMENT_CONFIRMED -> send(payload.recipientEmail(),
                    payload.recipient() == NotificationRecipient.CUSTOMER ? "Reserva confirmada" : "Nueva reserva",
                    payload.recipient() == NotificationRecipient.CUSTOMER
                            ? "Hola " + payload.recipientName() + ",\n\nTu cita de " + payload.serviceName()
                                    + " con " + payload.professionalName() + " está confirmada para " + date + "."
                            : "Tienes una nueva cita de " + payload.serviceName() + " con " + payload.customerName()
                                    + " para " + date + ".");
            case APPOINTMENT_CANCELLED -> {
                String reason = payload.cancellationReason() == null ? "Sin motivo informado" : payload.cancellationReason();
                send(payload.recipientEmail(), payload.recipient() == NotificationRecipient.CUSTOMER
                                ? "Reserva cancelada" : "Cita cancelada",
                        payload.recipient() == NotificationRecipient.CUSTOMER
                                ? "La cita de " + payload.serviceName() + " para " + date + " fue cancelada.\nMotivo: " + reason
                                : "La cita con " + payload.customerName() + " para " + date + " fue cancelada.\nMotivo: " + reason);
            }
            case APPOINTMENT_REMINDER_24H -> send(payload.recipientEmail(), "Recordatorio de tu reserva",
                    "Hola " + payload.recipientName() + ",\n\nTe recordamos tu cita de " + payload.serviceName()
                            + " con " + payload.professionalName() + " para " + date + ".");
        }
    }

    private void send(String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
