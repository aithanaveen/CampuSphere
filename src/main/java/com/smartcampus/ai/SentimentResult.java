package com.smartcampus.ai;

public class SentimentResult {

    private String sentiment;
    private Double score;

    public SentimentResult() {
    }

    public SentimentResult(String sentiment, Double score) {
        this.sentiment = sentiment;
        this.score = score;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}