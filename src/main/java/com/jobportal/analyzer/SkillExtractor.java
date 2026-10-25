package com.jobportal.analyzer;

import org.springframework.stereotype.Component;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.*;

@Component
public class SkillExtractor {

    private static final Map<String, List<String>> SKILLS = new LinkedHashMap<>();
    static {
        add("Java", "java");
        add("Spring Boot", "spring boot", "springboot");
        add("Spring", "spring framework", "spring mvc", "spring security");
        add("Hibernate", "hibernate", "jpa");
        add("SQL", "sql");
        add("MySQL", "mysql");
        add("PostgreSQL", "postgresql", "postgres");
        add("MongoDB", "mongodb", "mongo db");
        add("REST API", "rest api", "rest apis", "restful");
        add("Microservices", "microservices", "microservice");
        add("Maven", "maven");
        add("Git", "git", "github");
        add("Docker", "docker");
        add("Kubernetes", "kubernetes", "k8s");
        add("HTML", "html", "html5");
        add("CSS", "css", "css3");
        add("Bootstrap", "bootstrap");
        add("Tailwind CSS", "tailwind", "tailwind css");
        add("Responsive Design", "responsive design", "responsive web design");
        add("JavaScript", "javascript", "js", "es6");
        add("TypeScript", "typescript");
        add("React", "react", "reactjs", "react.js");
        add("Angular", "angular");
        add("Vue.js", "vue", "vue.js", "vuejs");
        add("Node.js", "node.js", "nodejs", "node js");
        add("Express", "expressjs", "express.js");
        add("Python", "python");
        add("Django", "django");
        add("Flask", "flask");
        add("C++", "c++");
        add("C#", "c#");
        add("Machine Learning", "machine learning", "ml");
        add("Deep Learning", "deep learning");
        add("NLP", "nlp", "natural language processing", "nltk", "spacy");
        add("Data Structures", "data structures", "dsa");
        add("AWS", "aws", "amazon web services");
        add("JUnit", "junit");
        add("Linux", "linux");
        add("Thymeleaf", "thymeleaf");
        add("Figma", "figma");
        add("Excel", "excel", "ms excel");
        add("Postman", "postman");
        add("Redis", "redis");
    }

    private static final Map<String, Integer> MONTHS = new HashMap<>();
    static {
        String[] m = {"jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};
        for (int i = 0; i < m.length; i++) MONTHS.put(m[i], i + 1);
    }

    private static void add(String name, String... aliases) {
        SKILLS.put(name, Arrays.asList(aliases));
    }

    public List<String> extractSkills(String text) {
        String lower = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : SKILLS.entrySet()) {
            for (String alias : e.getValue()) {
                Pattern p = Pattern.compile("(?<![a-z0-9+#.])" + Pattern.quote(alias) + "(?![a-z0-9+#])");
                if (p.matcher(lower).find()) {
                    found.add(e.getKey());
                    break;
                }
            }
        }
        // MySQL and PostgreSQL are SQL databases, so a job asking for SQL should count them
        if ((found.contains("MySQL") || found.contains("PostgreSQL")) && !found.contains("SQL")) {
            found.add("SQL");
        }
        return found;
    }

    /** Experience in months: "June 2026 - Present" style ranges, or "3 years" style phrases. */
    public int extractExperienceMonths(String text) {
        // 1) explicit "N years / yrs"
        int explicitYears = 0;
        Matcher y = Pattern.compile("(\\d{1,2})\\+?\\s*(?:years?|yrs?)", Pattern.CASE_INSENSITIVE).matcher(text);
        while (y.find()) explicitYears = Math.max(explicitYears, Integer.parseInt(y.group(1)));

        // 2) "Month YYYY - Present" or "Month YYYY - Month YYYY"
        String mon = "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?";
        Pattern range = Pattern.compile(
                mon + "\\s+(\\d{4})\\s*(?:-|\u2013|\u2014|to)\\s*(?:(present|current|now)|" + mon + "\\s+(\\d{4}))",
                Pattern.CASE_INSENSITIVE);
        Matcher r = range.matcher(text);
        int rangeMonths = 0;
        YearMonth now = YearMonth.now();
        while (r.find()) {
            YearMonth start = YearMonth.of(Integer.parseInt(r.group(2)), MONTHS.get(r.group(1).toLowerCase()));
            YearMonth end = r.group(3) != null ? now
                    : YearMonth.of(Integer.parseInt(r.group(5)), MONTHS.get(r.group(4).toLowerCase()));
            long diff = ChronoUnit.MONTHS.between(start, end);
            if (diff > 0) rangeMonths += (int) diff;
        }
        return Math.min(Math.max(explicitYears * 12, rangeMonths), 40 * 12);
    }
}
