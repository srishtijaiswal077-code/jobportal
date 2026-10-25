package com.jobportal.analyzer;

import com.jobportal.entity.*;
import com.jobportal.repository.ResumeAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyzerService {
    private final SkillExtractor skillExtractor;
    private final MatchScoreCalculator calculator;
    private final SuggestionEngine suggestionEngine;
    private final ResumeAnalysisRepository analysisRepository;

    public ResumeAnalysis analyze(Resume resume, Job job) {
        // skills the candidate has
        Set<String> resumeSkills = new HashSet<>();
        if (resume.getSkills() != null && !resume.getSkills().isBlank()) {
            for (String s : resume.getSkills().split(",")) resumeSkills.add(s.trim().toLowerCase());
        }

        // skills the job needs (converted to the same names our dictionary uses)
        List<String> required = new ArrayList<>();
        for (String token : job.getRequiredSkills().split(",")) {
            String t = token.trim();
            if (t.isEmpty()) continue;
            List<String> canonical = skillExtractor.extractSkills(t);
            String name = canonical.isEmpty() ? t : canonical.get(0);
            if (!required.contains(name)) required.add(name);
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String r : required) {
            if (resumeSkills.contains(r.toLowerCase())) matched.add(r);
            else missing.add(r);
        }

        // 1) skills: 60 marks
        int skillScore = required.isEmpty() ? 60
                : (int) Math.round(60.0 * matched.size() / required.size());

        // 2) experience: 20 marks
        int minExp = job.getMinExperience();
        int haveMonths = resume.getExperienceMonths() != null
                ? resume.getExperienceMonths() : resume.getExperienceYears() * 12;
        int haveExp = haveMonths / 12;
        int expScore = minExp <= 0 ? 20
                : (int) Math.round(20.0 * Math.min(haveMonths, minExp * 12) / (minExp * 12.0));

        // 3) text similarity: 20 marks
        String jobText = job.getTitle() + " " + job.getDescription() + " " + job.getRequiredSkills();
        int simScore = (int) Math.round(20.0 * calculator.similarity(resume.getRawText(), jobText));

        int total = skillScore + expScore + simScore;

        ResumeAnalysis a = new ResumeAnalysis();
        a.setResume(resume);
        a.setJob(job);
        a.setScore(total);
        a.setSkillScore(skillScore);
        a.setExperienceScore(expScore);
        a.setSimilarityScore(simScore);
        a.setMatchedSkills(String.join(",", matched));
        a.setMissingSkills(String.join(",", missing));
        a.setSuggestions(String.join("|",
                suggestionEngine.suggest(missing, haveExp, minExp, resume.getRawText(), total)));
        return analysisRepository.save(a);
    }
}
