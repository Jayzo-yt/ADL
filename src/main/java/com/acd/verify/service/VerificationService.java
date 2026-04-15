package com.acd.verify.service;

import com.acd.verify.model.Certificate;
import com.acd.verify.model.ExtractedData;
import com.acd.verify.model.VerificationResult;
import com.acd.verify.repository.CertificateRepository;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private RiskScoringService riskScoringService;

    public VerificationResult verify(ExtractedData extractedData) {
        VerificationResult result = new VerificationResult();
        result.setExtractedData(extractedData);

        if (extractedData.getCertId() == null || extractedData.getCertId().isEmpty()) {
            result.addIssue("Certificate ID not found in document");
            result.setRiskScore(riskScoringService.calculateScore(result));
            result.setStatus(determineStatus(result.getRiskScore()));
            return result;
        }

        Optional<Certificate> certOpt = certificateRepository.findByCertId(extractedData.getCertId());

        if (certOpt.isEmpty()) {
            result.addIssue("Certificate ID not found in database");
            result.setRiskScore(riskScoringService.calculateScore(result));
            result.setStatus(determineStatus(result.getRiskScore()));
            return result;
        }

        Certificate dbCert = certOpt.get();
        result.setMatchedCertificate(dbCert);

        verifyRollNumber(extractedData, dbCert, result);
        verifyName(extractedData, dbCert, result);
        verifyMarks(extractedData, dbCert, result);
        verifyCourse(extractedData, dbCert, result);
        verifyUniversity(extractedData, dbCert, result);
        verifyHash(extractedData, dbCert, result);

        result.setRiskScore(riskScoringService.calculateScore(result));
        result.setStatus(determineStatus(result.getRiskScore()));

        logger.info("Verification result: {}", result);

        return result;
    }

    private void verifyRollNumber(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getRollNo() != null && !extracted.getRollNo().equals(dbCert.getRollNo())) {
            result.addIssue("Roll number mismatch: Expected " + dbCert.getRollNo() +
                    ", Found " + extracted.getRollNo());
        }
    }

    private void verifyName(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getName() != null) {
            String extractedName = extracted.getName().trim();
            String dbName = dbCert.getName().toUpperCase().trim();

            if (extractedName.equals(dbName)) {
                return;
            }

            LevenshteinDistance ld = new LevenshteinDistance();
            int distance = ld.apply(extractedName, dbName);

            if (distance > 3) {
                result.addIssue("Name mismatch: Expected " + dbCert.getName() +
                        ", Found " + extracted.getName());
            }
        }
    }

    private void verifyMarks(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getMarks() != null &&
                !extracted.getMarks().equals(dbCert.getMarks())) {
            result.addIssue("Marks mismatch: Expected " + dbCert.getMarks() +
                    ", Found " + extracted.getMarks());
        }
    }

    private void verifyCourse(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getCourse() != null &&
                !extracted.getCourse().contains(dbCert.getCourse().toUpperCase())) {
            result.addIssue("Course mismatch: Expected " + dbCert.getCourse() +
                    ", Found " + extracted.getCourse());
        }
    }

    private void verifyUniversity(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getUniversity() != null &&
                !extracted.getUniversity().contains(dbCert.getUniversity().toUpperCase())) {
            result.addIssue("University mismatch: Expected " + dbCert.getUniversity() +
                    ", Found " + extracted.getUniversity());
        }
    }

    private void verifyHash(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (dbCert.getHashValue() != null && !dbCert.getHashValue().isEmpty()) {
            String calculatedHash = calculateHash(extracted);

            if (!calculatedHash.equals(dbCert.getHashValue())) {
                result.addIssue("Document hash mismatch - possible tampering detected");
            }
        }
    }

    private String calculateHash(ExtractedData data) {
        try {
            String dataString = String.format("%s%s%s%s%s",
                    data.getCertId(),
                    data.getRollNo(),
                    data.getName(),
                    data.getMarks(),
                    data.getCourse()
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error calculating hash", e);
            return "";
        }
    }

    private String determineStatus(int riskScore) {
        if (riskScore <= 20) {
            return "VALID";
        } else if (riskScore <= 50) {
            return "SUSPICIOUS";
        } else {
            return "FAKE";
        }
    }
}