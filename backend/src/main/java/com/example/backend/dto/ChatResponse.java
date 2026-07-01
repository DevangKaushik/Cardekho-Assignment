package com.example.backend.dto;

import com.example.backend.model.Car;

import java.util.List;

public record ChatResponse(String reply, List<Car> suggestions) {
}