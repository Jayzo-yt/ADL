package com.acd.verify.controller;

import com.acd.verify.model.ExtractedData;
import com.acd.verify.model.VerificationLog;
import com.acd.verify.model.VerificationResult;
import com.acd.verify.repository.VerificationLogRepository;
import com.acd.verify.service.DataExtractionService;
import com.acd.verify.service.OCRService;
import com.acd.verify.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    @Autowired
    private OCRService ocrService;

    @Autowired
    private DataExtractionService dataExtractionService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private VerificationLogRepository verificationLogRepository;

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/home")
    public String homePage() { return "index"; }

    @PostMapping("/upload")
    public String uploadCertificate(@RequestParam("certificate") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
            auth.getAuthorities().stream().noneMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
        String returnPage = isAuthenticated ? "/university/dashboard" : "/home";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:" + returnPage;
        }

        // Basic input validation
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        long maxSize = 5L * 1024L * 1024L; // 5MB
        if (!(contentType.contains("pdf") || contentType.contains("jpeg") || contentType.contains("jpg") || contentType.contains("png"))) {
            redirectAttributes.addFlashAttribute("error", "Unsupported file type. Allowed: PDF, JPG, PNG");
            return "redirect:" + returnPage;
        }

        if (file.getSize() > maxSize) {
            redirectAttributes.addFlashAttribute("error", "File too large. Max 5MB");
            return "redirect:" + returnPage;
        }

        try {
            logger.info("Processing file: {}", file.getOriginalFilename());

            var ocrResult = ocrService.extractWithConfidence(file);

            ExtractedData extractedData = dataExtractionService.extractFields(ocrResult.getText());

            VerificationResult result = verificationService.verify(extractedData, ocrResult.getAverageConfidence());

            String username = isAuthenticated ? auth.getName() : "PUBLIC";

            VerificationLog log = new VerificationLog();
            log.setCertId(extractedData.getCertId());
            log.setResult(result.getStatus());
            log.setRiskScore(result.getRiskScore());
            log.setIssues(String.join("; ", result.getIssues()));
            log.setTimestamp(LocalDateTime.now());
            log.setUsername(username);
            log.setUploadedFileName(file.getOriginalFilename());

            log = verificationLogRepository.save(log);
            result.setVerificationLogId(log.getId());

            redirectAttributes.addFlashAttribute("result", result);

            return "redirect:/result/" + log.getId();

        } catch (Exception e) {
    logger.error("Error processing certificate", e);

    redirectAttributes.addFlashAttribute(
        "error",
        "Error processing certificate: " + e.getMessage()
    );

    return "redirect:" + returnPage; }
  }

    @GetMapping("/result/{id}")
    public String showResult(@PathVariable Long id, Model model) {
        VerificationLog log = verificationLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification log not found"));

        model.addAttribute("log", log);
        return "result";
    }
}