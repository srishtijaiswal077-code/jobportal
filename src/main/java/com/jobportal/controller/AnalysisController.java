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
import java.util.Optional;

@Controller
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class AnalysisController {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final AnalyzerService analyzerService;

    private User current(Principal p) {
        return userRepository.findByEmail(p.getName()).orElseThrow();
    }

    @GetMapping("/jobs/{id}/analysis")
    public String analyze(@PathVariable Long id, Principal p, Model model, RedirectAttributes ra) {
        Optional<Resume> resume = resumeRepository.findFirstByUserOrderByUploadedAtDesc(current(p));
        if (resume.isEmpty()) {
            ra.addFlashAttribute("error", "Upload your resume first to see the match score.");
            return "redirect:/candidate/resume";
        }
        Job job = jobRepository.findById(id).orElseThrow();
        model.addAttribute("analysis", analyzerService.analyze(resume.get(), job));
        return "candidate/analysis";
    }

    @GetMapping("/analyses")
    public String history(Principal p, Model model) {
        model.addAttribute("analyses",
                analysisRepository.findByResume_UserOrderByCreatedAtDesc(current(p)));
        return "candidate/history";
    }
}
