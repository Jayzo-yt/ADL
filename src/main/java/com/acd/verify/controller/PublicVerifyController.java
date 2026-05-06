package com.acd.verify.controller;

import com.acd.verify.model.Certificate;
import com.acd.verify.repository.CertificateRepository;
import com.acd.verify.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/verify")
public class PublicVerifyController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping
    public String verifyPage(@RequestParam(value = "id", required = false) String certId, Model model) {
        return renderVerification(certId, model);
    }

    @GetMapping("/{certId}")
    public String verifyPageByPath(@PathVariable String certId, Model model) {
        return renderVerification(certId, model);
    }

    private String renderVerification(String certId, Model model) {
        if (certId != null && !certId.isBlank()) {
            Optional<Certificate> certOpt = certificateRepository.findByCertId(certId.trim());

            if (certOpt.isPresent()) {
                Certificate cert = certOpt.get();
                boolean isActive = "ACTIVE".equalsIgnoreCase(cert.getStatus());
                model.addAttribute("certificate", cert);
                model.addAttribute("verificationStatus", isActive ? "VALID" : "REVOKED");
                model.addAttribute("statusClass", isActive ? "valid" : "invalid");
                model.addAttribute("verificationMessage", isActive
                        ? "Certificate found and marked active."
                        : "Certificate found but is not active.");

                activityLogService.log("PUBLIC_VERIFICATION", "PUBLIC",
                        "Certificate verified: " + certId + " — " + (isActive ? "VALID" : "REVOKED"));
            } else {
                model.addAttribute("verificationStatus", "INVALID");
                model.addAttribute("statusClass", "invalid");
                model.addAttribute("errorMessage", "No certificate found with ID: " + certId);

                activityLogService.log("PUBLIC_VERIFICATION", "PUBLIC",
                        "Certificate verification failed: " + certId + " — NOT FOUND");
            }

            model.addAttribute("searchedId", certId);
        }

        return "verify";
    }
}
