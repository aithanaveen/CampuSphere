package com.smartcampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.ai.SentimentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for calling the ML Sentiment Classifier (TF-IDF + Logistic Regression)
 * hosted on the FastAPI microservice.
 *
 * This provides a distinct machine-learning based sentiment classifier that operates
 * alongside the existing OpenAI Generative AI sentiment analyzer.
 */
@Service
public class MLSentimentService {

    private static final Logger log = Logger.getLogger(MLSentimentService.class.getName());

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MLSentimentService(
            ObjectMapper objectMapper,
            @Value("${ml.service.url:http://127.0.0.1:8000}") String mlServiceUrl) {

        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(2000));
        requestFactory.setReadTimeout(Duration.ofMillis(3000));

        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Analyze sentiment of a student comment using the TF-IDF + Logistic Regression ML model.
     *
     * @param comment The feedback text
     * @return SentimentResult with sentiment label and confidence score
     */
    public SentimentResult analyzeSentimentML(String comment) {
        if (comment == null || comment.isBlank()) {
            return new SentimentResult("NEUTRAL", 0.5);
        }

        try {
            Map<String, String> payload = Map.of("text", comment);

            String response = restClient.post()
                    .uri("/predict-sentiment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String sentiment = node.path("sentiment").asText("NEUTRAL");
            double score = node.path("score").asDouble(0.5);

            return new SentimentResult(sentiment, score);

        } catch (Exception e) {
            log.log(Level.WARNING, "ML Sentiment Service failed: " + e.getMessage());
            return new SentimentResult("NEUTRAL", 0.5);
        }
    }
}
