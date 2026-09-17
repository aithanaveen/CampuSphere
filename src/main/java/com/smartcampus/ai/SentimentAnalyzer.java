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
import java.util.Set;
import java.util.logging.Logger;

/**
 * AI-powered sentiment analyzer.
 * Calls the OpenAI Chat Completions API to classify student feedback comments
 * as POSITIVE, NEGATIVE, or NEUTRAL with a confidence score (0.0 – 1.0).
 *
 * Fallback: If the API key is missing or the call fails for any reason,
 * returns NEUTRAL with score 0.5 so that feedback submission never fails.
 */
@Service
public class SentimentAnalyzer {

    private static final Logger log = Logger.getLogger(SentimentAnalyzer.class.getName());

    private static final Set<String> VALID_SENTIMENTS =
            Set.of("POSITIVE", "NEGATIVE", "NEUTRAL");

    // Keywords for local fallback when OpenAI API key is absent
    private static final List<String> POSITIVE_KEYWORDS = Arrays.asList(
            "excellent", "amazing", "great", "good", "fantastic", "wonderful",
            "outstanding", "brilliant", "superb", "well organized", "well-organized",
            "very well", "loved", "enjoyed", "awesome", "impressive", "helpful",
            "informative", "best", "perfect", "happy", "satisfied", "recommend",
            "learned", "useful", "nice", "thank", "appreciate", "positive",
            "engaging", "interesting", "fun", "enjoyed", "liked", "pleased"
    );

    private static final List<String> NEGATIVE_KEYWORDS = Arrays.asList(
            "bad", "poor", "terrible", "awful", "worst", "horrible", "disappointing",
            "disappointed", "boring", "waste", "useless", "not good", "not helpful",
            "not organized", "disorganized", "irrelevant", "uninteresting", "dull",
            "confusing", "pathetic", "unprofessional", "late", "delayed", "cancelled",
            "not satisfied", "unhappy", "frustrated", "problem", "issue", "complaint"
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    public SentimentAnalyzer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    /**
     * Analyze the sentiment of a student feedback comment.
     *
     * @param comment The student's feedback text
     * @return SentimentResult with sentiment label and confidence score
     */
    public SentimentResult analyze(String comment) {

        // Handle empty or blank comment
        if (comment == null || comment.isBlank()) {
            return new SentimentResult("NEUTRAL", 0.5);
        }

        // Handle missing API key — use keyword-based local analysis
        if (apiKey == null || apiKey.isBlank()) {
            log.info("OpenAI API key is not configured. Using keyword-based local sentiment analysis.");
            return localSentimentAnalysis(comment);
        }

        try {
            String prompt = """
                    Analyze the sentiment of the following student feedback about a campus event.

                    Return ONLY valid JSON in this exact format (no extra text, no markdown):
                    {"sentiment": "POSITIVE", "score": 0.95}

                    Rules:
                    - sentiment must be exactly one of: POSITIVE, NEGATIVE, NEUTRAL
                    - score must be a decimal number between 0.0 and 1.0

                    Student feedback:
                    """ + comment;

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.1,
                    "max_tokens", 60
            );

            String response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String text = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            // Strip markdown code fences if present
            text = text.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode result = objectMapper.readTree(text);

            String sentiment = result.path("sentiment").asText("NEUTRAL").toUpperCase().trim();
            double score = result.path("score").asDouble(0.5);

            // Validate sentiment
            if (!VALID_SENTIMENTS.contains(sentiment)) {
                log.warning("AI returned invalid sentiment: " + sentiment + ". Defaulting to NEUTRAL.");
                sentiment = "NEUTRAL";
            }

            // Clamp score to [0.0, 1.0]
            if (score < 0.0 || score > 1.0) {
                log.warning("AI returned out-of-range score: " + score + ". Clamping to 0.5.");
                score = 0.5;
            }

            return new SentimentResult(sentiment, score);

        } catch (Exception e) {
            log.warning("Sentiment analysis failed: " + e.getMessage() + ". Falling back to keyword analysis.");
            return localSentimentAnalysis(comment);
        }
    }

    /**
     * Simple keyword-based sentiment analysis used as fallback when OpenAI is unavailable.
     * Counts positive and negative keyword matches and decides sentiment accordingly.
     */
    private SentimentResult localSentimentAnalysis(String comment) {
        String lower = comment.toLowerCase();

        long positiveCount = POSITIVE_KEYWORDS.stream()
                .filter(lower::contains)
                .count();

        long negativeCount = NEGATIVE_KEYWORDS.stream()
                .filter(lower::contains)
                .count();

        if (positiveCount > 0 && positiveCount >= negativeCount) {
            double score = Math.min(0.5 + (positiveCount * 0.1), 0.95);
            return new SentimentResult("POSITIVE", score);
        } else if (negativeCount > 0 && negativeCount > positiveCount) {
            double score = Math.min(0.5 + (negativeCount * 0.1), 0.95);
            return new SentimentResult("NEGATIVE", score);
        } else {
            return new SentimentResult("NEUTRAL", 0.5);
        }
    }
}