package com.jobportal.controller;

import com.jobportal.analyzer.*;
import com.jobportal.entity.*;
import com.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/candidate/resume")
@RequiredArgsConstructor
public class ResumeController {
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final SkillExtractor skillExtractor;

    private User current(Principal p) {
        return userRepository.findByEmail(p.getName()).orElseThrow();
    }

    @GetMapping
    public String page(Principal p, Model model) {
        model.addAttribute("resume",
                resumeRepository.findFirstByUserOrderByUploadedAtDesc(current(p)).orElse(null));
        return "candidate/resume";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         Principal p, RedirectAttributes ra) {
        String name = file.getOriginalFilename();
        if (file.isEmpty() || name == null || !name.toLowerCase().endsWith(".pdf")) {
            ra.addFlashAttribute("error", "Please choose a PDF file.");
            return "redirect:/candidate/resume";
        }
        try {
            String text = pdfTextExtractor.extract(file);
            if (text.isBlank()) {
                ra.addFlashAttribute("error",
                        "No text found. This looks like a scanned PDF, please upload a text-based PDF.");
                return "redirect:/candidate/resume";
            }
            List<String> skills = skillExtractor.extractSkills(text);

            Resume r = new Resume();
            r.setUser(current(p));
            r.setFileName(name);
            r.setRawText(text);
            r.setSkills(String.join(",", skills));
            int months = skillExtractor.extractExperienceMonths(text);
            r.setExperienceMonths(months);
            r.setExperienceYears(months / 12);
            resumeRepository.save(r);

            ra.addFlashAttribute("success", "Resume uploaded. " + skills.size() + " skills found.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Could not read this PDF. Try another file.");
        }
        return "redirect:/candidate/resume";
    }
}
