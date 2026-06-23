import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/auth-context'
import type { Role } from '../auth/types'

type Overview = {
  generatedAt: string
  totalUsers: number
  customers: number
  professionals: number
  newUsersLast30Days: number
  appointmentsToday: number
  upcomingConfirmedAppointments: number
  cancellationsLast30Days: number
  connectedGoogleCalendars: number
  pendingOperationalFailures: number
}

type AdminUser = {
  id: string
  name: string
  email: string
  role: Role
  active: boolean
  emailVerified: boolean
  createdAt: string
}

type UserPage = { items: AdminUser[]; page: number; size: number; totalItems: number; totalPages: number }
type Failure = { channel: string; id: string; aggregateId: string; operation: string; attempts: number; nextAttemptAt: string; lastError: string | null }
type Operations = { totalFailures: number; failures: Failure[] }

export function AdminPage() {
  const { request, logout } = useAuth()
  const [overview, setOverview] = useState<Overview | null>(null)
  const [users, setUsers] = useState<UserPage | null>(null)
  const [operations, setOperations] = useState<Operations | null>(null)
  const [query, setQuery] = useState('')
  const [role, setRole] = useState<Role | ''>('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')
  const [workingId, setWorkingId] = useState('')

  const loadUsers = useCallback(async (nextPage: number, nextQuery = query, nextRole = role) => {
    const params = new URLSearchParams({ query: nextQuery, page: String(nextPage), size: '20' })
    if (nextRole) params.set('role', nextRole)
    const result = await request<UserPage>(`/api/v1/admin/users?${params}`)
    setUsers(result); setPage(result.page)
  }, [query, request, role])

  useEffect(() => {
    let active = true
    Promise.all([
      request<Overview>('/api/v1/admin/overview'),
      request<UserPage>('/api/v1/admin/users?page=0&size=20'),
      request<Operations>('/api/v1/admin/operations/failures'),
    ]).then(([summary, userPage, failures]) => {
      if (!active) return
      setOverview(summary); setUsers(userPage); setOperations(failures)
    }).catch((caught) => { if (active) setError(caught instanceof Error ? caught.message : 'No fue posible cargar la consola') })
    return () => { active = false }
  }, [request])

  async function search(event: React.FormEvent) {
    event.preventDefault(); setError('')
    try { await loadUsers(0) } catch (caught) { setError(caught instanceof Error ? caught.message : 'No fue posible buscar usuarios') }
  }

  async function toggleUser(user: AdminUser) {
    setWorkingId(user.id); setError('')
    try {
      const updated = await request<AdminUser>(`/api/v1/admin/users/${user.id}/status`, {
        method: 'PATCH', body: JSON.stringify({ active: !user.active }),
      })
      setUsers((current) => current ? { ...current, items: current.items.map((item) => item.id === updated.id ? updated : item) } : current)
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'No fue posible cambiar el estado') }
    finally { setWorkingId('') }
  }

  return <main className="container-fluid admin-shell px-3 px-lg-5 py-4">
    <nav className="d-flex justify-content-between align-items-center mb-5">
      <Link className="brand text-decoration-none" to="/dashboard">Reservas / Operaciones</Link>
      <button className="btn btn-outline-secondary btn-sm" onClick={() => void logout()}>Cerrar sesión</button>
    </nav>
    <header className="mb-5"><p className="eyebrow">CONSOLA ADMINISTRATIVA</p><h1 className="profile-title">Pulso del producto.</h1>
      <p className="text-secondary">Negocio, usuarios y fallos operativos en un solo lugar.</p></header>
    {error && <div className="alert alert-danger" role="alert">{error}</div>}

    {overview && <section aria-labelledby="business-metrics"><h2 id="business-metrics" className="h4 mb-3">Métricas de negocio</h2>
      <div className="row g-3 mb-5">
        <AdminMetric label="Usuarios" value={overview.totalUsers} detail={`${overview.newUsersLast30Days} nuevos en 30 días`} />
        <AdminMetric label="Profesionales" value={overview.professionals} detail={`${overview.connectedGoogleCalendars} con Google Calendar`} />
        <AdminMetric label="Citas de hoy" value={overview.appointmentsToday} detail={`${overview.upcomingConfirmedAppointments} confirmadas futuras`} />
        <AdminMetric label="Fallos operativos" value={overview.pendingOperationalFailures} detail={`${overview.cancellationsLast30Days} cancelaciones en 30 días`} warning={overview.pendingOperationalFailures > 0} />
      </div>
    </section>}

    <div className="row g-4">
      <section className="col-xl-8" aria-labelledby="users-title"><div className="workspace-card h-100">
        <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-4"><div><h2 id="users-title" className="mb-1">Usuarios</h2><span className="text-secondary">{users?.totalItems ?? 0} resultados</span></div>
          <form className="d-flex flex-wrap gap-2" onSubmit={search}>
            <label className="visually-hidden" htmlFor="admin-query">Buscar</label><input id="admin-query" className="form-control admin-search" placeholder="Nombre o correo" value={query} onChange={(event) => setQuery(event.target.value)} />
            <label className="visually-hidden" htmlFor="admin-role">Rol</label><select id="admin-role" className="form-select admin-role" value={role} onChange={(event) => setRole(event.target.value as Role | '')}>
              <option value="">Todos</option><option value="CUSTOMER">Clientes</option><option value="PROFESSIONAL">Profesionales</option><option value="ADMIN">Administradores</option>
            </select><button className="btn btn-primary">Buscar</button>
          </form></div>
        <div className="table-responsive"><table className="table align-middle"><thead><tr><th>Usuario</th><th>Rol</th><th>Verificación</th><th>Estado</th><th><span className="visually-hidden">Acciones</span></th></tr></thead>
          <tbody>{users?.items.map((user) => <tr key={user.id}><td><strong>{user.name}</strong><div className="small text-secondary">{user.email}</div></td><td>{roleLabel(user.role)}</td>
            <td>{user.emailVerified ? 'Verificado' : 'Pendiente'}</td><td><span className={`badge ${user.active ? 'text-bg-success' : 'text-bg-secondary'}`}>{user.active ? 'Activo' : 'Inactivo'}</span></td>
            <td className="text-end"><button className={`btn btn-sm ${user.active ? 'btn-outline-danger' : 'btn-outline-success'}`} disabled={workingId === user.id} onClick={() => void toggleUser(user)}>{user.active ? 'Desactivar' : 'Activar'}</button></td></tr>)}</tbody></table></div>
        {users && users.totalPages > 1 && <div className="d-flex justify-content-between align-items-center mt-3"><button className="btn btn-sm btn-outline-secondary" disabled={page === 0} onClick={() => void loadUsers(page - 1)}>Anterior</button><span className="small">Página {page + 1} de {users.totalPages}</span><button className="btn btn-sm btn-outline-secondary" disabled={page + 1 >= users.totalPages} onClick={() => void loadUsers(page + 1)}>Siguiente</button></div>}
      </div></section>

      <section className="col-xl-4" aria-labelledby="failures-title"><div className="workspace-card h-100"><h2 id="failures-title">Fallos operativos</h2>
        {!operations?.failures.length ? <p className="text-secondary mb-0">No hay entregas fallidas.</p> : <div className="operation-list">{operations.failures.map((failure) => <article className="operation-item" key={`${failure.channel}-${failure.id}`}>
          <div className="d-flex justify-content-between gap-2"><strong>{failure.channel === 'EMAIL' ? 'Correo' : 'Google Calendar'}</strong><span className="badge text-bg-warning">{failure.attempts} intentos</span></div>
          <div className="small mt-2">{failure.operation}</div><div className="small text-secondary text-break">{failure.lastError ?? 'Error sin detalle'}</div><code className="small">{failure.aggregateId}</code>
        </article>)}</div>}
      </div></section>
    </div>
  </main>
}

function AdminMetric({ label, value, detail, warning = false }: { label: string; value: number; detail: string; warning?: boolean }) {
  return <div className="col-sm-6 col-xl-3"><article className={`admin-metric ${warning ? 'admin-metric-warning' : ''}`}><span>{label}</span><strong>{value}</strong><small>{detail}</small></article></div>
}

function roleLabel(role: Role) {
  return role === 'CUSTOMER' ? 'Cliente' : role === 'PROFESSIONAL' ? 'Profesional' : 'Administrador'
}
