export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'

export type Appointment = {
  id: string
  status: AppointmentStatus
  startAt: string
  endAt: string
  professionalTimeZone: string
  serviceId: string
  serviceName: string
  durationMinutes: number
  priceAmount: number
  currency: string
  professionalName: string
  professionalSlug: string
  customerName: string
  customerEmail: string
  cancellationReason: string | null
  cancelledAt: string | null
}
