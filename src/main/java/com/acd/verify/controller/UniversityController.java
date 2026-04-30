package com.acd.verify.controller;

import com.acd.verify.model.Certificate;
import com.acd.verify.repository.CertificateRepository;
import com.acd.verify.service.CertificateHashService;
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

@Controller
@RequestMapping("/university")
public class UniversityController {

    @Autowired
    private UserService userService;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateHashService certificateHashService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        var user = userService.findByUsername(username);

        model.addAttribute("user", user);
        if (!model.containsAttribute("certificate")) {
            model.addAttribute("certificate", new Certificate());
        }
        return "university-dashboard";
    }

    @PostMapping("/certificates/add")
    public String addCertificate(@ModelAttribute("certificate") @NotNull Certificate certificate,
                                 RedirectAttributes redirectAttributes) {
        if (isBlank(certificate.getCertId()) || isBlank(certificate.getRollNo()) || isBlank(certificate.getName()) ||
                isBlank(certificate.getCourse()) || certificate.getCgpa() == null || isBlank(certificate.getUniversity())) {
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

        certificate.setHashValue(certificateHashService.calculateHash(certificate));
        certificateRepository.save(certificate);

        redirectAttributes.addFlashAttribute("success", "Certificate details uploaded successfully.");
        return "redirect:/university/dashboard";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}