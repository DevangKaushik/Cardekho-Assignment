export interface Car {
  make: string | null
  model: string | null
  variant: string | null
  price: number | null
  priceDisplay: string | null
  bodyType: string | null
  fuelType: string | null
  transmission: string | null
  seatingCapacity: number | null
  doors: number | null
  drivetrain: string | null
  displacementCc: number | null
  cylinders: number | null
  gears: number | null
  powerBhp: number | null
  powerRpm: number | null
  torqueNm: number | null
  torqueRpm: number | null
  cityMileage: number | null
  highwayMileage: number | null
  araiMileage: number | null
  araiMileageCng: number | null
  fuelTankCapacity: number | null
  length: number | null
  width: number | null
  height: number | null
  wheelbase: number | null
  groundClearance: number | null
  kerbWeight: number | null
  bootSpace: number | null
  airbagsCount: number | null
  abs: boolean | null
  ebd: boolean | null
  esp: boolean | null
  isofix: boolean | null
  powerSteering: boolean | null
  powerWindows: boolean | null
  bluetooth: boolean | null
  androidAuto: boolean | null
  appleCarPlay: boolean | null
  navigationSystem: boolean | null
  cruiseControl: boolean | null
  emissionNorm: string | null
}

export interface FilterOptions {
  makes: string[]
  bodyTypes: string[]
  fuelTypes: string[]
  transmissions: string[]
  seatingCapacities: number[]
  minPrice: number
  maxPrice: number
}

export interface CarFilters {
  make?: string
  bodyType?: string
  fuelType?: string
  transmission?: string
  seatingCapacity?: number
  minPrice?: number
  maxPrice?: number
}

export interface ChatResponse {
  reply: string
  suggestions: Car[]
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  text: string
  suggestions?: Car[]
}
