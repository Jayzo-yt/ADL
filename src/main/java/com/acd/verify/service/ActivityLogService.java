package com.acd.verify.service;

import com.acd.verify.model.ActivityLog;
import com.acd.verify.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void log(String action, String username, String details) {
        ActivityLog entry = new ActivityLog(action, username, details);
        activityLogRepository.save(entry);
    }

    public List<ActivityLog> getRecentActivity() {
        return activityLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<ActivityLog> getAllActivity() {
        return activityLogRepository.findAllByOrderByTimestampDesc();
    }

    public long count() {
        return activityLogRepository.count();
    }
}
