package com.acd.verify.service;

import com.acd.verify.model.Certificate;
import com.acd.verify.model.ExtractedData;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Service
public class CertificateHashService {

    public String calculateHash(Certificate cert) {
        return calculateHash(
                cert.getCertId(),
                cert.getRollNo(),
                cert.getName(),
                cert.getCgpa(),
                cert.getCourse()
        );
    }

    public String calculateHash(ExtractedData data) {
        return calculateHash(
                data.getCertId(),
                data.getRollNo(),
                data.getName(),
                data.getCgpa(),
                data.getCourse()
        );
    }

    private String calculateHash(String certId, String rollNo, String name, Double cgpa, String course) {
        try {
            String canonical = canonicalize(certId)
                    + "|" + canonicalize(rollNo)
                    + "|" + canonicalize(name)
                    + "|" + canonicalizeNumber(cgpa)
                    + "|" + canonicalizeCourse(course);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private String canonicalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    private String canonicalizeNumber(Double value) {
        if (value == null) {
            return "";
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        DecimalFormat format = new DecimalFormat("0.##", symbols);
        return format.format(value);
    }

    private String canonicalizeCourse(String course) {
        String normalized = canonicalize(course);
        if (normalized.contains("BACHELOROFTECHNOLOGY") || normalized.contains("BTECH")) return "BTECH";
        if (normalized.contains("MASTEROFTECHNOLOGY") || normalized.contains("MTECH")) return "MTECH";
        if (normalized.contains("BACHELOROFSCIENCE") || normalized.contains("BSC")) return "BSC";
        if (normalized.contains("MASTEROFSCIENCE") || normalized.contains("MSC")) return "MSC";
        if (normalized.contains("BACHELOROFARTS") || normalized.contains("BA")) return "BA";
        if (normalized.contains("BACHELOROFCOMMERCE") || normalized.contains("BCOM")) return "BCOM";
        if (normalized.contains("MASTEROFBUSINESSADMINISTRATION") || normalized.contains("MBA")) return "MBA";
        if (normalized.contains("MASTEROFCOMPUTERAPPLICATIONS") || normalized.contains("MCA")) return "MCA";
        return normalized;
    }
}