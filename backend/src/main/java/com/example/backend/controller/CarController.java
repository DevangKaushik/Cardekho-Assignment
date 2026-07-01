package com.example.backend.controller;

import com.example.backend.dto.FilterOptions;
import com.example.backend.model.Car;
import com.example.backend.service.CarDataService;
import com.example.backend.service.CarFilterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CarController {

    private final CarDataService carDataService;
    private final CarFilterService carFilterService;

    public CarController(CarDataService carDataService, CarFilterService carFilterService) {
        this.carDataService = carDataService;
        this.carFilterService = carFilterService;
    }

    @GetMapping("/api/cars")
    public List<Car> getCars(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String bodyType,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) Integer seatingCapacity,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        return carFilterService.filter(
                carDataService.getAll(), make, bodyType, fuelType, transmission,
                seatingCapacity, minPrice, maxPrice
        );
    }

    @GetMapping("/api/cars/filters")
    public FilterOptions getFilterOptions() {
        return new FilterOptions(
                carDataService.distinctMakes(),
                carDataService.distinctBodyTypes(),
                carDataService.distinctFuelTypes(),
                carDataService.distinctTransmissions(),
                carDataService.distinctSeatingCapacities(),
                carDataService.minPrice(),
                carDataService.maxPrice()
        );
    }
}