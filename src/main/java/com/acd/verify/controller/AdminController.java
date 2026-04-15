package com.acd.verify.controller;

import com.acd.verify.model.Certificate;
import com.acd.verify.model.VerificationLog;
import com.acd.verify.repository.CertificateRepository;
import com.acd.verify.repository.VerificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private VerificationLogRepository verificationLogRepository;

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
        String hash = calculateHash(certificate);
        certificate.setHashValue(hash);

        return certificateRepository.save(certificate);
    }

    private String calculateHash(Certificate cert) {
        try {
            String dataString = String.format("%s%s%s%s%s",
                    cert.getCertId(),
                    cert.getRollNo(),
                    cert.getName(),
                    cert.getMarks(),
                    cert.getCourse()
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}