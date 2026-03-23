package br.com.auth.controller;

import br.com.auth.dominio.interfaces.IServicoAutenticacao;
import br.com.auth.dto.AuthenticationRequest;
import br.com.auth.dto.AuthenticationResponse;
import br.com.auth.dto.RegisterRequest;
import br.com.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticação refatorado
 * Segue padrão MVC e princípios SOLID
 * Usa interfaces para inversão de dependência (DIP)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/autenticacao")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
public class AutenticacaoController {

    private final IServicoAutenticacao servicoAutenticacao;
    private final AuthenticationService authenticationService;

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo usuário", 
               description = "Registra um novo usuário no sistema com análise de contexto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Email já existe")
    })
    public ResponseEntity<AuthenticationResponse> registrar(
            @RequestBody RegisterRequest requisicao,
            HttpServletRequest request) {
        
        try {
            log.info("Tentativa de registro para email: {}", requisicao.getEmail());
            
            AuthenticationResponse resposta = servicoAutenticacao.registrar(requisicao);
            
            log.info("Usuário {} registrado com sucesso", requisicao.getEmail());
            return ResponseEntity.ok(resposta);
            
        } catch (Exception e) {
            log.error("Erro ao registrar usuário {}: {}", requisicao.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/entrar")
    @Operation(summary = "Autenticar usuário", 
               description = "Autentica usuário com análise inteligente de contexto usando IA")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "423", description = "Conta bloqueada"),
        @ApiResponse(responseCode = "403", description = "Acesso negado pela IA")
    })
    public ResponseEntity<AuthenticationResponse> entrar(
            @RequestBody AuthenticationRequest requisicao,
            HttpServletRequest request) {
        
        try {
            // Enriquece requisição com dados do contexto HTTP
            enriquecerContextoRequisicao(requisicao, request);
            
            log.info("Tentativa de login para usuário: {} de IP: {}", 
                    requisicao.getEmail(), requisicao.getIpAddress());
            
            AuthenticationResponse resposta = servicoAutenticacao.autenticar(requisicao);
            
            log.info("Login bem-sucedido para usuário: {}", requisicao.getEmail());
            return ResponseEntity.ok(resposta);
            
        } catch (RuntimeException e) {
            log.warn("Falha na autenticação para usuário {}: {}", 
                    requisicao.getEmail(), e.getMessage());
            
            // Retorna diferentes códigos baseado no tipo de erro
            if (e.getMessage().contains("bloqueada")) {
                return ResponseEntity.status(423).build(); // Locked
            } else if (e.getMessage().contains("negado pela análise de IA")) {
                return ResponseEntity.status(403).build(); // Forbidden
            } else {
                return ResponseEntity.status(401).build(); // Unauthorized
            }
        }
    }

    @PostMapping("/validar-token")
    @Operation(summary = "Validar token JWT", 
               description = "Valida se um token JWT está válido e não expirado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token válido"),
        @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<Boolean> validarToken(@RequestParam String token) {
        try {
            boolean valido = servicoAutenticacao.validarToken(token);
            return ResponseEntity.ok(valido);
            
        } catch (Exception e) {
            log.error("Erro ao validar token: {}", e.getMessage());
            return ResponseEntity.status(401).body(false);
        }
    }

    @PostMapping("/renovar-token")
    @Operation(summary = "Renovar token JWT", 
               description = "Renova um token JWT expirado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token inválido para renovação")
    })
    public ResponseEntity<String> renovarToken(@RequestParam String tokenExpirado) {
        try {
            String novoToken = servicoAutenticacao.renovarToken(tokenExpirado);
            return ResponseEntity.ok(novoToken);
            
        } catch (Exception e) {
            log.error("Erro ao renovar token: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Status do serviço", 
               description = "Verifica se o serviço de autenticação está funcionando")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Serviço de autenticação operacional");
    }

    @PostMapping("/atualizar-roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar roles do usuário",
               description = "Atualiza as roles de um usuário no sistema (requer ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles atualizadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<String> atualizarRoles(@RequestParam String email, @RequestParam String novaRole) {
        try {
            authenticationService.atualizarRoles(email, novaRole);
            return ResponseEntity.ok("Roles atualizadas com sucesso");
        } catch (Exception e) {
            log.error("Erro ao atualizar roles do usuário {}: {}", email, e.getMessage());
            return ResponseEntity.status(400).body("Erro ao atualizar roles do usuário");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    // Métodos auxiliares privados

    private void enriquecerContextoRequisicao(AuthenticationRequest requisicao, HttpServletRequest request) {
        // Define IP se não foi fornecido
        if (requisicao.getIpAddress() == null) {
            String ip = obterEnderecoIPReal(request);
            requisicao.setIpAddress(ip);
        }

        // Define User-Agent se não foi fornecido
        if (requisicao.getUserAgent() == null) {
            String userAgent = request.getHeader("User-Agent");
            requisicao.setUserAgent(userAgent);
        }

        // Define localização padrão se não foi fornecida
        if (requisicao.getLocation() == null) {
            requisicao.setLocation("São Paulo, BR"); // Padrão
        }
    }

    private String obterEnderecoIPReal(HttpServletRequest request) {
        // Verifica headers de proxy/load balancer
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
} 