package com.boa.hackathon.autodocgen.controller;

import com.boa.hackathon.autodocgen.model.ProjectMetadata;
import com.boa.hackathon.autodocgen.service.AIService;
import com.boa.hackathon.autodocgen.service.DocGeneratorService;
import com.boa.hackathon.autodocgen.service.RepoParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/doc")
@Slf4j
public class DocController {


    @Autowired
    private RepoParserService repoService;

    @Autowired
    private AIService aiService;

    @Autowired
    private DocGeneratorService docGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> generate(@RequestBody Req req) throws Exception {
        // 1. clone+parse
        ProjectMetadata projectMeta = repoService.cloneAndParse(req.getUrl());

        // 2. enrich with AI summaries
        aiService.enrichProject(projectMeta);

        // 3. generate docs & UML -> returns path to zip
        File zip = docGeneratorService.generateDocsZip(projectMeta);

        InputStreamResource resource = new InputStreamResource(Files.newInputStream(zip.toPath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(zip.getName()).build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("test")
    public ResponseEntity<String> test() throws Exception {
        log.info("test");
        return ResponseEntity.ok("Server is running");
    }

    public static class Req {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String u) {
            this.url = u;
        }
    }
}
