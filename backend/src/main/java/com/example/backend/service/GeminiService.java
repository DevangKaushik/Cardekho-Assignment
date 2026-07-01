package com.example.backend.service;

import com.example.backend.model.Car;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.List;

/**
 * Calls the Gemini API to turn a shortlist of dataset-matched cars into a
 * natural-language recommendation. The API key comes only from the
 * GEMINI_API_KEY environment variable - never hardcoded or committed.
 */
@Service
public class GeminiService {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public GeminiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(6));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Returns an AI-written recommendation grounded in the given candidates,
     * or null if Gemini isn't configured or the call fails - callers should
     * fall back to the plain keyword-match reply in that case.
     */
    public String recommend(String userMessage, List<Car> candidates) {
        if (!isConfigured()) {
            return null;
        }

        ObjectNode part = objectMapper.createObjectNode().put("text", buildPrompt(userMessage, candidates));
        ArrayNode parts = objectMapper.createArrayNode().add(part);
        ObjectNode content = objectMapper.createObjectNode().set("parts", parts);
        ArrayNode contents = objectMapper.createArrayNode().add(content);
        ObjectNode requestBody = objectMapper.createObjectNode().set("contents", contents);

        try {
            JsonNode response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            String text = response
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asString(null);

            return (text == null || text.isBlank()) ? null : text.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String buildPrompt(String userMessage, List<Car> candidates) {
        return "You are a car-buying assistant for the Indian market. A user asked: \"" + userMessage + "\". "
                + "Below is a shortlist of at most 3 cars (JSON) already matched to their request - "
                + "recommend only from this list, never invent a car or spec that isn't there. "
                + "Compare them against each other directly: weigh price against running cost, "
                + "using mileage/fuel efficiency to reason about which is more cost-effective over time "
                + "(a cheaper car with poor mileage can lose out to a pricier one that saves more on fuel). "
                + "Call out which single car is the best overall buy and briefly say why, then note any "
                + "reasonable alternative from the list for a different priority (e.g. lowest running cost, "
                + "most features, lowest upfront price). "
                + "Reply in plain conversational text, no markdown or bullet points, in 3-5 sentences. "
                + "If none of the cars fit well, say so honestly instead of forcing a pick.\n\nCars:\n"
                + toCompactJson(candidates);
    }

    private String toCompactJson(List<Car> candidates) {
        ArrayNode array = objectMapper.createArrayNode();
        for (Car car : candidates) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("make", car.make());
            node.put("model", car.model());
            node.put("variant", car.variant());
            node.put("price", car.price());
            node.put("bodyType", car.bodyType());
            node.put("fuelType", car.fuelType());
            node.put("transmission", car.transmission());
            node.put("seatingCapacity", car.seatingCapacity());
            node.put("mileage", car.araiMileage());
            node.put("powerBhp", car.powerBhp());
            array.add(node);
        }
        return array.toString();
    }
}