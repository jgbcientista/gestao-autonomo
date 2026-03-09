package br.com.auth.controller;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para gerenciar configurações do sistema e usuários
 */
@RestController
@RequestMapping("/api/v1/configuracoes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configurações", description = "APIs para gerenciar configurações do sistema e usuários")
public class ConfiguracoesController {

    private final RepositorioUsuario repositorioUsuario;

    // === CONFIGURAÇÕES DO USUÁRIO ===

    @GetMapping("/usuario")
    @Operation(summary = "Obter configurações do usuário", description = "Retorna as configurações personalizadas do usuário atual")
    public ResponseEntity<Map<String, Object>> obterConfiguracoes(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("Obtendo configurações para usuário: {}", email);

            Optional<Usuario> usuarioOpt = repositorioUsuario.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            
            // Configurações padrão simuladas (em produção, viria de uma tabela de configurações)
            Map<String, Object> configuracoes = new HashMap<>();
            configuracoes.put("id", 1);
            configuracoes.put("userId", usuario.getId());
            
            // Configurações de Segurança
            configuracoes.put("twoFactorEnabled", false);
            configuracoes.put("loginNotifications", true);
            configuracoes.put("suspiciousActivityAlerts", true);
            configuracoes.put("sessionTimeout", 30);
            configuracoes.put("passwordExpirationDays", 90);
            
            // Configurações de Notificações
            configuracoes.put("emailNotifications", true);
            configuracoes.put("pushNotifications", true);
            configuracoes.put("smsNotifications", false);
            configuracoes.put("securityAlerts", true);
            configuracoes.put("systemUpdates", true);
            
            // Configurações de Privacidade
            configuracoes.put("profileVisibility", "PRIVATE");
            configuracoes.put("dataSharing", false);
            configuracoes.put("analyticsTracking", true);
            configuracoes.put("locationTracking", false);
            
            // Configurações Avançadas
            configuracoes.put("theme", "AUTO");
            configuracoes.put("language", "PT_BR");
            configuracoes.put("timezone", "America/Sao_Paulo");
            configuracoes.put("dateFormat", "DD/MM/YYYY");
            
            // Configurações de API
            configuracoes.put("apiRateLimit", 1000);
            configuracoes.put("apiKeyRotationDays", 30);
            
            configuracoes.put("createdAt", LocalDateTime.now().minusDays(30));
            configuracoes.put("updatedAt", LocalDateTime.now());

            return ResponseEntity.ok(configuracoes);

        } catch (Exception e) {
            log.error("Erro ao obter configurações do usuário", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/usuario")
    @Operation(summary = "Atualizar configurações do usuário", description = "Atualiza as configurações personalizadas do usuário")
    public ResponseEntity<Map<String, Object>> atualizarConfiguracoes(
            @RequestBody Map<String, Object> configuracoes,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("Atualizando configurações para usuário: {}", email);

            Optional<Usuario> usuarioOpt = repositorioUsuario.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Em produção, aqui salvaria as configurações em uma tabela específica
            // Por ora, retornamos as configurações recebidas com timestamp atualizado
            configuracoes.put("updatedAt", LocalDateTime.now());
            
            log.info("Configurações atualizadas com sucesso para usuário: {}", email);
            return ResponseEntity.ok(configuracoes);

        } catch (Exception e) {
            log.error("Erro ao atualizar configurações do usuário", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === POLÍTICA DE SEGURANÇA (ADMIN) ===

    @GetMapping("/politica-seguranca")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter política de segurança", description = "Retorna a política de segurança atual do sistema (apenas admin)")
    public ResponseEntity<Map<String, Object>> obterPoliticaSeguranca() {
        try {
            log.info("Obtendo política de segurança do sistema");

            Map<String, Object> politica = new HashMap<>();
            politica.put("minPasswordLength", 8);
            politica.put("requireSpecialChars", true);
            politica.put("requireNumbers", true);
            politica.put("requireUppercase", true);
            politica.put("maxLoginAttempts", 5);
            politica.put("lockoutDuration", 15);

            return ResponseEntity.ok(politica);

        } catch (Exception e) {
            log.error("Erro ao obter política de segurança", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/politica-seguranca")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar política de segurança", description = "Atualiza a política de segurança do sistema (apenas admin)")
    public ResponseEntity<Map<String, Object>> atualizarPoliticaSeguranca(
            @RequestBody Map<String, Object> politica,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("Atualizando política de segurança por admin: {}", email);

            // Em produção, aqui validaria e salvaria a política
            politica.put("updatedAt", LocalDateTime.now());
            politica.put("updatedBy", email);
            
            log.info("Política de segurança atualizada com sucesso por: {}", email);
            return ResponseEntity.ok(politica);

        } catch (Exception e) {
            log.error("Erro ao atualizar política de segurança", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === CONFIGURAÇÃO DO SISTEMA (ADMIN) ===

    @GetMapping("/sistema")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter configuração do sistema", description = "Retorna as configurações gerais do sistema (apenas admin)")
    public ResponseEntity<Map<String, Object>> obterConfiguracaoSistema() {
        try {
            log.info("Obtendo configuração do sistema");

            Map<String, Object> config = new HashMap<>();
            config.put("maintenanceMode", false);
            config.put("maxConcurrentSessions", 10);
            config.put("sessionCleanupInterval", 60);
            config.put("logRetentionDays", 30);
            config.put("backupFrequency", "DAILY");

            return ResponseEntity.ok(config);

        } catch (Exception e) {
            log.error("Erro ao obter configuração do sistema", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/sistema")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar configuração do sistema", description = "Atualiza as configurações gerais do sistema (apenas admin)")
    public ResponseEntity<Map<String, Object>> atualizarConfiguracaoSistema(
            @RequestBody Map<String, Object> config,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("Atualizando configuração do sistema por admin: {}", email);

            // Em produção, aqui validaria e salvaria a configuração
            config.put("updatedAt", LocalDateTime.now());
            config.put("updatedBy", email);
            
            log.info("Configuração do sistema atualizada com sucesso por: {}", email);
            return ResponseEntity.ok(config);

        } catch (Exception e) {
            log.error("Erro ao atualizar configuração do sistema", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === FUNCIONALIDADES ESPECÍFICAS ===

    @PostMapping("/regenerar-chave-api")
    @Operation(summary = "Regenerar chave de API", description = "Gera uma nova chave de API para o usuário")
    public ResponseEntity<Map<String, Object>> regenerarChaveApi(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("Regenerando chave de API para usuário: {}", email);

            Optional<Usuario> usuarioOpt = repositorioUsuario.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Em produção, aqui geraria uma nova chave de API
            String novaChave = "sk-" + System.currentTimeMillis() + "-" + Math.random();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Nova chave de API gerada com sucesso");
            response.put("apiKey", novaChave);
            response.put("generatedAt", LocalDateTime.now());

            log.info("Nova chave de API gerada para usuário: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao regenerar chave de API", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/testar-notificacoes")
    @Operation(summary = "Testar notificações", description = "Envia uma notificação de teste para o usuário")
    public ResponseEntity<Map<String, Object>> testarNotificacoes(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("Enviando notificação de teste para usuário: {}", email);

            Optional<Usuario> usuarioOpt = repositorioUsuario.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Em produção, aqui enviaria uma notificação real
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificação de teste enviada com sucesso");
            response.put("sentTo", email);
            response.put("sentAt", LocalDateTime.now());
            response.put("type", "TEST_NOTIFICATION");

            log.info("Notificação de teste enviada para usuário: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao enviar notificação de teste", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // === ENDPOINTS AUXILIARES ===

    @GetMapping("/tipos-tema")
    @Operation(summary = "Obter tipos de tema", description = "Retorna os tipos de tema disponíveis")
    public ResponseEntity<Map<String, Object>> obterTiposTema() {
        Map<String, Object> response = new HashMap<>();
        response.put("temas", new String[]{"LIGHT", "DARK", "AUTO"});
        return ResponseEntity.ok(response);
    }

    @GetMapping("/idiomas-suportados")
    @Operation(summary = "Obter idiomas suportados", description = "Retorna os idiomas suportados pelo sistema")
    public ResponseEntity<Map<String, Object>> obterIdiomasSuportados() {
        Map<String, Object> response = new HashMap<>();
        response.put("idiomas", new String[]{"PT_BR", "EN_US", "ES_ES"});
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fusos-horarios")
    @Operation(summary = "Obter fusos horários", description = "Retorna os fusos horários disponíveis")
    public ResponseEntity<Map<String, Object>> obterFusosHorarios() {
        Map<String, Object> response = new HashMap<>();
        response.put("fusos", new String[]{
            "America/Sao_Paulo", "America/New_York", "Europe/London", "Asia/Tokyo"
        });
        return ResponseEntity.ok(response);
    }
} 