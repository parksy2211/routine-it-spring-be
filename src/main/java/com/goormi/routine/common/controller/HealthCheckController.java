package com.goormi.routine.common.controller;

import com.goormi.routine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {
    
    @Value("${spring.application.name:routine}")
    private String applicationName;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Operation(summary = "헬스 체크", description = "서버의 상태를 확인합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", applicationName);
        health.put("version", "0.0.1-SNAPSHOT");
        health.put("environment", activeProfile);
        
        return ResponseEntity.ok(ApiResponse.success("Server is running", health));
    }
    
    @Operation(summary = "간단한 헬스 체크", description = "서버가 살아있는지 확인합니다.")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}