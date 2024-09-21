package com.edufelizardo.maissaudepublica.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthHierarchyController {
    @RequestMapping("/health-hierarchy")
    public String createByHealthHierarchy(@RequestParam(required = false, defaultValue = "Usuário") String name) {
        return "Eu estou aqui, " + name;
    }
}


