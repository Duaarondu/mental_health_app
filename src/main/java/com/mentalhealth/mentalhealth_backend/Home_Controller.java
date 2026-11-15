package com.mentalhealth.mentalhealth_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home_Controller {

    @GetMapping("/api/home")
    public String home() {
        return "Welcome to our Mental Health App!";
    }
}