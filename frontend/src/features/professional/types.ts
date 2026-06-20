export type Specialty = { id: string; name: string; slug: string }

export type ProfessionalProfile = {
  id: string
  name: string
  slug: string
  bio: string | null
  phone: string | null
  timeZone: string
  published: boolean
  specialties: Specialty[]
}
export type ServiceOffering = {
  id: string
  name: string
  description: string | null
  durationMinutes: number
  priceAmount: number
  currency: string
  active: boolean
}

export type PublicProfessional = {
  name: string
  slug: string
  bio: string | null
  timeZone: string
  specialties: Specialty[]
  services: ServiceOffering[]
}
