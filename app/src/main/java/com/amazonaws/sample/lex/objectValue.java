package com.amazonaws.sample.lex;

public class objectValue {
    private String name;
    private double confidence;

    public objectValue() {
        this.name = "hello";
        this.confidence = 0.01;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
