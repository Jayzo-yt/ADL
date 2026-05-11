package com.acd.verify.controller;

import com.acd.verify.model.UserRegistrationDto;
import com.acd.verify.service.ActivityLogService;
import com.acd.verify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        return "redirect:/register/student";
    }

    // Institution self-registration is disabled.
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Institution registration is disabled. Admin creates institution accounts.");
        return "redirect:/login";
    }

    @GetMapping("/register/student")
    public String showStudentRegistrationPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "student-register";
    }

    @PostMapping("/register/student")
    public String registerStudent(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "student-register";
        }

        try {
            userService.registerStudent(registrationDto);
            activityLogService.log("STUDENT_REGISTERED", registrationDto.getUsername(),
                    "New student registered: " + registrationDto.getFullName());
            redirectAttributes.addFlashAttribute("success", "Student registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "student-register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            for (var authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) return "redirect:/admin/dashboard";
                if ("ROLE_STUDENT".equals(role)) return "redirect:/student/dashboard";
                if ("ROLE_USER".equals(role)) return "redirect:/university/dashboard";
            }
        }
        return "redirect:/university/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}