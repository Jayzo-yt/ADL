package com.acd.verify.repository;

import com.acd.verify.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByCode(String code);
    List<Institution> findByEnabled(boolean enabled);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
