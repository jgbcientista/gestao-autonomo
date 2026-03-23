package br.com.auth.controller;

import br.com.auth.service.SimulacaoAtaqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para simulacao de cenarios de ataque.
 * Permite demonstrar como o sistema de score de confianca
 * reage a diferentes tipos de ameacas.
 */
@RestController
@RequestMapping("/api/v1/simulacao")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Simulacao de Ataques", description = "APIs para simulacao de cenarios de ataque")
public class SimulacaoAtaqueController {

    private final SimulacaoAtaqueService simulacaoAtaqueService;

    @PostMapping("/forca-bruta/{email}")
    @Operation(summary = "Simula ataque de forca bruta",
            description = "Simula 10 tentativas rapidas de login do mesmo IP")
    public ResponseEntity<Map<String, Object>> simularForcaBruta(@PathVariable String email) {
        try {
            log.info("Requisicao de simulacao de forca bruta para: {}", email);
            Map<String, Object> resultado = simulacaoAtaqueService.simularForcaBruta(email);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro na simulacao de forca bruta para: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro na simulacao: " + e.getMessage()));
        }
    }

    @PostMapping("/credential-stuffing/{email}")
    @Operation(summary = "Simula ataque de credential stuffing",
            description = "Simula logins de 8 IPs internacionais diferentes")
    public ResponseEntity<Map<String, Object>> simularCredentialStuffing(@PathVariable String email) {
        try {
            log.info("Requisicao de simulacao de credential stuffing para: {}", email);
            Map<String, Object> resultado = simulacaoAtaqueService.simularCredentialStuffing(email);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro na simulacao de credential stuffing para: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro na simulacao: " + e.getMessage()));
        }
    }

    @PostMapping("/viagem-impossivel/{email}")
    @Operation(summary = "Simula viagem impossivel",
            description = "Simula login em Sao Paulo seguido de login em Tokyo 5 minutos depois")
    public ResponseEntity<Map<String, Object>> simularViagemImpossivel(@PathVariable String email) {
        try {
            log.info("Requisicao de simulacao de viagem impossivel para: {}", email);
            Map<String, Object> resultado = simulacaoAtaqueService.simularViagemImpossivel(email);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro na simulacao de viagem impossivel para: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro na simulacao: " + e.getMessage()));
        }
    }

    @PostMapping("/sequestro-dispositivo/{email}")
    @Operation(summary = "Simula sequestro de dispositivo",
            description = "Simula acesso de dispositivo e localizacao completamente diferentes")
    public ResponseEntity<Map<String, Object>> simularSequestroDispositivo(@PathVariable String email) {
        try {
            log.info("Requisicao de simulacao de sequestro de dispositivo para: {}", email);
            Map<String, Object> resultado = simulacaoAtaqueService.simularSequestroDispositivo(email);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro na simulacao de sequestro de dispositivo para: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro na simulacao: " + e.getMessage()));
        }
    }

    @PostMapping("/resetar/{email}")
    @Operation(summary = "Reseta simulacao",
            description = "Reseta o score de confianca do usuario para o valor inicial")
    public ResponseEntity<Map<String, Object>> resetarSimulacao(@PathVariable String email) {
        try {
            log.info("Requisicao de reset de simulacao para: {}", email);
            Map<String, Object> resultado = simulacaoAtaqueService.resetarSimulacao(email);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro ao resetar simulacao para: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao resetar: " + e.getMessage()));
        }
    }
}
