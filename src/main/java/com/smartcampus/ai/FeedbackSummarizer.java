package com.smartcampus.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AI-powered feedback summarizer.
 * Combines all student feedback comments for an event and generates
 * a concise human-readable summary using the OpenAI Chat Completions API.
 *
 * Fallback: If the API is unavailable, returns a plain text fallback message.
 */
@Service
public class FeedbackSummarizer {

    private static final Logger log = Logger.getLogger(FeedbackSummarizer.class.getName());

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    public FeedbackSummarizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    /**
     * Generate an AI summary for a set of combined feedback comments.
     *
     * @param feedbackText Combined feedback comments
     * @return AI-generated summary string, or fallback message if AI fails
     */
    public String generateSummary(String feedbackText) {

        if (feedbackText == null || feedbackText.isBlank()) {
            return "No feedback available to summarize.";
        }

        if (apiKey == null || apiKey.isBlank()) {
            log.info("OpenAI API key is not configured. Using local rule-based summary.");
            return generateLocalSummary(feedbackText);
        }

        try {
            String prompt = """
                    Summarize the following student feedback about a campus event.

                    Requirements:
                    - Identify the overall opinion of students.
                    - Mention the main positive points if any.
                    - Mention important complaints or problems if any.
                    - Keep the summary concise (2–4 sentences).
                    - Do not invent information not present in the feedback.
                    - Write in third person (e.g., "Students found...", "Attendees appreciated...").

                    Student feedback:
                    """ + feedbackText;

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.5,
                    "max_tokens", 300
            );

            String response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

        } catch (Exception e) {
            log.warning("Feedback summarization failed: " + e.getMessage());
            return generateLocalSummary(feedbackText);
        }
    }

    /**
     * Generates a simple rule-based summary from feedback text when OpenAI is unavailable.
     * Counts lines, detects positive/negative tone using keyword matching, and forms a summary.
     */
    private String generateLocalSummary(String feedbackText) {
        List<String> positiveWords = Arrays.asList(
                "excellent", "great", "good", "amazing", "fantastic", "well organized",
                "very well", "loved", "enjoyed", "awesome", "impressive", "helpful",
                "informative", "best", "perfect", "happy", "satisfied", "wonderful",
                "outstanding", "superb", "nice", "useful", "thank", "appreciate");

        List<String> negativeWords = Arrays.asList(
                "bad", "poor", "terrible", "awful", "worst", "horrible", "disappointing",
                "boring", "waste", "useless", "not good", "not organized", "problem",
                "issue", "complaint", "frustrated", "unhappy", "disorganized", "dull");

        String lower = feedbackText.toLowerCase();
        long posCount = positiveWords.stream().filter(lower::contains).count();
        long negCount = negativeWords.stream().filter(lower::contains).count();

        String[] lines = feedbackText.split("\n");
        int responseCount = (int) Arrays.stream(lines)
                .filter(l -> l.startsWith("- ") && l.trim().length() > 2)
                .count();

        String tone;
        if (posCount > negCount * 2) {
            tone = "overwhelmingly positive";
        } else if (posCount > negCount) {
            tone = "generally positive";
        } else if (negCount > posCount * 2) {
            tone = "largely negative";
        } else if (negCount > posCount) {
            tone = "mixed with some concerns";
        } else {
            tone = "neutral";
        }

        return String.format(
                "Based on %d student response%s, the overall feedback is %s. " +
                "Students shared their experiences and opinions about the event's organization, content, and delivery. " +
                "Administrators are encouraged to review individual comments for specific insights.",
                responseCount, responseCount == 1 ? "" : "s", tone);
    }
}