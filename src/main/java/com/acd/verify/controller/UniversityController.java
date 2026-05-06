package com.acd.verify.controller;

import com.acd.verify.model.Certificate;
import com.acd.verify.repository.CertificateRepository;
import com.acd.verify.service.ActivityLogService;
import com.acd.verify.service.CertificateHashService;
import com.acd.verify.service.QRCodeService;
import com.acd.verify.service.UserService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/university")
public class UniversityController {

    @Autowired
    private UserService userService;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateHashService certificateHashService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        var user = userService.findByUsername(username);

        model.addAttribute("user", user);
        if (!model.containsAttribute("certificate")) {
            model.addAttribute("certificate", new Certificate());
        }

        // Load certificates for this institution
        String org = user.getOrganization();
        List<Certificate> certificates = (org != null && !org.isBlank())
                ? certificateRepository.findByUniversity(org)
                : List.of();
        model.addAttribute("certificates", certificates);
        model.addAttribute("totalCertificates", certificates.size());

        return "university-dashboard";
    }

    @PostMapping("/certificates/add")
    public String addCertificate(@ModelAttribute("certificate") @NotNull Certificate certificate,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userService.findByUsername(auth.getName());

        if (isBlank(certificate.getCertId())) {
            certificate.setCertId(generateUniqueCertificateId(user.getOrganization()));
        } else {
            certificate.setCertId(certificate.getCertId().trim());
        }

        if (isBlank(certificate.getUniversity()) && !isBlank(user.getOrganization())) {
            certificate.setUniversity(user.getOrganization());
        }

        if (isBlank(certificate.getIssueDate())) {
            certificate.setIssueDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        }

        if (!isBlank(certificate.getStudentEmail())) {
            certificate.setStudentEmail(certificate.getStudentEmail().trim().toLowerCase(Locale.ROOT));
        }

        if (isBlank(certificate.getCertId()) || isBlank(certificate.getRollNo()) || isBlank(certificate.getName()) ||
                isBlank(certificate.getCourse()) || certificate.getCgpa() == null || isBlank(certificate.getUniversity()) ||
                isBlank(certificate.getStudentEmail())) {
            redirectAttributes.addFlashAttribute("error", "Please fill all required certificate fields.");
            redirectAttributes.addFlashAttribute("certificate", certificate);
            return "redirect:/university/dashboard";
        }

        if (certificateRepository.existsByCertId(certificate.getCertId())) {
            redirectAttributes.addFlashAttribute("error", "Certificate ID already exists.");
            redirectAttributes.addFlashAttribute("certificate", certificate);
            return "redirect:/university/dashboard";
        }

        if (certificateRepository.existsByRollNo(certificate.getRollNo())) {
            redirectAttributes.addFlashAttribute("error", "Roll number already exists.");
            redirectAttributes.addFlashAttribute("certificate", certificate);
            return "redirect:/university/dashboard";
        }

        // Generate hash
        certificate.setHashValue(certificateHashService.calculateHash(certificate));

        // Generate QR code
        String qrBase64 = qrCodeService.generateQRCodeBase64(certificate.getCertId());
        certificate.setQrCodeData(qrBase64);

        // Set status
        certificate.setStatus("ACTIVE");

        certificateRepository.save(certificate);

        // Log activity
        activityLogService.log("CERTIFICATE_UPLOADED", auth.getName(),
                "Certificate uploaded: " + certificate.getCertId() + " for " + certificate.getName());

        redirectAttributes.addFlashAttribute("success", "Certificate uploaded successfully with QR code generated.");
        return "redirect:/university/dashboard";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String generateUniqueCertificateId(String organization) {
        String prefix = isBlank(organization)
                ? "CERT"
                : organization.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);

        String generatedId;
        do {
            String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
            generatedId = prefix + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + randomPart;
        } while (certificateRepository.existsByCertId(generatedId));

        return generatedId;
    }
}