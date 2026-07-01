package com.example.backend.service;

import com.example.backend.model.Car;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarDataService {

    private final ObjectMapper objectMapper;
    private List<Car> cars;

    public CarDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadCars() throws IOException {
        try (InputStream in = new ClassPathResource("data/cars.json").getInputStream()) {
            cars = objectMapper.readValue(in, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Car.class));
        }
    }

    public List<Car> getAll() {
        return cars;
    }

    public List<String> distinctMakes() {
        return distinctValues(Car::make);
    }

    public List<String> distinctBodyTypes() {
        return distinctValues(Car::bodyType);
    }

    public List<String> distinctFuelTypes() {
        return distinctValues(Car::fuelType);
    }

    public List<String> distinctTransmissions() {
        return distinctValues(Car::transmission);
    }

    public List<Integer> distinctSeatingCapacities() {
        return cars.stream()
                .map(Car::seatingCapacity)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int minPrice() {
        return cars.stream().map(Car::price).filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(0);
    }

    public int maxPrice() {
        return cars.stream().map(Car::price).filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(0);
    }

    private List<String> distinctValues(java.util.function.Function<Car, String> extractor) {
        return cars.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}