package com.example.backend.service;

import com.example.backend.dto.ChatResponse;
import com.example.backend.model.Car;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lightweight keyword-based parser for free-text car requests. Not an NLP/LLM
 * model - it extracts budget, body type, fuel type, transmission and seating
 * hints from the message and reuses the same filtering logic as the dropdown
 * search, so the two input modes stay consistent.
 */
@Service
public class ChatSuggestionService {

    private static final Pattern SEATER_PATTERN = Pattern.compile("(\\d+)\\s*seat(?:er|s)?");
    private static final Pattern LAKH_PATTERN = Pattern.compile("([\\d.]+)\\s*(lakh|lac|l\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CRORE_PATTERN = Pattern.compile("([\\d.]+)\\s*(crore|cr\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIN_QUALIFIER = Pattern.compile("above|over|more than|starting from", Pattern.CASE_INSENSITIVE);
    private static final int MAX_SUGGESTIONS = 3;

    private final CarDataService carDataService;
    private final CarFilterService carFilterService;
    private final GeminiService geminiService;

    public ChatSuggestionService(CarDataService carDataService, CarFilterService carFilterService, GeminiService geminiService) {
        this.carDataService = carDataService;
        this.carFilterService = carFilterService;
        this.geminiService = geminiService;
    }

    public ChatResponse suggest(String message) {
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);

        String make = findKeyword(text, carDataService.distinctMakes());
        String bodyType = findKeyword(text, carDataService.distinctBodyTypes());
        String fuelType = findKeyword(text, carDataService.distinctFuelTypes());
        String transmission = findTransmission(text);
        Integer seatingCapacity = findSeatingCapacity(text);
        Integer price = findPriceInRupees(text);
        boolean isMinBudget = price != null && MIN_QUALIFIER.matcher(text).find();

        Integer minPrice = isMinBudget ? price : null;
        Integer maxPrice = !isMinBudget ? price : null;

        List<Car> matches = carFilterService.filter(
                carDataService.getAll(), make, bodyType, fuelType, transmission,
                seatingCapacity, minPrice, maxPrice
        );

        List<Car> distinctModels = oneVariantPerModel(matches);

        List<Car> suggestions = distinctModels.stream()
                .sorted(Comparator.comparing(Car::price, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(MAX_SUGGESTIONS)
                .collect(Collectors.toList());

        String fallbackReply = buildReply(distinctModels.size(), make, bodyType, fuelType, transmission, seatingCapacity, minPrice, maxPrice);
        String reply = suggestions.isEmpty() ? fallbackReply : geminiService.recommend(message, suggestions);

        return new ChatResponse(reply != null ? reply : fallbackReply, suggestions);
    }

    /**
     * The dataset has one row per variant, so the same model (e.g. "Hyundai
     * Creta") can appear many times. Suggestions should name one car per
     * model, not every trim - keep the cheapest variant as the representative.
     */
    private List<Car> oneVariantPerModel(List<Car> cars) {
        LinkedHashMap<String, Car> byModel = cars.stream()
                .collect(Collectors.toMap(
                        car -> car.make() + "|" + car.model(),
                        car -> car,
                        (a, b) -> {
                            if (a.price() == null) return b;
                            if (b.price() == null) return a;
                            return a.price() <= b.price() ? a : b;
                        },
                        LinkedHashMap::new
                ));
        return new ArrayList<>(byModel.values());
    }

    private String findKeyword(String text, List<String> candidates) {
        return candidates.stream()
                .filter(candidate -> containsWord(text, candidate.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
    }

    private String findTransmission(String text) {
        if (containsWord(text, "automatic") || containsWord(text, "amt")
                || containsWord(text, "cvt") || containsWord(text, "dct")) {
            return "Automatic";
        }
        if (containsWord(text, "manual")) {
            return "Manual";
        }
        return null;
    }

    private boolean containsWord(String text, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    private Integer findSeatingCapacity(String text) {
        Matcher matcher = SEATER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private Integer findPriceInRupees(String text) {
        Matcher crore = CRORE_PATTERN.matcher(text);
        if (crore.find()) {
            return (int) Math.round(Double.parseDouble(crore.group(1)) * 10_000_000);
        }
        Matcher lakh = LAKH_PATTERN.matcher(text);
        if (lakh.find()) {
            return (int) Math.round(Double.parseDouble(lakh.group(1)) * 100_000);
        }
        return null;
    }

    private String buildReply(int matchCount, String make, String bodyType, String fuelType,
                               String transmission, Integer seatingCapacity, Integer minPrice, Integer maxPrice) {
        StringBuilder understood = new StringBuilder();
        if (make != null) understood.append("make: ").append(make).append(", ");
        if (bodyType != null) understood.append("body type: ").append(bodyType).append(", ");
        if (fuelType != null) understood.append("fuel type: ").append(fuelType).append(", ");
        if (transmission != null) understood.append("transmission: ").append(transmission).append(", ");
        if (seatingCapacity != null) understood.append("seats: ").append(seatingCapacity).append(", ");
        if (maxPrice != null) understood.append("budget under Rs. ").append(maxPrice).append(", ");
        if (minPrice != null) understood.append("budget above Rs. ").append(minPrice).append(", ");

        if (understood.isEmpty()) {
            return "I couldn't pick out specific requirements from that message, so here are some options across the dataset. "
                    + "Try mentioning things like budget, body type, fuel type or seating (e.g. \"petrol SUV under 15 lakh with automatic transmission\").";
        }

        String criteria = understood.substring(0, understood.length() - 2);
        return String.format("Found %d car(s) matching %s.", matchCount, criteria);
    }
}