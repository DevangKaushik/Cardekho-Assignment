import type { Car, CarFilters, ChatResponse, FilterOptions } from './types'

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`Request failed: ${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<T>
}

export function fetchFilterOptions(): Promise<FilterOptions> {
  return fetch(`${BASE_URL}/api/cars/filters`).then((res) => handle<FilterOptions>(res))
}

export function fetchCars(filters: CarFilters): Promise<Car[]> {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      params.set(key, String(value))
    }
  })
  const query = params.toString()
  return fetch(`${BASE_URL}/api/cars${query ? `?${query}` : ''}`).then((res) => handle<Car[]>(res))
}

export function sendChatMessage(message: string): Promise<ChatResponse> {
  return fetch(`${BASE_URL}/api/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message }),
  }).then((res) => handle<ChatResponse>(res))
}
