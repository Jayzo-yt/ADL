package com.acd.verify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}