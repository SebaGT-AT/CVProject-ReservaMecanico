import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../../lib/api'
import type { PublicProfessional } from './types'

export function PublicProfessionalPage() {
  const { slug = '' } = useParams()
  const [professional, setProfessional] = useState<PublicProfessional | null>(null)
  const [error, setError] = useState('')
  useEffect(() => {
    api<PublicProfessional>(`/api/v1/professionals/${encodeURIComponent(slug)}`)
      .then(setProfessional)
      .catch((caught) => setError(caught instanceof Error ? caught.message : 'Perfil no disponible'))
  }, [slug])

  if (error) return <main className="container py-5"><Link className="brand text-decoration-none" to="/">Reservas</Link><div className="alert alert-warning mt-5">{error}</div></main>
  if (!professional) return <main className="min-vh-100 d-flex align-items-center justify-content-center"><div className="spinner-border text-success"><span className="visually-hidden">Cargando</span></div></main>

  return <main>
    <header className="public-profile-hero"><div className="container py-5">
      <Link className="brand text-decoration-none text-white" to="/">Reservas</Link>
      <div className="public-profile-heading"><div className="d-flex flex-wrap gap-2 mb-3">{professional.specialties.map((item) => <span className="public-tag" key={item.id}>{item.name}</span>)}</div>
        <h1>{professional.name}</h1><p>{professional.bio || 'Profesional independiente disponible para reservas.'}</p></div>
    </div></header>
    <section className="container py-5"><div className="row"><div className="col-lg-8"><p className="eyebrow">SERVICIOS</p>
      {professional.services.map((service) => <article className="public-service" key={service.id}><div><h2>{service.name}</h2><p>{service.description}</p><small>{service.durationMinutes} minutos</small></div>
        <strong>{new Intl.NumberFormat('es-CL', { style: 'currency', currency: service.currency }).format(service.priceAmount)}</strong></article>)}</div>
      <aside className="col-lg-4"><div className="coming-booking"><strong>Agenda en construcción</strong><p className="mb-0">Pronto podrás elegir un horario y reservar desde aquí.</p></div></aside></div></section>
  </main>
}
