package com.acd.verify.controller;

import com.acd.verify.model.Certificate;
import com.acd.verify.model.VerificationLog;
import com.acd.verify.repository.CertificateRepository;
import com.acd.verify.repository.VerificationLogRepository;
import com.acd.verify.service.CertificateHashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private VerificationLogRepository verificationLogRepository;

    @Autowired
    private CertificateHashService certificateHashService;

    @GetMapping("/logs")
    public String viewLogs(Model model) {
        List<VerificationLog> logs = verificationLogRepository.findAllByOrderByTimestampDesc();
        model.addAttribute("logs", logs);
        return "admin";
    }

    @GetMapping("/certificates")
    @ResponseBody
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    @PostMapping("/certificates/add")
    @ResponseBody
    public Certificate addCertificate(@RequestBody Certificate certificate) {
        // Basic validation
        if (certificate.getCertId() == null || certificate.getCertId().isBlank()) {
            throw new RuntimeException("certId required");
        }

        String hash = certificateHashService.calculateHash(certificate);
        certificate.setHashValue(hash);

        return certificateRepository.save(certificate);
    }
}