package com.smartcampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.dto.MLPredictionRequest;
import com.smartcampus.dto.MLPredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for communicating with the external Python FastAPI Machine Learning microservice.
 *
 * Responsibilities:
 * - Submits student-event feature vectors to POST /predict or POST /predict-batch
 * - Receives real model-predicted registration probabilities from the trained scikit-learn model
 * - Handles service unavailability gracefully: logs warnings and returns null/empty so that
 *   the calling recommendation engine can seamlessly fall back to deterministic scoring.
 */
@Service
public class MLRecommendationService {

    private static final Logger log = Logger.getLogger(MLRecommendationService.class.getName());

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String mlServiceUrl;

    public MLRecommendationService(
            ObjectMapper objectMapper,
            @Value("${ml.service.url:http://127.0.0.1:8000}") String mlServiceUrl) {

        this.objectMapper = objectMapper;
        this.mlServiceUrl = mlServiceUrl;

        // Configure client with short timeouts so application remains fast and responsive
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(2000));
        requestFactory.setReadTimeout(Duration.ofMillis(3000));

        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Checks if the Python ML microservice is online and responding.
     */
    public boolean isMLServiceAvailable() {
        try {
            String response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(String.class);

            if (response != null && response.contains("healthy")) {
                return true;
            }
        } catch (Exception e) {
            log.fine("ML service health check failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Call ML Service to predict registration for a single student-event pair.
     *
     * @param request Feature payload
     * @return MLPredictionResponse with probability, prediction, and model name; or null if service fails
     */
    public MLPredictionResponse predictRegistration(MLPredictionRequest request) {
        try {
            return restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MLPredictionResponse.class);

        } catch (Exception e) {
            log.log(Level.WARNING, "ML Service predict call failed at " + mlServiceUrl + "/predict: " + e.getMessage()
                    + " — falling back to deterministic recommendation logic.");
            return null;
        }
    }

    /**
     * Batch prediction for multiple events to minimize network roundtrips.
     *
     * @param requests List of feature payloads
     * @return List of MLPredictionResponse; or empty list if service fails
     */
    public List<MLPredictionResponse> predictBatch(List<MLPredictionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        try {
            Map<String, Object> payload = Map.of("items", requests);

            String responseBody = restClient.post()
                    .uri("/predict-batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode predictionsNode = root.path("predictions");

            List<MLPredictionResponse> list = new ArrayList<>();
            if (predictionsNode.isArray()) {
                for (JsonNode item : predictionsNode) {
                    list.add(objectMapper.treeToValue(item, MLPredictionResponse.class));
                }
            }
            return list;

        } catch (Exception e) {
            log.log(Level.WARNING, "ML Service predict-batch failed at " + mlServiceUrl + "/predict-batch: " + e.getMessage()
                    + " — falling back to deterministic recommendation logic.");
            return List.of();
        }
    }

    public String getMlServiceUrl() {
        return mlServiceUrl;
    }
}
