package com.acd.verify.service;

import com.acd.verify.model.UserRegistrationDto;
import com.acd.verify.model.User;
import com.acd.verify.model.UserRole;
import com.acd.verify.repository.RoleRepository;
import com.acd.verify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDto registrationDto) {
        return registerWithRole(registrationDto, "ROLE_USER");
    }

    public User registerStudent(UserRegistrationDto registrationDto) {
        return registerWithRole(registrationDto, "ROLE_STUDENT");
    }

    private User registerWithRole(UserRegistrationDto registrationDto, String roleName) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setOrganization(registrationDto.getOrganization());
        user.setEnabled(true);

        Set<UserRole> roles = new HashSet<>();
        UserRole userRole = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    UserRole newRole = new UserRole(roleName);
                    return roleRepository.save(newRole);
                });
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> findAllByRole(String roleName) {
        return userRepository.findAllByRoles_Name(roleName);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public long count() {
        return userRepository.count();
    }

    public long countByRole(String roleName) {
        return userRepository.findAllByRoles_Name(roleName).size();
    }
}