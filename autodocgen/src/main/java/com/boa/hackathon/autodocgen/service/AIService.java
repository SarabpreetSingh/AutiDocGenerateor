package com.boa.hackathon.autodocgen.service;

import com.boa.hackathon.autodocgen.model.ClassMetadata;
import com.boa.hackathon.autodocgen.model.MethodMeta;
import com.boa.hackathon.autodocgen.model.ProjectMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate rest = new RestTemplate();

    @Value("${spring.ai.openai.api-key}")
    private  String OPENROUTER_KEY;

    @Value("${spring.ai.openai.base-url}")
    private  String OLLAMA_URL;

    @Value("${spring.ai.openai.chat.model}")
    private  String MODEL;



    public void enrichProject(ProjectMetadata pm) {
        List<ClassMetadata> classes = pm.getClasses();
        if (classes == null || classes.isEmpty()) return;

        int batchSize = 10; // one call per 10 classes
        for (int i = 0; i < classes.size(); i += batchSize) {
            int end = Math.min(i + batchSize, classes.size());
            List<ClassMetadata> batch = classes.subList(i, end);
            enrichBatch(batch);
        }
    }

    private void enrichBatch(List<ClassMetadata> batch) {
        try {

            StringBuilder prompt = new StringBuilder("You are a senior backend engineer. For each of the following Java classes, explain its business purpose clearly in the format:\n\n");
            prompt.append("CLASS: <class name>\nDESCRIPTION: <explanation>\nKEY_POINTS:\n - point1\n - point2\n\n");

            for (ClassMetadata cm : batch) {
                prompt.append("\nClassName: ").append(cm.getClassName())
                        .append("\nType: ").append(cm.getType())
                        .append("\nPackage: ").append(cm.getPackageName())
                        .append("\nFields: ").append(Optional.ofNullable(cm.getFields()).orElse(Collections.emptyList()))
                        .append("\nMethods: ");

                if (cm.getMethods() != null) {
                    prompt.append(cm.getMethods().stream().map(MethodMeta::getName).collect(Collectors.toList()));
                } else prompt.append("[]");

                Set<String> domain = new HashSet<>();
                if (cm.getMethods() != null) {
                    cm.getMethods().forEach(m -> {
                        if (m.getDomainKeywords() != null) domain.addAll(m.getDomainKeywords());
                    });
                }
                prompt.append("\nDomain hints: ").append(domain).append("\n");
            }

            String response = callOpenRouter(prompt.toString());
            distributeResponse(batch, response);

        } catch (Exception e) {
            log.error("Batch enrichment failed: {}", e.getMessage());
        }
    }

    private void distributeResponse(List<ClassMetadata> batch, String aiResponse) {

        if (aiResponse == null || aiResponse.isBlank()) return;
        String[] parts = aiResponse.split("CLASS:");
        for (int i = 0; i < batch.size() && i < parts.length - 1; i++) {
            batch.get(i).setAiDescription("CLASS:" + parts[i + 1].trim());
        }
    }

    private String callOpenRouter(String prompt) {
        try {
            if (OPENROUTER_KEY == null || OPENROUTER_KEY.isBlank()) {
                return "Error: OPENROUTER_API_KEY not set in environment variables.";
            }

            log.info("Calling OpenRouter API for batch...");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", MODEL);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            body.put("max_tokens", 8000); // reduce to stay under token limits

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + OPENROUTER_KEY);
            headers.set("HTTP-Referer", "https://autodocgen");
            headers.set("X-Title", "AutoDocGenerator");

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = rest.exchange(OLLAMA_URL, HttpMethod.POST, req, Map.class);

            if (resp.getBody() == null) return "No response received";

            List<?> choices = (List<?>) resp.getBody().get("choices");
            if (choices == null || choices.isEmpty()) return "No choices returned";

            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) first.get("message");
            String content = (String) message.get("content");

            log.info("Batch AI Response (trimmed): {}", content != null ? content.substring(0, Math.min(400, content.length())) : "null");
            return content;
        } catch (Exception e) {
            log.error("Error calling OpenRouter: {}", e.getMessage());
            return "Error calling OpenRouter: " + e.getMessage();
        }
    }
}
