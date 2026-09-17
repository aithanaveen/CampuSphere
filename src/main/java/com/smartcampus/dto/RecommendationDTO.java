package com.smartcampus.dto;

import com.smartcampus.entity.Event;

public class RecommendationDTO {

    private Event event;
    private double score;
    private String reason;

    public RecommendationDTO() {
    }

    public RecommendationDTO(
            Event event,
            double score,
            String reason) {

        this.event = event;
        this.score = score;
        this.reason = reason;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}