package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_analyses")
@Getter @Setter @NoArgsConstructor
public class ResumeAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    private int score;
    private int skillScore;
    private int experienceScore;
    private int similarityScore;

    @Column(columnDefinition = "TEXT")
    private String matchedSkills;     // comma separated

    @Column(columnDefinition = "TEXT")
    private String missingSkills;     // comma separated

    @Column(columnDefinition = "TEXT")
    private String suggestions;       // separated by |

    private LocalDateTime createdAt = LocalDateTime.now();
}
