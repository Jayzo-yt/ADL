package com.acd.verify.service;

import com.acd.verify.model.UserRegistrationDto;
import com.acd.verify.model.UserRole;
import com.acd.verify.repository.RoleRepository;
import com.acd.verify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserRole registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        UserRole user = new UserRole();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setOrganization(registrationDto.getOrganization());
        user.setEnabled(true);

        // Assign default USER role
        Set<UserRole> roles = new HashSet<>();
        UserRole userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    UserRole newRole = new UserRole("ROLE_USER");
                    return roleRepository.save(newRole);
                });
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public UserRole findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}