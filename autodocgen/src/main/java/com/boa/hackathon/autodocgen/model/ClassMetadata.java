package com.boa.hackathon.autodocgen.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassMetadata {
    private String className;
    private String packageName;
    private String type; // Controller/Service/Repository/Entity/Model
    private List<MethodMeta> methods;
    private List<String> fields;
    private String comment;
    private String aiDescription; // filled by AI
    private Map<String,Object> extra; // hold repository calls, domain keywords
	
}
