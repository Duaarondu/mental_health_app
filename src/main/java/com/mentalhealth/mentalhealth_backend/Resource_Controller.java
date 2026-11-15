
package com.mentalhealth.mentalhealth_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Resource_Controller {

@GetMapping("/api/resources")
public String[] getResources() {
    return new String[] {
        "Counseling",
        "Stress Management",
        "Support Groups"
    };
}
}
