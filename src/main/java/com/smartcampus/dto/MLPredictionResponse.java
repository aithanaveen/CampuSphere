package com.smartcampus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MLPredictionResponse {

    @JsonProperty("prediction")
    private Integer prediction;

    @JsonProperty("probability")
    private Double probability;

    @JsonProperty("model_used")
    private String modelUsed;

    @JsonProperty("event_id")
    private Long eventId;

    public MLPredictionResponse() {
    }

    public MLPredictionResponse(Integer prediction, Double probability, String modelUsed, Long eventId) {
        this.prediction = prediction;
        this.probability = probability;
        this.modelUsed = modelUsed;
        this.eventId = eventId;
    }

    public Integer getPrediction() {
        return prediction;
    }

    public void setPrediction(Integer prediction) {
        this.prediction = prediction;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
