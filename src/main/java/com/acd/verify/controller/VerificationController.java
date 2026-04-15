package com.acd.verify.controller;

import com.acd.verify.model.ExtractedData;
import com.acd.verify.model.VerificationLog;
import com.acd.verify.model.VerificationResult;
import com.acd.verify.repository.VerificationLogRepository;
import com.acd.verify.service.DataExtractionService;
import com.acd.verify.service.OCRService;
import com.acd.verify.service.UserService;
import com.acd.verify.service.VerificationService;
import net.sourceforge.tess4j.TesseractException;
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

import java.io.IOException;
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

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/home")
    public String homePage() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadCertificate(@RequestParam("certificate") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/dashboard";
        }

        try {
            logger.info("Processing file: {}", file.getOriginalFilename());
            String extractedText = ocrService.extractText(file);

            ExtractedData extractedData = dataExtractionService.extractFields(extractedText);

            VerificationResult result = verificationService.verify(extractedData);

            // Get current logged-in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            VerificationLog log = new VerificationLog();
            log.setCertId(extractedData.getCertId());
            log.setResult(result.getStatus());
            log.setRiskScore(result.getRiskScore());
            log.setIssues(String.join("; ", result.getIssues()));
            log.setTimestamp(LocalDateTime.now());
            log.setUploadedFileName(file.getOriginalFilename());

            log = verificationLogRepository.save(log);
            result.setVerificationLogId(log.getId());

            redirectAttributes.addFlashAttribute("result", result);

            return "redirect:/result/" + log.getId();

        } catch (IOException | TesseractException e) {
            logger.error("Error processing certificate", e);
            redirectAttributes.addFlashAttribute("error", "Error processing certificate: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/result/{id}")
    public String showResult(@PathVariable Long id, Model model) {
        VerificationLog log = verificationLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification log not found"));

        model.addAttribute("log", log);
        return "result";
    }
}