package br.com.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/test")
@Slf4j
public class TestController {

    @GetMapping
    public ResponseEntity<String> test() {
        log.info("Endpoint de teste básico chamado");
        return ResponseEntity.ok("OK - Aplicação funcionando!");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        log.info("Endpoint de status chamado");
        return ResponseEntity.ok("Status: ATIVO");
    }

    @GetMapping("/simple")
    public ResponseEntity<String> simple() {
        System.out.println("🔍 SIMPLE ENDPOINT CHAMADO!");
        log.info("Endpoint simple chamado");
        return ResponseEntity.ok("SUCCESS - ENDPOINT FUNCIONANDO!");
    }

    @GetMapping("/health-simple")
    public ResponseEntity<String> healthSimple() {
        System.out.println("🔍 HEALTH SIMPLE ENDPOINT CHAMADO!");
        log.info("Endpoint health-simple chamado");
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug(HttpServletRequest request) {
        System.out.println("🔍 DEBUG ENDPOINT CHAMADO!");
        log.info("Endpoint debug chamado");
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("method", request.getMethod());
        debug.put("requestURI", request.getRequestURI());
        debug.put("contextPath", request.getContextPath());
        debug.put("servletPath", request.getServletPath());
        debug.put("pathInfo", request.getPathInfo());
        debug.put("queryString", request.getQueryString());
        debug.put("remoteAddr", request.getRemoteAddr());
        debug.put("remoteHost", request.getRemoteHost());
        debug.put("serverName", request.getServerName());
        debug.put("serverPort", request.getServerPort());
        debug.put("scheme", request.getScheme());
        debug.put("protocol", request.getProtocol());
        debug.put("timestamp", LocalDateTime.now().toString());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> 
            headers.put(name, request.getHeader(name))
        );
        debug.put("headers", headers);
        
        System.out.println("🎯 DEBUG INFO: " + debug);
        
        return ResponseEntity.ok(debug);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        log.info("Endpoint info chamado");
        
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Auth Service Test Controller");
        info.put("version", "1.0.0");
        info.put("status", "FUNCIONANDO");
        info.put("endpoints", Map.of(
            "test", "/api/v1/test",
            "status", "/api/v1/test/status",
            "simple", "/api/v1/test/simple",
            "debug", "/api/v1/test/debug",
            "info", "/api/v1/test/info"
        ));
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("message", "Todos os endpoints básicos estão funcionais");
        
        return ResponseEntity.ok(info);
    }

    @PostMapping("/mock-login")
    public ResponseEntity<Map<String, Object>> mockLogin(@RequestBody Map<String, String> request) {
        System.out.println("🔍 MOCK LOGIN ENDPOINT CHAMADO!");
        log.info("Mock login endpoint chamado com dados: {}", request);
        
        String email = request.get("email");
        String password = request.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if ("joaoguedes@gmail.com".equals(email) && "1234567890".equals(password)) {
            response.put("token", "mock-jwt-token-12345");
            response.put("name", "João Guedes");
            response.put("email", "joaoguedes@gmail.com");
            response.put("success", true);
            
            System.out.println("✅ Mock login bem-sucedido para: " + email);
            log.info("Mock login bem-sucedido para: {}", email);
        } else {
            response.put("token", null);
            response.put("name", null);
            response.put("email", null);
            response.put("success", false);
            response.put("error", "Credenciais inválidas");
            
            System.out.println("❌ Mock login falhou para: " + email);
            log.info("Mock login falhou para: {}", email);
        }
        
        return ResponseEntity.ok(response);
    }
} 