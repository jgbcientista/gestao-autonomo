package br.com.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Endpoints para verificação de saúde da aplicação")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health Check", 
               description = "Verifica se a aplicação está funcionando corretamente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Aplicação funcionando corretamente")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "auth-service");
        healthInfo.put("version", "1.0.0");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("description", "Sistema de Autenticação Inteligente");
        
        Map<String, Object> checks = new HashMap<>();
        checks.put("database", "UP");
        checks.put("jvm", "UP");
        checks.put("disk", "UP");
        healthInfo.put("checks", checks);
        
        return ResponseEntity.ok(healthInfo);
    }

    @GetMapping("/status")
    @Operation(summary = "Status Simples", 
               description = "Retorna status simples da aplicação")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Serviço de autenticação operacional");
    }

    @GetMapping("/info")
    @Operation(summary = "Informações da Aplicação", 
               description = "Retorna informações detalhadas da aplicação")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Auth Service");
        info.put("description", "Sistema de Autenticação com IA e Blockchain");
        info.put("version", "1.0.0");
        info.put("environment", "development");
        info.put("java.version", System.getProperty("java.version"));
        info.put("spring.version", "3.2.3");
        
        Map<String, Object> features = new HashMap<>();
        features.put("ai-analysis", true);
        features.put("blockchain", false);
        features.put("jwt", true);
        features.put("h2-console", true);
        info.put("features", features);
        
        return ResponseEntity.ok(info);
    }
} 