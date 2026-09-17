package com.smartcampus.controller;

import com.smartcampus.dto.SentimentAnalyticsDTO;
import com.smartcampus.service.FeedbackService;
import com.smartcampus.service.SentimentAnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentAnalyticsController {

    private final SentimentAnalyticsService sentimentAnalyticsService;
    private final FeedbackService feedbackService;

    public SentimentAnalyticsController(
            SentimentAnalyticsService sentimentAnalyticsService,
            FeedbackService feedbackService) {

        this.sentimentAnalyticsService = sentimentAnalyticsService;
        this.feedbackService = feedbackService;
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
}