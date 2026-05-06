package com.acd.verify.config;

import com.acd.verify.model.User;
import com.acd.verify.model.UserRole;
import com.acd.verify.repository.RoleRepository;
import com.acd.verify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Ensure all roles exist
        createRoleIfNotExists("ROLE_ADMIN", "System Administrator");
        createRoleIfNotExists("ROLE_USER", "Institution User");
        createRoleIfNotExists("ROLE_STUDENT", "Student");

        // Create default admin if none exists
        if (userRepository.findAllByRoles_Name("ROLE_ADMIN").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@certiverify.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setEnabled(true);

            Set<UserRole> roles = new HashSet<>();
            roles.add(roleRepository.findByName("ROLE_ADMIN").orElseThrow());
            admin.setRoles(roles);

            userRepository.save(admin);
            logger.info("Default admin account created (admin / admin123)");
        }
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            UserRole role = new UserRole();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            logger.info("Created role: {}", name);
        }
    }
}
