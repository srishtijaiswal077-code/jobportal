package com.jobportal.repository;

import com.jobportal.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByRecruiter(User recruiter);
    List<Job> findByOpenTrueOrderByCreatedAtDesc();
    long countByOpen(boolean open);
}