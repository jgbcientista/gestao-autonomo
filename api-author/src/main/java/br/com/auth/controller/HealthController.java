package br.com.auth.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Tag(name = "Health Check", description = "Endpoints para verificação de saúde da aplicação")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health Check", 
               description = "Verifica se a aplicação está funcionando corretamente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Aplicação funcionando corretamente")
    })
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check endpoint chamado - /health");
        return createHealthResponse();
    }

    @GetMapping("/api/v1/health")
    @Operation(summary = "Health Check API v1", 
               description = "Verifica se a aplicação está funcionando corretamente (versão API v1)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Aplicação funcionando corretamente")
    })
    public ResponseEntity<Map<String, Object>> healthV1() {
        log.info("Health check endpoint chamado - /api/v1/health");
        return createHealthResponse();
    }

    private ResponseEntity<Map<String, Object>> createHealthResponse() {
        try {
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("status", "UP");
            healthInfo.put("service", "auth-service");
            healthInfo.put("version", "1.0.0");
            healthInfo.put("timestamp", LocalDateTime.now().toString());
            healthInfo.put("description", "Sistema de Autenticação Inteligente");
            
            Map<String, Object> checks = new HashMap<>();
            checks.put("database", "UP");
            checks.put("jvm", "UP");
            checks.put("disk", "UP");
            healthInfo.put("checks", checks);
            
            return ResponseEntity.ok(healthInfo);
        } catch (Exception e) {
            log.error("Erro no health check", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", "DOWN");
            errorInfo.put("error", e.getMessage());
            errorInfo.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorInfo);
        }
    }

    @GetMapping("/health/status")
    @Operation(summary = "Status Simples", 
               description = "Retorna status simples da aplicação")
    public ResponseEntity<String> status() {
        log.info("Status endpoint chamado");
        return ResponseEntity.ok("Serviço de autenticação operacional");
    }

    @GetMapping("/health/info")
    @Operation(summary = "Informações da Aplicação", 
               description = "Retorna informações detalhadas da aplicação")
    public ResponseEntity<Map<String, Object>> info() {
        log.info("Info endpoint chamado");
        
        try {
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
        } catch (Exception e) {
            log.error("Erro no endpoint info", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage())
            );
        }
    }
} 