package com.example.sdwan.api;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SdwanController {

    @GetMapping("/health")
    public Map<String, String> getHealth() {
        return Map.of(
                "status", "ok",
                "service", "sdwan-assignment-starter-api",
                "timestamp", Instant.now().toString(),
                "assignment", "Replace this starter endpoint with your SD-WAN dashboard API."
        );
    }
}
