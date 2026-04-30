package com.acd.verify.model;

import java.util.ArrayList;
import java.util.List;

public class VerificationResult {
    private String status;
    private Integer riskScore;
    private List<String> issues = new ArrayList<>();
    private ExtractedData extractedData;
    private Certificate matchedCertificate;
    private Long verificationLogId;

    public void addIssue(String issue) {
        this.issues.add(issue);
    }

    public VerificationResult() {
    }

    public VerificationResult(String status, Integer riskScore, List<String> issues, ExtractedData extractedData, Certificate matchedCertificate, Long verificationLogId) {
        this.status = status;
        this.riskScore = riskScore;
        this.issues = issues;
        this.extractedData = extractedData;
        this.matchedCertificate = matchedCertificate;
        this.verificationLogId = verificationLogId;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public List<String> getIssues() { return issues; }
    public void setIssues(List<String> issues) { this.issues = issues; }
    public ExtractedData getExtractedData() { return extractedData; }
    public void setExtractedData(ExtractedData extractedData) { this.extractedData = extractedData; }
    public Certificate getMatchedCertificate() { return matchedCertificate; }
    public void setMatchedCertificate(Certificate matchedCertificate) { this.matchedCertificate = matchedCertificate; }
    public Long getVerificationLogId() { return verificationLogId; }
    public void setVerificationLogId(Long verificationLogId) { this.verificationLogId = verificationLogId; }
}