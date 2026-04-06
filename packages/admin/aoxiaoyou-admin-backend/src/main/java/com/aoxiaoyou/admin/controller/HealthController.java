package com.aoxiaoyou.admin.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
            "service", "aoxiaoyou-admin-backend",
            "version", "0.1.0",
            "status", "UP"
        ));
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
