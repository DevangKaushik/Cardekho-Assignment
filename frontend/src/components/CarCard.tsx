import type { Car } from '../types'
import './CarCard.css'

interface CarCardProps {
  car: Car
  compact?: boolean
}

function CarCard({ car, compact }: CarCardProps) {
  const specs = [
    car.fuelType,
    car.transmission,
    car.seatingCapacity ? `${car.seatingCapacity} seats` : null,
    car.cityMileage ? `${car.cityMileage} kmpl` : null,
  ].filter(Boolean)

  return (
    <article className={`car-card${compact ? ' car-card--compact' : ''}`}>
      <header className="car-card__header">
        <h3>
          {car.make} {car.model}
        </h3>
        <span className="car-card__price">{car.priceDisplay ?? '—'}</span>
      </header>
      {car.variant && <p className="car-card__variant">{car.variant}</p>}
      {specs.length > 0 && (
        <ul className="car-card__specs">
          {specs.map((spec) => (
            <li key={spec}>{spec}</li>
          ))}
        </ul>
      )}
    </article>
  )
}

export default CarCard
