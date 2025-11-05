package com.boa.hackathon.autodocgen.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMetadata {
        private String projectName;
        private int classCount;
        private List<ClassMetadata> classes;

}
