package com.jobportal.repository;

import com.jobportal.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    List<ResumeAnalysis> findByResume_UserOrderByCreatedAtDesc(User user);
}
