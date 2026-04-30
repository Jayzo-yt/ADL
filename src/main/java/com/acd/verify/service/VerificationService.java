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

import java.util.Locale;
import java.util.Optional;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private CertificateHashService certificateHashService;

    public VerificationResult verify(ExtractedData extractedData, double ocrAverageConfidence) {
        VerificationResult result = new VerificationResult();
        result.setExtractedData(extractedData);

        if (extractedData.getCertId() == null || extractedData.getCertId().isEmpty()) {
            result.addIssue("Certificate ID not found in document");
                result.setRiskScore(riskScoringService.calculateScore(result, ocrAverageConfidence));
            result.setStatus(determineStatus(result.getRiskScore()));
            return result;
        }

        Optional<Certificate> certOpt = certificateRepository.findByCertId(extractedData.getCertId());

        if (certOpt.isEmpty()) {
            result.addIssue("Certificate ID not found in database");
            result.setRiskScore(riskScoringService.calculateScore(result, ocrAverageConfidence));
            result.setStatus(determineStatus(result.getRiskScore()));
            return result;
        }

        Certificate dbCert = certOpt.get();
        result.setMatchedCertificate(dbCert);

        verifyRollNumber(extractedData, dbCert, result);
        verifyName(extractedData, dbCert, result);
        verifyCgpa(extractedData, dbCert, result);
        verifyCourse(extractedData, dbCert, result);
        verifyUniversity(extractedData, dbCert, result);
        verifyHash(extractedData, dbCert, result);

        result.setRiskScore(riskScoringService.calculateScore(result, ocrAverageConfidence));
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
            String extractedName = normalizeText(extracted.getName());
            String dbName = normalizeText(dbCert.getName());

            if (extractedName.isEmpty() || dbName.isEmpty() || extractedName.equals(dbName)) {
                return;
            }

            LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();
            int distance = ld.apply(extractedName, dbName);

            if (distance > 3) {
                result.addIssue("Name mismatch: Expected " + dbCert.getName() +
                        ", Found " + extracted.getName());
            }
        }
    }

    private void verifyCgpa(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getCgpa() != null &&
                !extracted.getCgpa().equals(dbCert.getCgpa())) {
            result.addIssue("CGPA mismatch: Expected " + dbCert.getCgpa() +
                    ", Found " + extracted.getCgpa());
        }
    }

    private void verifyCourse(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getCourse() != null && dbCert.getCourse() != null) {
            String extractedCourse = normalizeText(extracted.getCourse());
            String dbCourse = normalizeText(dbCert.getCourse());

            if (!extractedCourse.contains(dbCourse) && !dbCourse.contains(extractedCourse)) {
            result.addIssue("Course mismatch: Expected " + dbCert.getCourse() +
                    ", Found " + extracted.getCourse());
            }
        }
    }

    private void verifyUniversity(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        if (extracted.getUniversity() != null && dbCert.getUniversity() != null) {
            String extractedUniversity = normalizeText(extracted.getUniversity());
            String dbUniversity = normalizeText(dbCert.getUniversity());

            if (!extractedUniversity.contains(dbUniversity) && !dbUniversity.contains(extractedUniversity)) {
            result.addIssue("University mismatch: Expected " + dbCert.getUniversity() +
                    ", Found " + extracted.getUniversity());
            }
        }
    }

    private void verifyHash(ExtractedData extracted, Certificate dbCert, VerificationResult result) {
        String extractedHash = certificateHashService.calculateHash(extracted);
        String dbCanonicalHash = certificateHashService.calculateHash(dbCert);
        String storedHash = dbCert.getHashValue();

        // Backward compatibility: older rows may contain non-canonical hash values.
        // Prefer canonical comparison between DB fields and extracted fields.
        boolean hashMatches = extractedHash.equals(dbCanonicalHash)
                || (storedHash != null && !storedHash.isEmpty() && extractedHash.equals(storedHash));

        if (!hashMatches) {
            result.addIssue("Document hash mismatch - possible tampering detected");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "")
                .trim();
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