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

    public ExtractedData extractFields(String rawText) {
        ExtractedData data = new ExtractedData();
        data.setRawText(rawText);

        // Normalize text
        String normalizedText = rawText.toUpperCase().replaceAll("\\s+", " ");

        // Extract Certificate ID
        data.setCertId(extractCertificateId(normalizedText));

        // Extract Roll Number
        data.setRollNo(extractRollNumber(normalizedText));

        // Extract Name
        data.setName(extractName(normalizedText));

        // Extract Course
        data.setCourse(extractCourse(normalizedText));

        // Extract Marks
        data.setMarks(extractMarks(normalizedText));

        // Extract University
        data.setUniversity(extractUniversity(normalizedText));

        // Extract Issue Date
        data.setIssueDate(extractIssueDate(normalizedText));

        logger.info("Extracted data: {}", data);

        return data;
    }

    private String extractCertificateId(String text) {
        Pattern pattern = Pattern.compile("CERT(?:IFICATE)?[\\s\\-_:]*(?:ID|NO|NUMBER)?[\\s\\-_:]*([A-Z0-9]{4,20})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractRollNumber(String text) {
        Pattern pattern = Pattern.compile("ROLL[\\s\\-_]*(?:NO|NUMBER)?[\\s\\-_:]*([A-Z0-9]{4,20})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractName(String text) {
        Pattern pattern = Pattern.compile("(?:NAME|STUDENT)[\\s\\-_:]*([A-Z][A-Z\\s]{5,50})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        pattern = Pattern.compile("(?:CERTIFY THAT|AWARDED TO)[\\s\\-_:]*([A-Z][A-Z\\s]{5,50})");
        matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractCourse(String text) {
        String[] courseKeywords = {
                "BACHELOR OF TECHNOLOGY", "B.TECH", "BTECH",
                "BACHELOR OF SCIENCE", "B.SC", "BSC",
                "MASTER OF TECHNOLOGY", "M.TECH", "MTECH",
                "BACHELOR OF ARTS", "B.A", "BA",
                "MASTER OF BUSINESS ADMINISTRATION", "MBA"
        };

        for (String course : courseKeywords) {
            if (text.contains(course)) {
                return course;
            }
        }

        return null;
    }

    private Double extractMarks(String text) {
        Pattern pattern = Pattern.compile("(?:MARKS?|PERCENTAGE|CGPA|SCORE)[\\s\\-_:]*([0-9]{1,3}\\.?[0-9]{0,2})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse marks: {}", matcher.group(1));
            }
        }

        return null;
    }

    private String extractUniversity(String text) {
        Pattern pattern = Pattern.compile("([A-Z\\s]{10,60}UNIVERSITY)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractIssueDate(String text) {
        Pattern pattern = Pattern.compile("(?:DATE|ISSUED)[\\s\\-_:]*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{4})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
}