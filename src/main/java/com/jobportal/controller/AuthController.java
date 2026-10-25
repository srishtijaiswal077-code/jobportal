package com.jobportal.controller;

import com.jobportal.entity.*;
import com.jobportal.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result) {
        if (userRepository.existsByEmail(user.getEmail()))
            result.rejectValue("email", "dup", "Email already registered");
        if (user.getRole() == null || user.getRole() == Role.ADMIN)
            result.rejectValue("role", "bad", "Choose Candidate or Recruiter");
        if (result.hasErrors()) return "register";

        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN" -> "redirect:/admin/home";
            case "ROLE_RECRUITER" -> "redirect:/recruiter/home";
            default -> "redirect:/candidate/home";
        };
    }
}
