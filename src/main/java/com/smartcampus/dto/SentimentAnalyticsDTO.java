package com.smartcampus.dto;

public class SentimentAnalyticsDTO {

    private Long eventId;
    private long totalFeedback;
    private long positive;
    private long negative;
    private long neutral;

    public SentimentAnalyticsDTO() {
    }

    public SentimentAnalyticsDTO(
            Long eventId,
            long totalFeedback,
            long positive,
            long negative,
            long neutral) {

        this.eventId = eventId;
        this.totalFeedback = totalFeedback;
        this.positive = positive;
        this.negative = negative;
        this.neutral = neutral;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public long getTotalFeedback() {
        return totalFeedback;
    }

    public void setTotalFeedback(long totalFeedback) {
        this.totalFeedback = totalFeedback;
    }

    public long getPositive() {
        return positive;
    }

    public void setPositive(long positive) {
        this.positive = positive;
    }

    public long getNegative() {
        return negative;
    }

    public void setNegative(long negative) {
        this.negative = negative;
    }

    public long getNeutral() {
        return neutral;
    }

    public void setNeutral(long neutral) {
        this.neutral = neutral;
    }
}