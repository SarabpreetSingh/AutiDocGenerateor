package com.boa.hackathon.autodocgen.model;
import lombok.Data;
import java.util.List;

@Data
public class ProjectMetadata {
        private String projectName;
        private int classCount;
        private List<ClassMetadata> classes;

}
