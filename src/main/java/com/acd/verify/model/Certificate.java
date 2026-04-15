package com.acd.verify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private Double marks;

    @Column(nullable = false)
    private String university;

    @Column(name = "hash_value")
    private String hashValue;

    @Column(name = "issue_date")
    private String issueDate;
}