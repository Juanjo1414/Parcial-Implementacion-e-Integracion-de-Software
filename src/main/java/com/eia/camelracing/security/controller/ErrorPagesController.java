package com.eia.camelracing.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPagesController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}