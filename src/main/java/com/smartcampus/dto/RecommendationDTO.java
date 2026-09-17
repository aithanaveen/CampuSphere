package com.smartcampus.dto;

import com.smartcampus.entity.Event;

public class RecommendationDTO {

    private Event event;
    private double score;
    private String reason;
    private Double probability;
    private Integer prediction;
    private String modelSource;

    public RecommendationDTO() {
    }

    public RecommendationDTO(Event event, double score, String reason) {
        this.event = event;
        this.score = score;
        this.reason = reason;
        this.probability = score > 0 ? Math.min(1.0, score / 100.0) : 0.0;
        this.prediction = score > 0 ? 1 : 0;
        this.modelSource = "CONTENT_FALLBACK";
    }

    public RecommendationDTO(
            Event event,
            double score,
            String reason,
            Double probability,
            Integer prediction,
            String modelSource) {

        this.event = event;
        this.score = score;
        this.reason = reason;
        this.probability = probability;
        this.prediction = prediction;
        this.modelSource = modelSource;
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

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public Integer getPrediction() {
        return prediction;
    }

    public void setPrediction(Integer prediction) {
        this.prediction = prediction;
    }

    public String getModelSource() {
        return modelSource;
    }

    public void setModelSource(String modelSource) {
        this.modelSource = modelSource;
    }
}