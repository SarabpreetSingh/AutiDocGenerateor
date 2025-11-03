package com.boa.hackathon.autodocgen.service;

import com.boa.hackathon.autodocgen.model.ClassMetadata;
import com.boa.hackathon.autodocgen.model.MethodMeta;
import com.boa.hackathon.autodocgen.model.ProjectMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate rest = new RestTemplate();
    private final String OPENAI_KEY = System.getenv("OPENAI_API_KEY");
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final String MODEL = "gpt-4o-mini";

    public void enrichProject(ProjectMetadata pm) {
        pm.getClasses().forEach(this::enrichClass);
    }

    private void enrichClass(ClassMetadata cm) {
        try {
            String prompt = buildClassPrompt(cm);
            String ai = callOpenAI(prompt);
            cm.setAiDescription(ai);

            if (cm.getMethods()!=null) {
                for(MethodMeta mm: cm.getMethods()) {
                    String mp = buildMethodPrompt(cm, mm);
                    String ma = callOpenAI(mp);
                    mm.setAiDescription(ma);
                }
            }
        } catch(Exception e) {
            log.warn("AI enrich failed for {}: {}", cm.getClassName(), e.getMessage());
        }
    }

    private String buildClassPrompt(ClassMetadata cm) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a senior software engineer. Given the following class metadata, write in detail in plain-English description of the class's business responsibility and list key points about what it does.\n");
        sb.append("ClassName: ").append(cm.getClassName()).append("\n");
        sb.append("Type: ").append(cm.getType()).append("\n");
        sb.append("Package: ").append(cm.getPackageName()).append("\n");
        sb.append("Fields: ").append(Optional.ofNullable(cm.getFields()).orElse(Collections.emptyList())).append("\n");
        sb.append("Methods: ");
        if (cm.getMethods()!=null) {
            sb.append(cm.getMethods().stream().map(MethodMeta::getName).collect(Collectors.toList()));
        } else sb.append("[]");
        sb.append("\n");
        sb.append("Domain hints (from code): ");
        Set<String> domain = new HashSet<>();
        if (cm.getMethods()!=null) {
            cm.getMethods().forEach(m->{
                if (m.getDomainKeywords()!=null) domain.addAll(m.getDomainKeywords());
            });
        }
        sb.append(domain).append("\n");
        sb.append("Write the answer as: DESCRIPTION: <long description>\\nKEY_POINTS:\\n - point1\\n - point2");
        return sb.toString();
    }

    private String buildMethodPrompt(ClassMetadata cm, MethodMeta mm) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a software engineer. Explain this method in 1 sentence in business terms and list side-effects (DB writes, external calls) if any.\n");
        sb.append("Class: ").append(cm.getClassName()).append("\n");
        sb.append("Method: ").append(mm.getName()).append("\n");
        sb.append("Parameters: ").append(Optional.ofNullable(mm.getParams()).orElse(Collections.emptyList())).append("\n");
        sb.append("Repository calls: ").append(Optional.ofNullable(mm.getRepositoryCalls()).orElse(Collections.emptyList())).append("\n");
        sb.append("Domain keywords: ").append(Optional.ofNullable(mm.getDomainKeywords()).orElse(Collections.emptyList())).append("\n");
        sb.append("Answer format: SENTENCE: <one-liner> \\n SIDE_EFFECTS: <list>");
        return sb.toString();
    }

    private String callOpenAI(String prompt) {
        if (OPENAI_KEY==null || OPENAI_KEY.isBlank()) {
            return "AI_KEY_MISSING: " + (prompt.length()>200? prompt.substring(0,200):prompt);
        }
        Map<String,Object> message = Map.of("role","user","content",prompt);
        Map<String,Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", List.of(message));
        body.put("max_tokens", 200);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_KEY);

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, headers);
        Map<?,?> resp = rest.postForObject(OPENAI_URL, req, Map.class);
        if (resp==null) return "No response";
        List<?> choices = (List<?>) resp.get("choices");
        if (choices==null || choices.isEmpty()) return "No choices";
        Map<?,?> first = (Map<?,?>) choices.get(0);
        Object messageObj = first.get("message");
        if (messageObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) messageObj;
            Object value = map.containsKey("content") ? map.get("content") : "";
            return value != null ? value.toString().trim() : "";
        } else return messageObj.toString();
    }
}
