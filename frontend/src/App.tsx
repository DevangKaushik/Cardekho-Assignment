import { useEffect, useState } from 'react'
import { fetchCars, fetchFilterOptions } from './api'
import FilterBar from './components/FilterBar'
import CarList from './components/CarList'
import ChatWidget from './components/ChatWidget'
import type { Car, CarFilters, FilterOptions } from './types'
import './App.css'

function App() {
  const [options, setOptions] = useState<FilterOptions | null>(null)
  const [filters, setFilters] = useState<CarFilters>({})
  const [cars, setCars] = useState<Car[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchFilterOptions()
      .then(setOptions)
      .catch(() => setError('Could not load filter options from the server.'))

    fetchCars({})
      .then(setCars)
      .catch(() => setError('Could not load cars from the server.'))
      .finally(() => setLoading(false))
  }, [])

  function runSearch() {
    setLoading(true)
    setError(null)
    fetchCars(filters)
      .then(setCars)
      .catch(() => setError('Could not load cars from the server.'))
      .finally(() => setLoading(false))
  }

  function resetSearch() {
    setFilters({})
    setLoading(true)
    setError(null)
    fetchCars({})
      .then(setCars)
      .catch(() => setError('Could not load cars from the server.'))
      .finally(() => setLoading(false))
  }

  return (
    <div className="app">
      <header className="app__header">
        <h1>CarFinder</h1>
        <p>Browse by filters, or ask the assistant for a recommendation.</p>
      </header>

      <main className="app__main">
        <FilterBar
          options={options}
          filters={filters}
          onChange={setFilters}
          onSearch={runSearch}
          onReset={resetSearch}
        />
        <CarList cars={cars} loading={loading} error={error} />
      </main>

      <ChatWidget />
    </div>
  )
}

export default App
