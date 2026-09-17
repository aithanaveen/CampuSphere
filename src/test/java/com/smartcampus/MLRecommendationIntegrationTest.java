package com.smartcampus;

import com.smartcampus.ai.SentimentResult;
import com.smartcampus.dto.MLPredictionRequest;
import com.smartcampus.dto.MLPredictionResponse;
import com.smartcampus.dto.RecommendationDTO;
import com.smartcampus.service.MLRecommendationService;
import com.smartcampus.service.MLSentimentService;
import com.smartcampus.service.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MLRecommendationIntegrationTest {

    @Autowired
    private MLRecommendationService mlRecommendationService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private MLSentimentService mlSentimentService;

    @Test
    @DisplayName("Verify FastAPI ML service /health and /predict connectivity")
    void testMLServicePrediction() {
        boolean available = mlRecommendationService.isMLServiceAvailable();
        assertTrue(available, "FastAPI ML service should be healthy and available on port 8000");

        MLPredictionRequest request = new MLPredictionRequest(
                "CSE",
                4,
                "Technical",
                "CSE",
                5,
                4,
                1,
                1,
                80,
                1L
        );

        MLPredictionResponse response = mlRecommendationService.predictRegistration(request);
        assertNotNull(response, "Response from ML service should not be null");
        assertNotNull(response.getProbability(), "Probability should be present");
        assertTrue(response.getProbability() >= 0.0 && response.getProbability() <= 1.0,
                "Probability must be within range [0.0, 1.0]");
        assertNotNull(response.getPrediction(), "Prediction binary class should be present");
        assertEquals("Logistic Regression", response.getModelUsed());
    }

    @Test
    @DisplayName("Verify end-to-end student recommendations with ML predictions and AI explanations")
    void testRecommendationServiceEndToEnd() {
        // student@smartcampus.com is seeded by DataInitializer
        List<RecommendationDTO> recommendations = recommendationService.getRecommendations("student@smartcampus.com");
        assertNotNull(recommendations, "Recommendations list should not be null");

        if (!recommendations.isEmpty()) {
            RecommendationDTO first = recommendations.get(0);
            assertNotNull(first.getEvent(), "Event must not be null");
            assertNotNull(first.getProbability(), "Probability must be populated");
            assertNotNull(first.getReason(), "AI reason must be populated");
            assertTrue(first.getReason().length() > 5, "AI reason must be a descriptive sentence");
            assertTrue(first.getModelSource().equals("ML_MODEL") || first.getModelSource().equals("CONTENT_FALLBACK"),
                    "Model source must be either ML_MODEL or CONTENT_FALLBACK");
        }
    }

    @Test
    @DisplayName("Verify TF-IDF + Logistic Regression ML sentiment analysis")
    void testMLSentimentAnalysis() {
        SentimentResult positiveResult = mlSentimentService.analyzeSentimentML(
                "The session was fantastic, awesome presentation and well organized!");
        assertNotNull(positiveResult);
        assertEquals("POSITIVE", positiveResult.getSentiment());
        assertTrue(positiveResult.getScore() >= 0.5);

        SentimentResult negativeResult = mlSentimentService.analyzeSentimentML(
                "Very poorly organized, started almost an hour late and terrible audio.");
        assertNotNull(negativeResult);
        assertEquals("NEGATIVE", negativeResult.getSentiment());
    }
}
