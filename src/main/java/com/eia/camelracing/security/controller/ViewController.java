package com.eia.camelracing.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// OJO: @Controller, NO @RestController. @RestController devuelve JSON;
// @Controller devuelve el NOMBRE de una plantilla Thymeleaf para renderizar.
@Controller
public class ViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // busca templates/login.html
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}