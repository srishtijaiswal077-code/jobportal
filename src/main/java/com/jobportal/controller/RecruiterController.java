package com.jobportal.controller;

import com.jobportal.entity.*;
import com.jobportal.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterController {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

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
    public String applications(@PathVariable Long id, Principal p, Model model) {
        Job job = jobRepository.findById(id).orElseThrow();
        if (!job.getRecruiter().getId().equals(current(p).getId()))
            return "redirect:/recruiter/home";
        model.addAttribute("job", job);
        model.addAttribute("applications", applicationRepository.findByJob(job));
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
