package com.smartcampus.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AI-powered recommendation reason generator.
 * Given a student's profile and an event's details, generates a one-sentence
 * explanation of why this event may be relevant to the student.
 *
 * The actual recommendation score is calculated deterministically by
 * RecommendationService using a content-based algorithm.
 * This class only generates the human-readable reason.
 *
 * Fallback: If the AI API is unavailable, a deterministic fallback reason
 * is returned — recommendations are NEVER broken by AI failure.
 */
@Service
public class RecommendationAI {

    private static final Logger log = Logger.getLogger(RecommendationAI.class.getName());

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    public RecommendationAI(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    /**
     * Generate a one-sentence reason why this event is recommended for this student.
     *
     * @param studentInterests Comma-separated student interests
     * @param department       Student's department
     * @param year             Student's year of study
     * @param eventTitle       Event title
     * @param eventCategory    Event category
     * @param eventDescription Event description
     * @return Human-readable recommendation reason
     */
    public String generateReason(
            String studentInterests,
            String department,
            String year,
            String eventTitle,
            String eventCategory,
            String eventDescription) {

        // Fallback reason if API key is not configured
        if (apiKey == null || apiKey.isBlank()) {
            return buildFallbackReason(studentInterests, eventTitle, eventCategory);
        }

        try {
            String prompt = """
                    You are an event recommendation assistant for a college campus system.

                    Student profile:
                    - Interests: %s
                    - Department: %s
                    - Year of study: %s

                    Event details:
                    - Title: %s
                    - Category: %s
                    - Description: %s

                    Write exactly ONE short sentence (under 25 words) explaining why this event
                    may be relevant or useful to this student based on their profile.
                    Do not use phrases like "This event" at the start. Be specific and friendly.
                    Do not invent information not mentioned above.
                    """.formatted(
                            studentInterests, department, year,
                            eventTitle, eventCategory, eventDescription);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 80
            );

            String response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String reason = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            return reason.isEmpty() ? buildFallbackReason(studentInterests, eventTitle, eventCategory) : reason;

        } catch (Exception e) {
            log.warning("RecommendationAI failed for event '" + eventTitle + "': " + e.getMessage()
                    + " — using fallback reason.");
            return buildFallbackReason(studentInterests, eventTitle, eventCategory);
        }
    }

    /**
     * Deterministic fallback reason when AI is unavailable.
     * Uses the student's interests and event category to craft a basic reason.
     */
    private String buildFallbackReason(String interests, String eventTitle, String eventCategory) {
        if (interests != null && !interests.isBlank() && eventCategory != null && !eventCategory.isBlank()) {
            String[] interestArr = interests.split(",");
            for (String interest : interestArr) {
                String trimmed = interest.trim();
                if (!trimmed.isEmpty() &&
                    (eventCategory.toLowerCase().contains(trimmed.toLowerCase()) ||
                     eventTitle.toLowerCase().contains(trimmed.toLowerCase()))) {
                    return "Matches your interest in " + trimmed + " based on the event category and title.";
                }
            }
            return "Aligns with your interests (" + interests.trim() + ") and may enhance your academic profile.";
        }
        return "This event may provide a valuable learning and networking opportunity relevant to your profile.";
    }
}