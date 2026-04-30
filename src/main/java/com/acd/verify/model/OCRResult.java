package com.acd.verify.model;

public class OCRResult {
    private String text;
    private double averageConfidence;

    public OCRResult() {}

    public OCRResult(String text, double averageConfidence) {
        this.text = text;
        this.averageConfidence = averageConfidence;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public double getAverageConfidence() { return averageConfidence; }
    public void setAverageConfidence(double averageConfidence) { this.averageConfidence = averageConfidence; }
}
