package com.acd.verify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedData {
    private String name;
    private String rollNo;
    private String certId;
    private String course;
    private Double marks;
    private String university;
    private String issueDate;
    private String rawText;
}