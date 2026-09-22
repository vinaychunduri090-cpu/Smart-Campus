package com.campusportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    // Handle other pages for SPA-like behavior if they don't have .html in URL
    @GetMapping({"/dashboard", "/register", "/login"})
    public String pages() {
        return "forward:/index.html";
    }
}
