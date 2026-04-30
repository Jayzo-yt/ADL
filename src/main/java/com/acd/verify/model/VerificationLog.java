package com.acd.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_logs")
public class VerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cert_id")
    private String certId;

    @Column(nullable = false)
    private String result;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(columnDefinition = "TEXT")
    private String issues;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "uploaded_file_name")
    private String uploadedFileName;
    
    @Column(name = "username")
    private String username;

    public VerificationLog() {
    }

    public VerificationLog(Long id, String certId, String result, Integer riskScore, String issues, LocalDateTime timestamp, String uploadedFileName) {
        this.id = id;
        this.certId = certId;
        this.result = result;
        this.riskScore = riskScore;
        this.issues = issues;
        this.timestamp = timestamp;
        this.uploadedFileName = uploadedFileName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCertId() { return certId; }
    public void setCertId(String certId) { this.certId = certId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public String getIssues() { return issues; }
    public void setIssues(String issues) { this.issues = issues; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUploadedFileName() { return uploadedFileName; }
    public void setUploadedFileName(String uploadedFileName) { this.uploadedFileName = uploadedFileName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}