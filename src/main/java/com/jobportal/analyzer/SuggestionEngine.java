package com.jobportal.analyzer;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class SuggestionEngine {

    private static final Pattern NUMBERS = Pattern.compile("\\d+\\s*(%|\\+|users|ms|x\\b)");

    public List<String> suggest(List<String> missing, int resumeYears, int minExp,
                                String resumeText, int total) {
        List<String> s = new ArrayList<>();
        String lower = resumeText == null ? "" : resumeText.toLowerCase();

        if (!missing.isEmpty()) {
            s.add("Missing skills for this job: " + String.join(", ", missing)
                    + ". If you already know them, mention them clearly in your Skills section.");
            for (String m : missing.stream().limit(2).toList()) {
                s.add("Build a small project using " + m + " and add it to your Projects section.");
            }
        }
        if (minExp > 0 && resumeYears < minExp) {
            s.add("This job asks for " + minExp + "+ years of experience. Add internships, freelance work "
                    + "or major projects with the duration mentioned.");
        }
        if (!NUMBERS.matcher(lower).find()) {
            s.add("Add measurable results to your projects, for example 'reduced page load time by 30%'.");
        }
        if (lower.length() < 1200) {
            s.add("Your resume looks short. Add more detail about projects, tools used and your role.");
        }
        if (!lower.contains("github") && !lower.contains("linkedin")) {
            s.add("Add your GitHub or LinkedIn link so recruiters can see your work.");
        }
        if (total >= 80 && missing.isEmpty()) {
            s.add("Strong match. Tailor your summary to this job title and apply.");
        }
        return s;
    }
}