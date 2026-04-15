package com.acd.verify.repository;

import com.acd.verify.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertId(String certId);
    Optional<Certificate> findByRollNo(String rollNo);
}