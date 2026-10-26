package com.jobportal.controller;

import com.jobportal.analyzer.AnalyzerService;
import com.jobportal.entity.*;
import com.jobportal.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterController {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;
    private final AnalyzerService analyzerService;

    private User current(Principal p) {
        return userRepository.findByEmail(p.getName()).orElseThrow();
    }

    @GetMapping("/home")
    public String home(Principal p, Model model) {
        model.addAttribute("jobs", jobRepository.findByRecruiter(current(p)));
        return "recruiter/home";
    }

    @GetMapping("/jobs/new")
    public String newJob(Model model) {
        model.addAttribute("job", new Job());
        return "recruiter/job-form";
    }

    @PostMapping("/jobs")
    public String saveJob(@Valid @ModelAttribute("job") Job job,
                          BindingResult result, Principal p) {
        if (result.hasErrors()) return "recruiter/job-form";
        job.setRecruiter(current(p));
        jobRepository.save(job);
        return "redirect:/recruiter/home";
    }

    @GetMapping("/jobs/{id}/applications")
    public String applications(@PathVariable Long id,
                               @RequestParam(required = false) String skill,
                               Principal p, Model model) {
        Job job = jobRepository.findById(id).orElseThrow();
        if (!job.getRecruiter().getId().equals(current(p).getId()))
            return "redirect:/recruiter/home";

        List<Application> all = applicationRepository.findByJob(job);
        Map<Long, String> skillsByApp = new HashMap<>();

        for (Application a : all) {
            Optional<Resume> r = resumeRepository.findFirstByUserOrderByUploadedAtDesc(a.getCandidate());
            skillsByApp.put(a.getId(), r.map(Resume::getSkills).orElse(""));
            // older applications have no score yet, so calculate it now
            if (a.getMatchScore() == null && r.isPresent()) {
                a.setMatchScore((double) analyzerService.quickScore(r.get(), job));
                applicationRepository.save(a);
            }
        }

        List<Application> list = new ArrayList<>(all);
        if (skill != null && !skill.isBlank()) {
            String wanted = skill.trim().toLowerCase();
            list.removeIf(a -> {
                String s = skillsByApp.getOrDefault(a.getId(), "").toLowerCase();
                return Arrays.stream(s.split(",")).noneMatch(x -> x.trim().contains(wanted));
            });
        }
        list.sort((a, b) -> Double.compare(
                b.getMatchScore() == null ? -1 : b.getMatchScore(),
                a.getMatchScore() == null ? -1 : a.getMatchScore()));

        model.addAttribute("job", job);
        model.addAttribute("applications", list);
        model.addAttribute("skillsByApp", skillsByApp);
        model.addAttribute("skill", skill == null ? "" : skill);
        model.addAttribute("statuses", ApplicationStatus.values());
        return "recruiter/applications";
    }

    @PostMapping("/applications/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status, Principal p) {
        Application app = applicationRepository.findById(id).orElseThrow();
        Job job = app.getJob();
        if (!job.getRecruiter().getId().equals(current(p).getId()))
            return "redirect:/recruiter/home";
        app.setStatus(ApplicationStatus.valueOf(status));
        applicationRepository.save(app);
        return "redirect:/recruiter/jobs/" + job.getId() + "/applications";
    }
}
