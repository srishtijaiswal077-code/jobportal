package com.jobportal.controller;

import com.jobportal.entity.*;
import com.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class CandidateController {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    private User current(Principal p) {
        return userRepository.findByEmail(p.getName()).orElseThrow();
    }

    @GetMapping("/home")
    public String home(Principal p, Model model) {
        User me = current(p);
        Set<Long> appliedIds = applicationRepository.findByCandidateOrderByAppliedAtDesc(me)
                .stream().map(a -> a.getJob().getId()).collect(Collectors.toSet());
        model.addAttribute("jobs", jobRepository.findByOpenTrueOrderByCreatedAtDesc());
        model.addAttribute("appliedIds", appliedIds);
        return "candidate/home";
    }

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id, Principal p) {
        User me = current(p);
        Job job = jobRepository.findById(id).orElseThrow();
        if (job.isOpen() && !applicationRepository.existsByJobAndCandidate(job, me)) {
            Application app = new Application();
            app.setJob(job);
            app.setCandidate(me);
            applicationRepository.save(app);
        }
        return "redirect:/candidate/applications";
    }

    @GetMapping("/applications")
    public String myApplications(Principal p, Model model) {
        model.addAttribute("applications",
                applicationRepository.findByCandidateOrderByAppliedAtDesc(current(p)));
        return "candidate/applications";
    }
}
