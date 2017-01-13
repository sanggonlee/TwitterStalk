package com.example.sanggon.twitterstalk.models;

import java.io.Serializable;

/**
 * Pojo class that encapsulates analysis description and score pair
 */
public class AnalysisSegment
        implements Serializable {
    private String description;
    private Float score;

    public AnalysisSegment(String description, Float score) {
        this.description = description;
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public Float getScore() {
        return score;
    }
}
