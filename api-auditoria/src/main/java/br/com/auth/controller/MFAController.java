package br.com.auth.controller;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dto.AuthenticationResponse;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.service.BlockchainService;
import br.com.auth.service.GerenciadorSessaoService;
import br.com.auth.service.JwtService;
import br.com.auth.service.ServicoMFA;
import br.com.auth.service.ServicoScoreConfianca;
import br.com.auth.service.ServicoGeolocalizacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Controlador para gerenciamento da autenticação multifator (MFA) baseada em TOTP.
 * Permite configurar, verificar e validar códigos TOTP durante o fluxo de autenticação.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
@Tag(name = "MFA", description = "Endpoints para autenticação multifator (TOTP)")
public class MFAController {

    private final ServicoMFA servicoMFA;
    private final RepositorioUsuario repositorioUsuario;
    private final JwtService jwtService;
    private final ServicoScoreConfianca servicoScoreConfianca;
    private final GerenciadorSessaoService gerenciadorSessaoService;
    private final ServicoGeolocalizacao servicoGeolocalizacao;
    @Autowired(required = false)
    private BlockchainService blockchainService;

    @PostMapping("/configurar")
    @Operation(summary = "Iniciar configuração MFA",
               description = "Gera segredo TOTP e QR code para configuração no aplicativo autenticador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "QR code gerado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "MFA já está habilitado")
    })
    public ResponseEntity<?> configurar(@RequestBody Map<String, String> requisicao) {
        try {
            String email = requisicao.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email é obrigatório"));
            }

            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

            if (Boolean.TRUE.equals(usuario.getAutenticacaoDoisFatoresHabilitada())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "MFA já está habilitado para este usuário"
                ));
            }

            ServicoMFA.ResultadoConfiguracaoMFA resultado = servicoMFA.habilitarMFA(usuario);

            log.info("Configuração MFA iniciada para usuário: {}", email);

            return ResponseEntity.ok(Map.of(
                "qrCode", resultado.qrCodeBase64(),
                "secret", resultado.segredo(),
                "message", "Escaneie o QR code com seu aplicativo autenticador e insira o código para confirmar"
            ));

        } catch (UsernameNotFoundException e) {
            log.warn("Usuário não encontrado para configuração MFA: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao configurar MFA: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao configurar MFA"));
        }
    }

    @PostMapping("/verificar-configuracao")
    @Operation(summary = "Verificar configuração MFA",
               description = "Valida o código TOTP inicial para confirmar a configuração do MFA")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MFA configurado e habilitado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Código inválido"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> verificarConfiguracao(@RequestBody Map<String, String> requisicao) {
        try {
            String email = requisicao.get("email");
            String codigo = requisicao.get("codigo");

            if (email == null || email.isBlank() || codigo == null || codigo.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email e código são obrigatórios"));
            }

            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

            boolean verificado = servicoMFA.verificarMFA(usuario, codigo);

            if (verificado) {
                log.info("MFA habilitado com sucesso para usuário: {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "MFA habilitado com sucesso"
                ));
            } else {
                log.warn("Código MFA inválido na verificação de configuração para: {}", email);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Código inválido. Verifique seu aplicativo autenticador e tente novamente"
                ));
            }

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao verificar configuração MFA: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao verificar configuração MFA"));
        }
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar código MFA durante login",
               description = "Valida o código TOTP durante o fluxo de login e retorna token JWT completo se válido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código MFA válido, token JWT retornado"),
        @ApiResponse(responseCode = "401", description = "Código MFA inválido"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> validar(@RequestBody Map<String, String> requisicao, HttpServletRequest httpRequest) {
        try {
            String email = requisicao.get("email");
            String codigo = requisicao.get("codigo");

            if (email == null || email.isBlank() || codigo == null || codigo.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email e código são obrigatórios"));
            }

            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

            if (usuario.getSegredoDoisFatores() == null || usuario.getSegredoDoisFatores().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "MFA não está configurado para este usuário"));
            }

            boolean codigoValido = servicoMFA.validarCodigo(usuario.getSegredoDoisFatores(), codigo);

            if (codigoValido) {
                // Gerar token JWT completo
                String jwtToken = jwtService.generateToken(usuario);

                // Criar sessão
                gerenciadorSessaoService.criarSessao(usuario, jwtToken, httpRequest);

                // Calcular trust score
                double trustScore = 0.5;
                try {
                    trustScore = servicoScoreConfianca.calcularScore(usuario, httpRequest);
                } catch (Exception e) {
                    log.warn("Erro ao calcular trust score após MFA: {}", e.getMessage());
                }

                String trustLevel;
                if (trustScore >= 0.7) trustLevel = "HIGH";
                else if (trustScore >= 0.5) trustLevel = "MEDIUM";
                else if (trustScore >= 0.2) trustLevel = "LOW";
                else trustLevel = "CRITICAL";

                // Registrar no blockchain
                try {
                    if (blockchainService != null) {
                        Map<String, String> localizacao = servicoGeolocalizacao.obterLocalizacao(httpRequest.getRemoteAddr());
                        String locationStr = String.format("%s, %s",
                            localizacao.getOrDefault("cidade", "Unknown"),
                            localizacao.getOrDefault("pais", "Unknown"));
                        blockchainService.recordAuthenticationEvent(
                            usuario, "LOGIN_MFA_SUCCESS", "ALLOWED", 1.0 - trustScore,
                            httpRequest.getRemoteAddr(), locationStr,
                            httpRequest.getHeader("User-Agent"));
                    }
                } catch (Exception e) {
                    log.error("Erro ao registrar MFA no blockchain: {}", e.getMessage());
                }

                log.info("Validação MFA bem-sucedida para usuário: {} (trustScore={})", email, trustScore);

                return ResponseEntity.ok(AuthenticationResponse.builder()
                        .token(jwtToken)
                        .name(usuario.getNome())
                        .email(usuario.getEmail())
                        .role(determinarPerfilPrincipal(usuario))
                        .requiresMfa(false)
                        .mfaMessage("MFA validado com sucesso")
                        .trustScore(trustScore)
                        .trustLevel(trustLevel)
                        .build());
            } else {
                log.warn("Código MFA inválido para usuário: {}", email);

                // Registrar tentativa falha no blockchain
                try {
                    if (blockchainService != null) {
                        blockchainService.recordAuthenticationEvent(
                            usuario, "MFA_FAILED", "DENIED", 0.9,
                            httpRequest.getRemoteAddr(), "Unknown",
                            httpRequest.getHeader("User-Agent"));
                    }
                } catch (Exception e) {
                    log.error("Erro ao registrar falha MFA no blockchain: {}", e.getMessage());
                }

                return ResponseEntity.status(401).body(Map.of(
                    "error", "Código MFA inválido",
                    "message", "O código informado é inválido ou expirou. Tente novamente."
                ));
            }

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao validar MFA: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao validar MFA"));
        }
    }

    @GetMapping("/status/{email}")
    @Operation(summary = "Verificar status do MFA",
               description = "Verifica se o usuário tem MFA habilitado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status do MFA retornado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> status(@PathVariable String email) {
        try {
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

            return ResponseEntity.ok(Map.of(
                "email", usuario.getEmail(),
                "mfaHabilitado", Boolean.TRUE.equals(usuario.getAutenticacaoDoisFatoresHabilitada()),
                "mfaConfigurado", usuario.getSegredoDoisFatores() != null && !usuario.getSegredoDoisFatores().isEmpty()
            ));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao verificar status MFA: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao verificar status MFA"));
        }
    }

    private String determinarPerfilPrincipal(Usuario usuario) {
        Set<String> perfis = usuario.getPerfis();
        if (perfis == null || perfis.isEmpty()) return "USER";
        if (perfis.contains("ADMIN")) return "ADMIN";
        if (perfis.contains("GESTOR")) return "GESTOR";
        if (perfis.contains("AUDITOR")) return "AUDITOR";
        return perfis.iterator().next();
    }
}
