package com.acd.verify.controller;

import com.acd.verify.model.Institution;
import com.acd.verify.model.Certificate;
import com.acd.verify.model.User;
import com.acd.verify.model.VerificationLog;
import com.acd.verify.repository.CertificateRepository;
import com.acd.verify.repository.VerificationLogRepository;
import com.acd.verify.service.ActivityLogService;
import com.acd.verify.service.CertificateHashService;
import com.acd.verify.service.InstitutionService;
import com.acd.verify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalInstitutions", institutionService.count());
        model.addAttribute("totalCertificates", certificateRepository.count());
        model.addAttribute("totalStudents", userService.countByRole("ROLE_STUDENT"));
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("recentActivity", activityLogService.getRecentActivity());
        model.addAttribute("recentLogs", verificationLogRepository.findAllByOrderByTimestampDesc().stream().limit(10).toList());
        return "admin-dashboard";
    }

    @GetMapping("/institutions")
    public String institutions(Model model) {
        model.addAttribute("institutions", institutionService.findAll());
        model.addAttribute("newInstitution", new Institution());
        return "admin-institutions";
    }

    @PostMapping("/institutions/add")
    public String addInstitution(@ModelAttribute("newInstitution") Institution institution,
                                 @RequestParam("institutionUsername") String institutionUsername,
                                 @RequestParam("institutionPassword") String institutionPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            institutionService.createInstitutionWithAccount(institution, institutionUsername, institutionPassword);
            activityLogService.log("INSTITUTION_CREATED", "ADMIN", "Created institution: " + institution.getName() + " with login: " + institutionUsername);
            redirectAttributes.addFlashAttribute("success", "Institution created successfully. Login username: " + institutionUsername);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/institutions";
    }

    @PostMapping("/institutions/{id}/toggle")
    public String toggleInstitution(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            institutionService.toggleEnabled(id);
            activityLogService.log("INSTITUTION_TOGGLED", "ADMIN", "Toggled institution ID: " + id);
            redirectAttributes.addFlashAttribute("success", "Institution status updated.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/institutions";
    }

    @PostMapping("/institutions/{id}/delete")
    public String deleteInstitution(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            institutionService.deleteInstitution(id);
            activityLogService.log("INSTITUTION_DELETED", "ADMIN", "Deleted institution ID: " + id);
            redirectAttributes.addFlashAttribute("success", "Institution deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/institutions";
    }

    @GetMapping("/activity")
    public String activity(Model model) {
        model.addAttribute("activities", activityLogService.getAllActivity());
        return "admin-activity";
    }

    @GetMapping("/logs")
    public String viewLogs(Model model) {
        List<VerificationLog> logs = verificationLogRepository.findAllByOrderByTimestampDesc();
        model.addAttribute("logs", logs);
        return "admin";
    }

    @GetMapping("/users")
    public String viewUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin-users";
    }

    @GetMapping("/certificates")
    @ResponseBody
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    @PostMapping("/certificates/add")
    @ResponseBody
    public Certificate addCertificate(@RequestBody Certificate certificate) {
        if (certificate.getCertId() == null || certificate.getCertId().isBlank()) {
            throw new RuntimeException("certId required");
        }

        String hash = certificateHashService.calculateHash(certificate);
        certificate.setHashValue(hash);

        return certificateRepository.save(certificate);
    }
}