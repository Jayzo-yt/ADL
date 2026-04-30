package com.acd.verify.service;

import com.acd.verify.model.VerificationResult;
import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {

    public Integer calculateScore(VerificationResult result, double ocrAverageConfidence) {
        int score = 0;

        for (String issue : result.getIssues()) {
            if (issue.contains("Certificate ID not found in database")) {
                score += 40;
            } else if (issue.contains("Name mismatch")) {
                score += 30;
            } else if (issue.contains("Roll number mismatch")) {
                score += 20;
            } else if (issue.contains("Marks mismatch") || issue.contains("CGPA mismatch")) {
                score += 20;
            } else if (issue.contains("Course mismatch")) {
                score += 15;
            } else if (issue.contains("University mismatch")) {
                score += 15;
            } else if (issue.toLowerCase().contains("hash mismatch") || issue.toLowerCase().contains("tampering")) {
                score += 30;
            } else if (issue.contains("not found")) {
                score += 15;
            } else {
                score += 10;
            }
        }

        // Penalize low OCR confidence
        if (ocrAverageConfidence > 0 && ocrAverageConfidence < 70.0) {
            score += 25;
            result.addIssue("Low OCR confidence: " + String.format("%.1f", ocrAverageConfidence));
        }

        return Math.min(score, 100);
    }
}