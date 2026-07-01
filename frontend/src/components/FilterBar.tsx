import type { CarFilters, FilterOptions } from '../types'
import './FilterBar.css'

interface FilterBarProps {
  options: FilterOptions | null
  filters: CarFilters
  onChange: (filters: CarFilters) => void
  onSearch: () => void
  onReset: () => void
}

function FilterBar({ options, filters, onChange, onSearch, onReset }: FilterBarProps) {
  function update<K extends keyof CarFilters>(key: K, value: CarFilters[K]) {
    onChange({ ...filters, [key]: value })
  }

  return (
    <form
      className="filter-bar"
      onSubmit={(e) => {
        e.preventDefault()
        onSearch()
      }}
    >
      <div className="filter-bar__field">
        <label htmlFor="make">Make</label>
        <select
          id="make"
          value={filters.make ?? ''}
          onChange={(e) => update('make', e.target.value || undefined)}
        >
          <option value="">Any</option>
          {options?.makes.map((make) => (
            <option key={make} value={make}>
              {make}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-bar__field">
        <label htmlFor="bodyType">Body type</label>
        <select
          id="bodyType"
          value={filters.bodyType ?? ''}
          onChange={(e) => update('bodyType', e.target.value || undefined)}
        >
          <option value="">Any</option>
          {options?.bodyTypes.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-bar__field">
        <label htmlFor="fuelType">Fuel</label>
        <select
          id="fuelType"
          value={filters.fuelType ?? ''}
          onChange={(e) => update('fuelType', e.target.value || undefined)}
        >
          <option value="">Any</option>
          {options?.fuelTypes.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-bar__field">
        <label htmlFor="transmission">Transmission</label>
        <select
          id="transmission"
          value={filters.transmission ?? ''}
          onChange={(e) => update('transmission', e.target.value || undefined)}
        >
          <option value="">Any</option>
          {options?.transmissions.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-bar__field">
        <label htmlFor="seats">Seats</label>
        <select
          id="seats"
          value={filters.seatingCapacity ?? ''}
          onChange={(e) =>
            update('seatingCapacity', e.target.value ? Number(e.target.value) : undefined)
          }
        >
          <option value="">Any</option>
          {options?.seatingCapacities.map((seats) => (
            <option key={seats} value={seats}>
              {seats}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-bar__field">
        <label htmlFor="minPrice">Min price</label>
        <input
          id="minPrice"
          type="number"
          placeholder={options ? String(options.minPrice) : undefined}
          value={filters.minPrice ?? ''}
          onChange={(e) => update('minPrice', e.target.value ? Number(e.target.value) : undefined)}
        />
      </div>

      <div className="filter-bar__field">
        <label htmlFor="maxPrice">Max price</label>
        <input
          id="maxPrice"
          type="number"
          placeholder={options ? String(options.maxPrice) : undefined}
          value={filters.maxPrice ?? ''}
          onChange={(e) => update('maxPrice', e.target.value ? Number(e.target.value) : undefined)}
        />
      </div>

      <div className="filter-bar__actions">
        <button type="submit" className="filter-bar__search">
          Search
        </button>
        <button type="button" className="filter-bar__reset" onClick={onReset}>
          Reset
        </button>
      </div>
    </form>
  )
}

export default FilterBar
