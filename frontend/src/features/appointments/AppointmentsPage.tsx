import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/auth-context'
import type { Appointment, AppointmentStatus } from './types'

const statusLabels: Record<AppointmentStatus, string> = {
  PENDING: 'Pendiente', CONFIRMED: 'Confirmada', COMPLETED: 'Completada',
  CANCELLED: 'Cancelada', NO_SHOW: 'No asistió',
}

function appointmentDate(appointment: Appointment) {
  return new Intl.DateTimeFormat('es-CL', {
    dateStyle: 'full', timeStyle: 'short', timeZone: appointment.professionalTimeZone,
  }).format(new Date(appointment.startAt))
}

export function AppointmentsPage() {
  const { user, request, logout } = useAuth()
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    const now = new Date()
    const from = new Date(now); from.setDate(from.getDate() - 30)
    const to = new Date(now); to.setDate(to.getDate() + 335)
    const path = user?.role === 'PROFESSIONAL'
      ? `/api/v1/professional/appointments?from=${encodeURIComponent(from.toISOString())}&to=${encodeURIComponent(to.toISOString())}`
      : '/api/v1/appointments/mine'
    request<Appointment[]>(path).then((result) => { if (active) setAppointments(result) })
      .catch((caught) => { if (active) setError(caught instanceof Error ? caught.message : 'No fue posible cargar las citas') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [request, user?.role])

  async function cancelCustomer(appointment: Appointment) {
    if (!window.confirm('¿Confirmas que quieres cancelar esta cita?')) return
    try {
      const updated = await request<Appointment>(`/api/v1/appointments/${appointment.id}/cancel`, {
        method: 'POST', body: JSON.stringify({ reason: 'Cancelada por el cliente' }),
      })
      replace(updated)
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'No fue posible cancelar') }
  }

  async function updateStatus(appointment: Appointment, status: AppointmentStatus) {
    try {
      const updated = await request<Appointment>(`/api/v1/professional/appointments/${appointment.id}/status`, {
        method: 'PATCH', body: JSON.stringify({ status }),
      })
      replace(updated)
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'No fue posible actualizar la cita') }
  }

  function replace(updated: Appointment) {
    setAppointments((current) => current.map((item) => item.id === updated.id ? updated : item))
  }

  return <main className="container py-4 py-lg-5">
    <nav className="d-flex justify-content-between align-items-center mb-5"><Link className="brand text-decoration-none" to="/dashboard">Reservas</Link><button className="btn btn-outline-secondary btn-sm" onClick={() => void logout()}>Cerrar sesión</button></nav>
    <div className="d-flex justify-content-between align-items-end mb-4"><div><p className="eyebrow">HISTORIAL</p><h1 className="profile-title">{user?.role === 'PROFESSIONAL' ? 'Tus citas.' : 'Tus reservas.'}</h1></div></div>
    {error && <div className="alert alert-danger">{error}</div>}
    {loading ? <div className="spinner-border text-success"><span className="visually-hidden">Cargando</span></div>
      : appointments.length === 0 ? <div className="workspace-card"><p className="mb-0 text-secondary">Todavía no hay citas para mostrar.</p></div>
        : <div className="appointment-list">{appointments.map((appointment) => <article className="appointment-card" key={appointment.id}>
          <div><span className={`appointment-status status-${appointment.status.toLowerCase()}`}>{statusLabels[appointment.status]}</span>
            <h2>{appointment.serviceName}</h2><p className="mb-1 text-capitalize">{appointmentDate(appointment)}</p>
            <small className="text-secondary">{user?.role === 'PROFESSIONAL' ? `${appointment.customerName} · ${appointment.customerEmail}` : appointment.professionalName}</small>
            {appointment.cancellationReason && <p className="small text-secondary mt-2 mb-0">Motivo: {appointment.cancellationReason}</p>}</div>
          <div className="appointment-actions">{user?.role === 'CUSTOMER' && ['PENDING', 'CONFIRMED'].includes(appointment.status) && <button className="btn btn-sm btn-outline-danger" onClick={() => void cancelCustomer(appointment)}>Cancelar</button>}
            {user?.role === 'PROFESSIONAL' && appointment.status === 'PENDING' && <button className="btn btn-sm btn-success" onClick={() => void updateStatus(appointment, 'CONFIRMED')}>Confirmar</button>}
            {user?.role === 'PROFESSIONAL' && appointment.status === 'CONFIRMED' && <><button className="btn btn-sm btn-success" onClick={() => void updateStatus(appointment, 'COMPLETED')}>Completar</button><button className="btn btn-sm btn-outline-secondary" onClick={() => void updateStatus(appointment, 'NO_SHOW')}>No asistió</button><button className="btn btn-sm btn-outline-danger" onClick={() => void updateStatus(appointment, 'CANCELLED')}>Cancelar</button></>}
          </div>
        </article>)}</div>}
  </main>
}
