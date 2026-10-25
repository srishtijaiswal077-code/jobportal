package com.jobportal.analyzer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class MatchScoreCalculator {

    // resume is long and job text is short, so raw cosine is always small; this scales it up fairly
    private static final double BOOST = 2.5;

    private static final Set<String> STOP = Set.of("the", "and", "for", "with", "you", "are", "our",
            "will", "this", "that", "from", "have", "has", "your", "can", "not", "but", "all", "any",
            "per", "use", "using", "work", "working", "team", "able", "etc", "job", "role", "about",
            "into", "such", "also", "build");

    public double similarity(String resumeText, String jobText) {
        return Math.min(1.0, cosine(resumeText, jobText) * BOOST);
    }

    private double cosine(String a, String b) {
        Map<String, Integer> va = termFrequency(a);
        Map<String, Integer> vb = termFrequency(b);
        if (va.isEmpty() || vb.isEmpty()) return 0;

        double dot = 0;
        for (Map.Entry<String, Integer> e : va.entrySet()) {
            Integer other = vb.get(e.getKey());
            if (other != null) dot += (double) e.getValue() * other;
        }
        double na = Math.sqrt(va.values().stream().mapToDouble(x -> x * x).sum());
        double nb = Math.sqrt(vb.values().stream().mapToDouble(x -> x * x).sum());
        return dot / (na * nb);
    }

    private Map<String, Integer> termFrequency(String text) {
        Map<String, Integer> m = new HashMap<>();
        if (text == null) return m;
        for (String w : text.toLowerCase().split("[^a-z0-9+#]+")) {
            if (w.length() < 3 || STOP.contains(w)) continue;
            m.merge(w, 1, Integer::sum);
        }
        return m;
    }
}
