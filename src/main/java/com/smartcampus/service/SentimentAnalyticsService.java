package com.smartcampus.service;

import com.smartcampus.dto.SentimentAnalyticsDTO;
import com.smartcampus.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

@Service
public class SentimentAnalyticsService {

    private final FeedbackRepository feedbackRepository;

    public SentimentAnalyticsService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public SentimentAnalyticsDTO getEventSentimentAnalytics(Long eventId) {

        long total = feedbackRepository.countByEventId(eventId);

        long positive =
                feedbackRepository.countByEventIdAndSentiment(
                        eventId, "POSITIVE");

        long negative =
                feedbackRepository.countByEventIdAndSentiment(
                        eventId, "NEGATIVE");

        long neutral =
                feedbackRepository.countByEventIdAndSentiment(
                        eventId, "NEUTRAL");

        return new SentimentAnalyticsDTO(
                eventId,
                total,
                positive,
                negative,
                neutral
        );
    }
}