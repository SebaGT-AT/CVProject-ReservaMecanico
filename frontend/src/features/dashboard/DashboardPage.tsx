import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import type { Appointment } from '../appointments/types'
import { useAuth } from '../auth/auth-context'

type ProfessionalDashboard = {
  date: string
  timeZone: string
  appointmentsToday: number
  newCustomersThisMonth: number
  availableMinutesToday: number
  upcomingAppointments: Appointment[]
}

function duration(minutes: number) {
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  if (!hours) return `${rest} min`
  return rest ? `${hours} h ${rest} min` : `${hours} h`
}

function appointmentTime(appointment: Appointment) {
  return new Intl.DateTimeFormat('es-CL', {
    weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit',
    timeZone: appointment.professionalTimeZone,
  }).format(new Date(appointment.startAt))
}

export function DashboardPage() {
  const { user, request, logout } = useAuth()
  const [professional, setProfessional] = useState<ProfessionalDashboard | null>(null)
  const [customerAppointments, setCustomerAppointments] = useState<Appointment[]>([])
  const [loading, setLoading] = useState(user?.role !== 'ADMIN')
  const [error, setError] = useState('')
  const [referenceTime, setReferenceTime] = useState(0)

  useEffect(() => {
    if (!user || user.role === 'ADMIN') return
    let active = true
    const path = user.role === 'PROFESSIONAL' ? '/api/v1/professional/dashboard' : '/api/v1/appointments/mine'
    request<ProfessionalDashboard | Appointment[]>(path)
      .then((result) => {
        if (!active) return
        if (user.role === 'PROFESSIONAL') setProfessional(result as ProfessionalDashboard)
        else { setCustomerAppointments(result as Appointment[]); setReferenceTime(Date.now()) }
      })
      .catch((caught) => { if (active) setError(caught instanceof Error ? caught.message : 'No fue posible cargar el dashboard') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [request, user])

  const activeCustomerAppointments = customerAppointments.filter((item) =>
    ['PENDING', 'CONFIRMED'].includes(item.status) && new Date(item.startAt).getTime() >= referenceTime)
  const nextAppointment = [...activeCustomerAppointments].sort((a, b) => a.startAt.localeCompare(b.startAt))[0]
  const completed = customerAppointments.filter((item) => item.status === 'COMPLETED').length

  return <main className="container py-5">
    <nav className="d-flex justify-content-between align-items-center mb-5">
      <span className="brand">Reservas</span>
      <button className="btn btn-outline-secondary btn-sm" onClick={() => void logout()}>Cerrar sesión</button>
    </nav>
    <p className="eyebrow">PANEL DE CONTROL</p>
    <h1 className="display-5 mb-2">Hola, {user?.name}</h1>
    <p className="text-secondary mb-4">{user?.role === 'PROFESSIONAL' ? 'Así se ve tu jornada en este momento.' : 'Tus próximas reservas y tu historial, sin ruido.'}</p>
    {error && <div className="alert alert-warning">{error}</div>}
    {user?.role === 'PROFESSIONAL' && <div className="d-flex flex-wrap gap-2 mb-5"><Link className="btn btn-primary" to="/perfil-profesional">Perfil y servicios</Link><Link className="btn btn-outline-success" to="/configurar-agenda">Configurar agenda</Link><Link className="btn btn-outline-secondary" to="/citas">Ver citas</Link></div>}
    {user?.role === 'CUSTOMER' && <Link className="btn btn-outline-secondary mb-5" to="/citas">Ver todas mis reservas</Link>}

    {loading ? <div className="spinner-border text-success"><span className="visually-hidden">Cargando</span></div>
      : user?.role === 'PROFESSIONAL' ? <ProfessionalContent summary={professional} />
        : user?.role === 'CUSTOMER' ? <CustomerContent activeCount={activeCustomerAppointments.length} completed={completed} next={nextAppointment} />
          : <div className="workspace-card"><p className="mb-0">Panel administrativo pendiente de la etapa de operaciones.</p></div>}
  </main>
}

function ProfessionalContent({ summary }: { summary: ProfessionalDashboard | null }) {
  if (!summary) return <div className="workspace-card"><p>Completa tu perfil profesional para activar las métricas.</p><Link to="/perfil-profesional">Configurar perfil →</Link></div>
  return <>
    <div className="row g-4">
      <Metric label="Reservas de hoy" value={String(summary.appointmentsToday)} />
      <Metric label="Clientes nuevos este mes" value={String(summary.newCustomersThisMonth)} />
      <Metric label="Disponible hoy" value={duration(summary.availableMinutesToday)} compact />
    </div>
    <section className="workspace-card mt-5"><div className="d-flex justify-content-between align-items-center mb-3"><h2 className="mb-0">Próximas citas</h2><Link to="/citas">Ver agenda completa</Link></div>
      {summary.upcomingAppointments.length === 0 ? <p className="text-secondary mb-0">No hay citas próximas.</p>
        : summary.upcomingAppointments.map((appointment) => <article className="dashboard-appointment" key={appointment.id}><div><strong>{appointment.serviceName}</strong><div className="small text-secondary text-capitalize">{appointmentTime(appointment)}</div></div><span>{appointment.customerName}</span></article>)}</section>
  </>
}

function CustomerContent({ activeCount, completed, next }: { activeCount: number; completed: number; next?: Appointment }) {
  return <>
    <div className="row g-4">
      <Metric label="Reservas próximas" value={String(activeCount)} />
      <Metric label="Citas completadas" value={String(completed)} />
      <Metric label="Próxima cita" value={next ? appointmentTime(next) : 'Sin reservas'} compact />
    </div>
    {next && <section className="workspace-card mt-5"><p className="eyebrow">LO QUE SIGUE</p><h2>{next.serviceName}</h2><p className="text-capitalize mb-1">{appointmentTime(next)}</p><span className="text-secondary">con {next.professionalName}</span></section>}
  </>
}

function Metric({ label, value, compact = false }: { label: string; value: string; compact?: boolean }) {
  return <div className="col-md-4"><section className={`metric-card ${compact ? 'metric-compact' : ''}`}><span>{label}</span><strong>{value}</strong></section></div>
}
