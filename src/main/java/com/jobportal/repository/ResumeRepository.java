package com.jobportal.repository;

import com.jobportal.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findFirstByUserOrderByUploadedAtDesc(User user);
}
