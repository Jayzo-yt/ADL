package com.acd.verify.service;

import com.acd.verify.model.ExtractedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(DataExtractionService.class);

    // --- Precompiled Regex Patterns (Thread-safe & Fast) ---
    
    private static final Pattern CERT_ID_PATTERN_1 = Pattern.compile(
            "CERTIFICATE\\s+(?:NO\\.?|NUMBER|ID)\\s*[:\\-]?\\s*([A-Z0-9]{4,20})", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern CERT_ID_PATTERN_2 = Pattern.compile(
            "\\b([A-Z]{2,4}[0-9]{4,10})\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern ROLL_NO_PATTERN = Pattern.compile(
            "(?:ROLL|REGISTRATION|REG)\\s*(?:NO\\.?|NUMBER)\\s*[:\\-]?\\s*([A-Z0-9]{4,20})", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?:NAME|STUDENT NAME)\\s*[:\\-]?\\s*([A-Z][A-Z\\s.]{2,40})(?=\\s|$|ROLL|ID|COURSE|DATE)", Pattern.CASE_INSENSITIVE);

    private static final Pattern UNIVERSITY_PATTERN = Pattern.compile(
            "([A-Z][A-Z\\s]{5,60}UNIVERSITY)", Pattern.CASE_INSENSITIVE);

    private static final Pattern CGPA_PATTERN = Pattern.compile(
            "(?:CGPA|GPA|MARKS?|PERCENTAGE)\\s*[:\\-]?\\s*([0-9]{1,3}(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4})\\b|\\b([0-9]{1,2}\\s+(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[A-Z]*\\s+[0-9]{4})\\b", Pattern.CASE_INSENSITIVE);

    private static final String[] COURSE_KEYWORDS = {
            "BACHELOR OF TECHNOLOGY", "B.TECH", "BTECH", "MASTER OF TECHNOLOGY", "M.TECH", 
            "BACHELOR OF SCIENCE", "B.SC", "MASTER OF SCIENCE", "M.SC",
            "BACHELOR OF ARTS", "B.A.", "MASTER OF BUSINESS ADMINISTRATION", "MBA",
            "BACHELOR OF COMMERCE", "B.COM", "MASTER OF COMPUTER APPLICATIONS", "MCA"
    };

    public ExtractedData extractFields(String rawText) {
        ExtractedData data = new ExtractedData();
        data.setRawText(rawText);

        if (rawText == null || rawText.isBlank()) {
            logger.warn("Input text is empty");
            return data;
        }

        // Clean text but keep numbers and casing for specific extractions
        String cleanedText = rawText.replaceAll("\\s+", " ").trim();
        String upperText = cleanedText.toUpperCase();

        logger.debug("Starting extraction on cleaned text");

        data.setCertId(extractCertId(upperText));
        data.setRollNo(extractRollNo(upperText));
        data.setName(extractName(cleanedText)); // Extract from cleaned original to keep "John Doe"
        data.setCourse(extractCourse(upperText));
        data.setCgpa(extractCgpa(upperText));
        data.setUniversity(extractUniversity(cleanedText)); // Keep original casing
        data.setIssueDate(extractDate(upperText));
        data.setNormalizedText(upperText);

        return data;
    }

    private String extractCertId(String text) {
        String result = matchGroup(CERT_ID_PATTERN_1, text, 1);
        if (result == null) {
            result = matchGroup(CERT_ID_PATTERN_2, text, 1);
            // Validation: Ensure it's not a known false positive
            if (result != null && (result.contains("ROLL") || result.contains("REG"))) return null;
        }
        return result;
    }

    private String extractRollNo(String text) {
        return matchGroup(ROLL_NO_PATTERN, text, 1);
    }

    private String extractName(String text) {
        String name = matchGroup(NAME_PATTERN, text, 1);
        if (name != null) {
            // Remove common OCR artifacts or trailing keywords
            return name.replaceAll("(?i)(ROLL|NO|ID|COURSE|UNIVERSITY).*", "").trim();
        }
        // Fallback: Check "This is to certify that..."
        Pattern certPattern = Pattern.compile("(?:CERTIFY THAT|AWARDED TO)\\s+([A-Z][A-Z\\s]{2,40})", Pattern.CASE_INSENSITIVE);
        return matchGroup(certPattern, text, 1);
    }

    private String extractCourse(String text) {
        // High specificity search first
        for (String course : COURSE_KEYWORDS) {
            if (text.contains(course)) return course;
        }
        return null;
    }

    private Double extractCgpa(String text) {
        String val = matchGroup(CGPA_PATTERN, text, 1);
        if (val != null) {
            try {
                double d = Double.parseDouble(val);
                // Basic validation: CGPA usually <= 10, Percentage <= 100
                if (d > 0 && d <= 100) return d;
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String extractUniversity(String text) {
        String uni = matchGroup(UNIVERSITY_PATTERN, text, 1);
        if (uni != null && !uni.toUpperCase().contains("BACHELOR") && !uni.toUpperCase().contains("MASTER")) {
            return uni.trim();
        }
        return null;
    }

    private String extractDate(String text) {
        Matcher m = DATE_PATTERN.matcher(text);
        if (m.find()) {
            String g1 = m.group(1);
            String g2 = m.group(2);
            if (g1 != null) return g1.trim();
            if (g2 != null) return g2.trim();
        }
        return null;
    }

    /**
     * Helper to extract a specific group from a pattern
     */
    private String matchGroup(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String g = matcher.group(group);
            return (g == null) ? null : g.trim();
        }
        return null;
    }
}