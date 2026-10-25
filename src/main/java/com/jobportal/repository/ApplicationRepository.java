package com.jobportal.repository;

import com.jobportal.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateOrderByAppliedAtDesc(User candidate);
    List<Application> findByJob(Job job);
    boolean existsByJobAndCandidate(Job job, User candidate);
}