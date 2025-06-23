package br.com.auth.controller;

import br.com.auth.service.JavaBlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para Blockchain Java Nativa
 * Disponibiliza endpoints para interagir com a blockchain implementada em Java
 */
@Slf4j
@RestController
@RequestMapping("/api/java-blockchain")
@RequiredArgsConstructor
@Tag(name = "Java Blockchain", description = "Endpoints para blockchain nativa Java")
@ConditionalOnProperty(value = "blockchain.native.enabled", havingValue = "true")
public class JavaBlockchainController {

    private final JavaBlockchainService javaBlockchainService;

    /**
     * Obter estatísticas da blockchain
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter estatísticas da blockchain Java")
    public ResponseEntity<Map<String, Object>> getBlockchainStats() {
        log.info("Obtendo estatísticas da blockchain Java");
        Map<String, Object> stats = javaBlockchainService.getBlockchainStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Verificar integridade da blockchain
     */
    @GetMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar integridade da blockchain")
    public ResponseEntity<Map<String, Object>> verifyIntegrity() {
        log.info("Verificando integridade da blockchain");
        boolean isValid = javaBlockchainService.verifyBlockchainIntegrity();
        
        return ResponseEntity.ok(Map.of(
            "valid", isValid,
            "message", isValid ? "Blockchain íntegra" : "Blockchain corrompida",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Obter todos os blocos
     */
    @GetMapping("/blocks")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter todos os blocos da blockchain")
    public ResponseEntity<List<JavaBlockchainService.Block>> getAllBlocks() {
        log.info("Obtendo todos os blocos da blockchain");
        List<JavaBlockchainService.Block> blocks = javaBlockchainService.getAllBlocks();
        return ResponseEntity.ok(blocks);
    }

    /**
     * Obter bloco específico por hash
     */
    @GetMapping("/blocks/{blockHash}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter bloco específico por hash")
    public ResponseEntity<JavaBlockchainService.Block> getBlock(@PathVariable String blockHash) {
        log.info("Obtendo bloco: {}", blockHash);
        Optional<JavaBlockchainService.Block> block = javaBlockchainService.getBlock(blockHash);
        
        return block.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Health check específico da blockchain Java
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar saúde da blockchain Java")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check da blockchain Java");
        
        try {
            Map<String, Object> stats = javaBlockchainService.getBlockchainStats();
            boolean isValid = javaBlockchainService.verifyBlockchainIntegrity();
            
            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "blockchain", "JavaBlockchain",
                "valid", isValid,
                "totalBlocks", stats.get("totalBlocks"),
                "pendingTransactions", stats.get("pendingTransactions"),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Erro no health check: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Ping para teste básico
     */
    @GetMapping("/ping")
    @Operation(summary = "Ping da blockchain Java")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
            "message", "pong",
            "blockchain", "JavaBlockchain",
            "timestamp", System.currentTimeMillis()
        ));
    }
} 