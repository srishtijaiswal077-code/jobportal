package com.jobportal.controller;

import com.jobportal.analyzer.AnalyzerService;
import com.jobportal.entity.*;
import com.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class CandidateController {
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
        User me = current(p);
        Set<Long> appliedIds = applicationRepository.findByCandidateOrderByAppliedAtDesc(me)
                .stream().map(a -> a.getJob().getId()).collect(Collectors.toSet());

        List<Job> jobs = new ArrayList<>(jobRepository.findByOpenTrueOrderByCreatedAtDesc());
        Optional<Resume> resume = resumeRepository.findFirstByUserOrderByUploadedAtDesc(me);
        Map<Long, Integer> scores = new HashMap<>();
        if (resume.isPresent()) {
            for (Job j : jobs) scores.put(j.getId(), analyzerService.quickScore(resume.get(), j));
            jobs.sort((a, b) -> scores.get(b.getId()) - scores.get(a.getId()));  // best match first
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("appliedIds", appliedIds);
        model.addAttribute("scores", scores);
        model.addAttribute("hasResume", resume.isPresent());
        return "candidate/home";
    }

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id, Principal p, RedirectAttributes ra) {
        User me = current(p);
        Optional<Resume> resume = resumeRepository.findFirstByUserOrderByUploadedAtDesc(me);
        if (resume.isEmpty()) {
            ra.addFlashAttribute("error", "Upload your resume before applying to a job.");
            return "redirect:/candidate/resume";
        }
        Job job = jobRepository.findById(id).orElseThrow();
        if (job.isOpen() && !applicationRepository.existsByJobAndCandidate(job, me)) {
            Application app = new Application();
            app.setJob(job);
            app.setCandidate(me);
            app.setMatchScore((double) analyzerService.quickScore(resume.get(), job));
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
