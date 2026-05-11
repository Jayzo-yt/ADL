package com.acd.verify.service;

import com.acd.verify.model.Institution;
import com.acd.verify.model.User;
import com.acd.verify.model.UserRole;
import com.acd.verify.repository.InstitutionRepository;
import com.acd.verify.repository.RoleRepository;
import com.acd.verify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class InstitutionService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Institution> findAll() {
        return institutionRepository.findAll();
    }

    public List<Institution> findEnabled() {
        return institutionRepository.findByEnabled(true);
    }

    public Optional<Institution> findById(Long id) {
        return institutionRepository.findById(id);
    }

    public Optional<Institution> findByCode(String code) {
        return institutionRepository.findByCode(code);
    }

    public Institution createInstitution(Institution institution) {
        if (institutionRepository.existsByCode(institution.getCode())) {
            throw new RuntimeException("Institution code already exists: " + institution.getCode());
        }
        return institutionRepository.save(institution);
    }

    @Transactional
    public Institution createInstitutionWithAccount(Institution institution, String username, String rawPassword) {
        if (institutionRepository.existsByCode(institution.getCode())) {
            throw new RuntimeException("Institution code already exists: " + institution.getCode());
        }
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Institution login username is required");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new RuntimeException("Institution password must be at least 6 characters");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Login username already exists: " + username);
        }
        if (institution.getEmail() != null && !institution.getEmail().isBlank() && userRepository.existsByEmail(institution.getEmail())) {
            throw new RuntimeException("Email already used by another account: " + institution.getEmail());
        }

        Institution savedInstitution = institutionRepository.save(institution);

        User institutionUser = new User();
        institutionUser.setUsername(username.trim());
        institutionUser.setEmail((savedInstitution.getEmail() == null || savedInstitution.getEmail().isBlank())
                ? (username.trim() + "@institution.local")
                : savedInstitution.getEmail().trim());
        institutionUser.setPassword(passwordEncoder.encode(rawPassword));
        institutionUser.setFullName(savedInstitution.getName() + " Admin");
        institutionUser.setOrganization(savedInstitution.getName());
        institutionUser.setInstitutionId(savedInstitution.getId());
        institutionUser.setEnabled(savedInstitution.isEnabled());

        Set<UserRole> roles = new HashSet<>();
        UserRole institutionRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new UserRole("ROLE_USER")));
        roles.add(institutionRole);
        institutionUser.setRoles(roles);

        userRepository.save(institutionUser);
        return savedInstitution;
    }

    public Institution updateInstitution(Long id, Institution updated) {
        Institution existing = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        return institutionRepository.save(existing);
    }

    public void toggleEnabled(Long id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        institution.setEnabled(!institution.isEnabled());
        institutionRepository.save(institution);
    }

    public void deleteInstitution(Long id) {
        institutionRepository.deleteById(id);
    }

    public long count() {
        return institutionRepository.count();
    }
}
