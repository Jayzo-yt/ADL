package com.acd.verify.model;

import jakarta.persistence.*;
@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "roll_no", nullable = false, unique = true)
    private String rollNo;

    @Column(name = "cert_id", nullable = false, unique = true)
    private String certId;

    @Column(nullable = false)
    private String course;

    @Column(name = "cgpa", nullable = false)
    private Double cgpa;

    @Column(nullable = false)
    private String university;

    @Column(name = "hash_value")
    private String hashValue;

    @Column(name = "issue_date")
    private String issueDate;

    public Certificate() {
    }

    public Certificate(Long id, String name, String rollNo, String certId, String course, Double cgpa, String university, String hashValue, String issueDate) {
        this.id = id;
        this.name = name;
        this.rollNo = rollNo;
        this.certId = certId;
        this.course = course;
        this.cgpa = cgpa;
        this.university = university;
        this.hashValue = hashValue;
        this.issueDate = issueDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getHashValue() { return hashValue; }
    public void setHashValue(String hashValue) { this.hashValue = hashValue; }
    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
}