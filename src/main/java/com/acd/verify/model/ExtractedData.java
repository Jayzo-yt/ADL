package com.acd.verify.model;

public class ExtractedData {
    private String name;
    private String rollNo;
    private String certId;
    private String course;
    private Double cgpa;
    private String university;
    private String issueDate;
    private String rawText;
    private String normalizedText;

    public ExtractedData() {
    }

    public ExtractedData(String name, String rollNo, String certId, String course, Double cgpa, String university, String issueDate, String rawText) {
        this.name = name;
        this.rollNo = rollNo;
        this.certId = certId;
        this.course = course;
        this.cgpa = cgpa;
        this.university = university;
        this.issueDate = issueDate;
        this.rawText = rawText;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getCertId() { return certId; }
    public void setCertId(String certId) { this.certId = certId; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public Double getCgpa() { return cgpa; }
    public void setCgpa(Double cgpa) { this.cgpa = cgpa; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public String getNormalizedText() { return normalizedText; }
    public void setNormalizedText(String normalizedText) { this.normalizedText = normalizedText; }
}