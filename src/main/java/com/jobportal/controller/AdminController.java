package com.jobportal.controller;

import com.jobportal.entity.*;
import com.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;

    @GetMapping("/home")
    public String home(Model model) {
        long candidates = userRepository.countByRole(Role.CANDIDATE);
        long recruiters = userRepository.countByRole(Role.RECRUITER);

        model.addAttribute("candidates", candidates);
        model.addAttribute("recruiters", recruiters);
        model.addAttribute("totalJobs", jobRepository.count());
        model.addAttribute("openJobs", jobRepository.countByOpen(true));
        model.addAttribute("totalApplications", applicationRepository.count());
        model.addAttribute("totalResumes", resumeRepository.count());

        double avg = applicationRepository.findAll().stream()
                .filter(a -> a.getMatchScore() != null)
                .mapToDouble(Application::getMatchScore)
                .average().orElse(0);
        model.addAttribute("avgScore", Math.round(avg));

        List<String> statusLabels = new ArrayList<>();
        List<Long> statusCounts = new ArrayList<>();
        for (ApplicationStatus s : ApplicationStatus.values()) {
            statusLabels.add(s.name());
            statusCounts.add(applicationRepository.countByStatus(s));
        }
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("roleLabels", List.of("Candidates", "Recruiters"));
        model.addAttribute("roleCounts", List.of(candidates, recruiters));
        return "admin/home";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow();
        if (u.getRole() != Role.ADMIN) {          // admin accounts are never deactivated
            u.setActive(!u.isActive());
            userRepository.save(u);
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/jobs")
    public String jobs(Model model) {
        List<Job> jobs = jobRepository.findAll();
        Map<Long, Long> counts = new HashMap<>();
        for (Job j : jobs) counts.put(j.getId(), applicationRepository.countByJob(j));
        model.addAttribute("jobs", jobs);
        model.addAttribute("counts", counts);
        return "admin/jobs";
    }

    @PostMapping("/jobs/{id}/toggle")
    public String toggleJob(@PathVariable Long id) {
        Job j = jobRepository.findById(id).orElseThrow();
        j.setOpen(!j.isOpen());
        jobRepository.save(j);
        return "redirect:/admin/jobs";
    }
}
