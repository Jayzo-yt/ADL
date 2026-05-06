package com.acd.verify.repository;

import com.acd.verify.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findAllByOrderByTimestampDesc();
    List<ActivityLog> findTop50ByOrderByTimestampDesc();
    List<ActivityLog> findByAction(String action);
    List<ActivityLog> findByUsername(String username);
}
