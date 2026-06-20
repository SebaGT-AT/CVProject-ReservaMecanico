export type ApiProblem = {
  title?: string
  detail?: string
  errors?: Record<string, string>
}

export class ApiError extends Error {
  constructor(public readonly status: number, message: string, public readonly problem: ApiProblem) {
    super(message)
    this.name = 'ApiError'
  }
}

const API_URL = import.meta.env.VITE_API_URL ?? ''

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...options.headers },
  })

  if (!response.ok) {
    const problem = (await response.json().catch(() => ({}))) as ApiProblem
    throw new ApiError(response.status, problem.detail ?? 'No fue posible completar la solicitud', problem)
  }

  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
