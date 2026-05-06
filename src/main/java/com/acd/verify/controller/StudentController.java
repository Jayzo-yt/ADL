package com.acd.verify.controller;

import com.acd.verify.model.Certificate;
import com.acd.verify.repository.CertificateRepository;
import com.acd.verify.service.ActivityLogService;
import com.acd.verify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByUsername(auth.getName());

        List<Certificate> certificates = findCertificatesForStudent(currentUser.getUsername(), currentUser.getEmail());

        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("fullName", currentUser.getFullName());
        model.addAttribute("email", currentUser.getEmail());
        model.addAttribute("certificates", certificates);
        model.addAttribute("totalCertificates", certificates.size());

        return "student-dashboard";
    }

    @GetMapping("/certificate/{certId}")
    public String viewCertificate(@PathVariable String certId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByUsername(auth.getName());

        Certificate certificate = certificateRepository.findByCertId(certId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        if (!ownsCertificate(certificate, currentUser.getUsername(), currentUser.getEmail())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("certificate", certificate);
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("email", currentUser.getEmail());

        activityLogService.log("CERTIFICATE_VIEWED", currentUser.getUsername(), "Student viewed certificate: " + certId);

        return "student-certificate";
    }

    @GetMapping("/certificate/{certId}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String certId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = userService.findByUsername(auth.getName());

        Certificate certificate = certificateRepository.findByCertId(certId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        if (!ownsCertificate(certificate, currentUser.getUsername(), currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("You do not have access to this certificate".getBytes(StandardCharsets.UTF_8));
        }

        String downloadText = buildDownloadText(certificate);
        String fileName = certificate.getCertId().replaceAll("[^A-Za-z0-9._-]", "_") + "-certificate.txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(downloadText.getBytes(StandardCharsets.UTF_8));
    }

    private List<Certificate> findCertificatesForStudent(String username, String email) {
        Map<Long, Certificate> certificates = new LinkedHashMap<>();

        if (username != null && !username.isBlank()) {
            addCertificates(certificates, certificateRepository.findByStudentEmail(username));
        }
        if (email != null && !email.isBlank()) {
            addCertificates(certificates, certificateRepository.findByStudentEmail(email));
        }

        return new ArrayList<>(certificates.values());
    }

    private void addCertificates(Map<Long, Certificate> certificates, List<Certificate> source) {
        for (Certificate certificate : source) {
            if (certificate != null && certificate.getId() != null) {
                certificates.putIfAbsent(certificate.getId(), certificate);
            }
        }
    }

    private boolean ownsCertificate(Certificate certificate, String username, String email) {
        if (certificate == null) {
            return false;
        }
        return matches(certificate.getStudentEmail(), username) || matches(certificate.getStudentEmail(), email);
    }

    private boolean matches(String storedValue, String candidate) {
        return storedValue != null && candidate != null && storedValue.trim().equalsIgnoreCase(candidate.trim());
    }

    private String buildDownloadText(Certificate certificate) {
        return "Certificate Summary\n"
                + "-------------------\n"
                + "Certificate ID: " + safe(certificate.getCertId()) + "\n"
                + "Student Name: " + safe(certificate.getName()) + "\n"
                + "Roll Number: " + safe(certificate.getRollNo()) + "\n"
                + "Course: " + safe(certificate.getCourse()) + "\n"
                + "CGPA / Percentage: " + safe(certificate.getCgpa()) + "\n"
                + "Institution: " + safe(certificate.getUniversity()) + "\n"
                + "Issue Date: " + safe(certificate.getIssueDate()) + "\n"
                + "Student Email: " + safe(certificate.getStudentEmail()) + "\n"
                + "Status: " + safe(certificate.getStatus()) + "\n"
                + "Hash: " + safe(certificate.getHashValue()) + "\n";
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
