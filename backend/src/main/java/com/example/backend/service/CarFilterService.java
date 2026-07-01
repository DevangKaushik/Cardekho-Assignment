package com.example.backend.service;

import com.example.backend.model.Car;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarFilterService {

    public List<Car> filter(
            List<Car> cars,
            String make,
            String bodyType,
            String fuelType,
            String transmission,
            Integer seatingCapacity,
            Integer minPrice,
            Integer maxPrice
    ) {
        return cars.stream()
                .filter(car -> matches(car.make(), make))
                .filter(car -> matches(car.bodyType(), bodyType))
                .filter(car -> matches(car.fuelType(), fuelType))
                .filter(car -> matches(car.transmission(), transmission))
                .filter(car -> seatingCapacity == null || seatingCapacity.equals(car.seatingCapacity()))
                .filter(car -> minPrice == null || car.price() == null || car.price() >= minPrice)
                .filter(car -> maxPrice == null || car.price() == null || car.price() <= maxPrice)
                .collect(Collectors.toList());
    }

    private boolean matches(String fieldValue, String filterValue) {
        return filterValue == null || filterValue.isBlank()
                || (fieldValue != null && fieldValue.equalsIgnoreCase(filterValue));
    }
}