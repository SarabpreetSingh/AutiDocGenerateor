package com.boa.hackathon.autodocgen.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodMeta {

    private String name;
    private List<String> params;
    private String returnType;
    private List<String> repositoryCalls;
    private List<String> domainKeywords;
    private String comment;
    private String aiDescription;
    private String body;            // raw method body (optional)
    private String endpoint;        // if you extracted @GetMapping value
    private String httpMethod;
}
