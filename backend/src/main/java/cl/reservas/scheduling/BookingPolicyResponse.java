package cl.reservas.scheduling;

public record BookingPolicyResponse(
        int minimumNoticeMinutes,
        int bookingWindowDays,
        int slotIntervalMinutes,
        int bufferAfterMinutes,
        int cancellationNoticeMinutes
) {
    static BookingPolicyResponse from(BookingPolicy policy) {
        return new BookingPolicyResponse(policy.getMinimumNoticeMinutes(), policy.getBookingWindowDays(),
                policy.getSlotIntervalMinutes(), policy.getBufferAfterMinutes(), policy.getCancellationNoticeMinutes());
    }
}
