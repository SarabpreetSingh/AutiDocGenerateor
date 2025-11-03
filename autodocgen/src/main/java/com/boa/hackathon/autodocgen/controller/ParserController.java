//package com.boa.hackathon.autodocgen.controller;
//
//import com.boa.hackathon.autodocgen.model.ProjectMetadata;
//import com.boa.hackathon.autodocgen.service.RepoParserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/parser")
//public class ParserController {
//
//    @Autowired
//    private RepoParserService repoParserService;
//
//    @PostMapping("/analyze")
//    public ProjectMetadata analyzeRepo(@RequestBody String githubUrl) {
//        return repoParserService.parseRepository(githubUrl);
//    }
//
//}
