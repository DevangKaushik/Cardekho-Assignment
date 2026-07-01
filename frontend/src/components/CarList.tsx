import { useEffect, useState } from 'react'
import type { Car } from '../types'
import CarCard from './CarCard'
import './CarList.css'

const PAGE_SIZE = 12

interface CarListProps {
  cars: Car[]
  loading: boolean
  error: string | null
}

function CarList({ cars, loading, error }: CarListProps) {
  const [visible, setVisible] = useState(PAGE_SIZE)

  useEffect(() => {
    setVisible(PAGE_SIZE)
  }, [cars])

  if (loading) {
    return <p className="car-list__status">Loading cars…</p>
  }

  if (error) {
    return <p className="car-list__status car-list__status--error">{error}</p>
  }

  if (cars.length === 0) {
    return <p className="car-list__status">No cars match those filters. Try widening your search.</p>
  }

  const shown = cars.slice(0, visible)

  return (
    <div>
      <p className="car-list__count">
        {cars.length} car{cars.length === 1 ? '' : 's'} found
      </p>
      <div className="car-list">
        {shown.map((car, idx) => (
          <CarCard key={`${car.make}-${car.model}-${car.variant}-${idx}`} car={car} />
        ))}
      </div>
      {visible < cars.length && (
        <button className="car-list__more" onClick={() => setVisible((v) => v + PAGE_SIZE)}>
          Load more
        </button>
      )}
    </div>
  )
}

export default CarList
