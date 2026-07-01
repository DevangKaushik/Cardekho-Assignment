package com.example.backend.dto;

import java.util.List;

public record FilterOptions(
        List<String> makes,
        List<String> bodyTypes,
        List<String> fuelTypes,
        List<String> transmissions,
        List<Integer> seatingCapacities,
        int minPrice,
        int maxPrice
) {
}