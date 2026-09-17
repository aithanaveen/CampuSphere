package com.smartcampus.controller;

import com.smartcampus.ai.SentimentResult;
import com.smartcampus.dto.SentimentAnalyticsDTO;
import com.smartcampus.service.FeedbackService;
import com.smartcampus.service.MLSentimentService;
import com.smartcampus.service.SentimentAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentAnalyticsController {

    private final SentimentAnalyticsService sentimentAnalyticsService;
    private final FeedbackService feedbackService;
    private final MLSentimentService mlSentimentService;

    public SentimentAnalyticsController(
            SentimentAnalyticsService sentimentAnalyticsService,
            FeedbackService feedbackService,
            MLSentimentService mlSentimentService) {

        this.sentimentAnalyticsService = sentimentAnalyticsService;
        this.feedbackService = feedbackService;
        this.mlSentimentService = mlSentimentService;
    }

    @GetMapping("/event/{eventId}")
    public SentimentAnalyticsDTO getEventSentiment(
            @PathVariable Long eventId) {

        return sentimentAnalyticsService
                .getEventSentimentAnalytics(eventId);
    }

    @PostMapping("/event/{eventId}/summary")
    public String generateSummary(@PathVariable Long eventId) {

        return feedbackService.generateEventSummary(eventId);
    }

    /**
     * Auxiliary ML Sentiment Classification endpoint (TF-IDF + Logistic Regression).
     * Distinguishes scikit-learn ML classification from the OpenAI Generative AI sentiment analyzer.
     */
    @PostMapping("/ml-analyze")
    public SentimentResult analyzeWithML(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return mlSentimentService.analyzeSentimentML(text);
    }
}