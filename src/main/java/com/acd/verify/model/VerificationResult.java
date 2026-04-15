package com.acd.verify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResult {
    private String status; // VALID, SUSPICIOUS, FAKE
    private Integer riskScore;
    private List<String> issues = new ArrayList<>();
    private ExtractedData extractedData;
    private Certificate matchedCertificate;
    private Long verificationLogId;

    public void addIssue(String issue) {
        this.issues.add(issue);
    }
}