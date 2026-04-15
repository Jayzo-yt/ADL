package com.acd.verify.repository;

import com.acd.verify.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByUsername(String username);
    Optional<UserRole> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}