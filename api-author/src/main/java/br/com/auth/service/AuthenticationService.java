package br.com.auth.service;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.interfaces.IServicoAutenticacao;
import br.com.auth.dto.AuthenticationRequest;
import br.com.auth.dto.AuthenticationResponse;
import br.com.auth.dto.RegisterRequest;
import br.com.auth.dto.RequisicaoAutenticacao;
import br.com.auth.dto.RespostaAutenticacao;
import br.com.auth.dto.RequisicaoRegistro;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements IServicoAutenticacao {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioLogAuditoria repositorioLogAuditoria;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BlockchainService blockchainService;
    private final ContextAnalysisService contextAnalysisService;
    private final AiContextAnalysisService aiContextAnalysisService;

    @Transactional
    public RespostaAutenticacao register(RequisicaoRegistro request) {
        if (repositorioUsuario.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Define roles padrão se não fornecidas
        Set<String> userRoles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            userRoles.addAll(request.getRoles());
        } else {
            userRoles.add("USER_DEFAULT");
        }

        var usuario = Usuario.builder()
                .nome(request.getName())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getPassword()))
                .perfis(userRoles)
                .tentativasLoginFalhadas(0)
                .contaBloqueada(false)
                .autenticacaoDoisFatoresHabilitada(false)
                .build();

        repositorioUsuario.save(usuario);

        var jwtToken = jwtService.generateToken(usuario);
        return RespostaAutenticacao.builder()
                .token(jwtToken)
                .nome(usuario.getName())
                .login(usuario.getEmail())
                .build();
    }

    @Transactional
    public RespostaAutenticacao authenticate(RequisicaoAutenticacao request) {
        var usuario = repositorioUsuario.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verifica se a conta está bloqueada
        if (Boolean.TRUE.equals(usuario.getAccountLocked()) && usuario.getAccountLockedUntil() != null 
            && usuario.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Conta bloqueada. Tente novamente mais tarde.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Análise de contexto básica
            contextAnalysisService.analyzeContext(usuario, request);

            // Análise de contexto com IA
            var contextRequest = createContextAnalysisRequest(usuario, request);
            var aiAnalysis = aiContextAnalysisService.analyzeContext(usuario, contextRequest);
            
            // Verifica decisão da IA
            String aiDecision = aiAnalysis.getDecision();
            if ("DENY".equals(aiDecision)) {
                // Registra negação no blockchain
                blockchainService.recordAuthenticationEvent(
                    usuario, "LOGIN_DENIED_AI", "DENIED", 
                    aiAnalysis.getOverallRiskScore(),
                    request.getIpAddress(), request.getLocation(),
                    contextRequest.getDeviceFingerprint()
                );
                
                throw new RuntimeException("Acesso negado pela análise de IA devido ao alto risco detectado");
            }
            
            if ("REQUIRE_MFA".equals(aiDecision) || "REQUIRE_ADDITIONAL_VERIFICATION".equals(aiDecision)) {
                // Em uma implementação real, aqui seria iniciado o processo de MFA
                log.warn("Usuário {} requer verificação adicional: {}", usuario.getEmail(), aiDecision);
            }

            // Atualiza informações de login
            usuario.setLastLoginTime(LocalDateTime.now());
            usuario.setLastLoginIp(request.getIpAddress());
            usuario.setLastLoginLocation(request.getLocation());
            usuario.setLastLoginDevice(request.getUserAgent());
            usuario.setFailedLoginAttempts(0);
            usuario.setAccountLocked(false);
            repositorioUsuario.save(usuario);

            // Registra o log de auditoria
            var logAuditoria = LogAuditoria.builder()
                    .usuario(usuario)
                    .tipoEvento("LOGIN_SUCCESS")
                    .descricao("Login realizado com sucesso")
                    .enderecoIp(request.getIpAddress())
                    .agenteUsuario(request.getUserAgent())
                    .localizacao(request.getLocation())
                    .sucesso(true)
                    .build();
            repositorioLogAuditoria.save(logAuditoria);

            // Registra sucesso no blockchain
            blockchainService.recordAuthenticationEvent(
                usuario, "LOGIN_SUCCESS", "ALLOWED", 
                aiAnalysis.getOverallRiskScore(),
                request.getIpAddress(), request.getLocation(),
                contextRequest.getDeviceFingerprint()
            );

            var jwtToken = jwtService.generateToken(usuario);
            return RespostaAutenticacao.builder()
                    .token(jwtToken)
                    .nome(usuario.getName())
                    .login(usuario.getEmail())
                    .build();

        } catch (Exception e) {
            // Incrementa tentativas de login
            int currentAttempts = usuario.getFailedLoginAttempts() != null ? usuario.getFailedLoginAttempts() : 0;
            usuario.setFailedLoginAttempts(currentAttempts + 1);
            
            // Bloqueia a conta após 5 tentativas
            if (usuario.getFailedLoginAttempts() >= 5) {
                usuario.setAccountLocked(true);
                usuario.setAccountLockedUntil(LocalDateTime.now().plusHours(1));
            }
            
            repositorioUsuario.save(usuario);

            // Registra o log de auditoria
            var logAuditoria = LogAuditoria.builder()
                    .usuario(usuario)
                    .tipoEvento("LOGIN_FAILED")
                    .descricao("Falha no login: " + e.getMessage())
                    .enderecoIp(request.getIpAddress())
                    .agenteUsuario(request.getUserAgent())
                    .localizacao(request.getLocation())
                    .sucesso(false)
                    .motivoFalha(e.getMessage())
                    .build();
            repositorioLogAuditoria.save(logAuditoria);

            // Registra falha no blockchain
            var contextRequestFailure = createContextAnalysisRequest(usuario, request);
            blockchainService.recordAuthenticationEvent(
                usuario, "LOGIN_FAILED", "DENIED", 
                0.8, // Score alto para falhas de credenciais
                request.getIpAddress(), request.getLocation(),
                contextRequestFailure.getDeviceFingerprint()
            );

            throw new RuntimeException("Credenciais inválidas");
        }
    }

    private br.com.auth.dto.RequisicaoAnaliseContexto createContextAnalysisRequest(Usuario usuario, RequisicaoAutenticacao request) {
        // Cria fingerprint do dispositivo baseado no User-Agent
        String deviceFingerprint = request.getUserAgent() != null ? 
            Integer.toHexString(request.getUserAgent().hashCode()) : "unknown";

        // Simula dados de geolocalização (em produção viria do frontend)
        var dadosGeolocalizacao = br.com.auth.dto.RequisicaoAnaliseContexto.DadosGeolocalizacao.builder()
                .pais("BR")
                .regiao("SP")
                .cidade("São Paulo")
                .fusoHorario("America/Sao_Paulo")
                .provedor("Unknown ISP")
                .build();

        // Simula informações de rede (em produção viria de análise do IP)
        var infoRede = br.com.auth.dto.RequisicaoAnaliseContexto.InfoRede.builder()
                .enderecoIp(request.getIpAddress())
                .agenteUsuario(request.getUserAgent())
                .tipoConexao("broadband")
                .vpnDetectado(false)
                .proxyDetectado(false)
                .torDetectado(false)
                .reputacaoIp("UNKNOWN")
                .build();

        // Simula dados comportamentais (em produção viria do frontend)
        var dadosComportamentais = br.com.auth.dto.RequisicaoAnaliseContexto.DadosComportamentais.builder()
                .resolucaoTela("1920x1080")
                .duracaoSessao(120L)
                .interacoesPagina(5)
                .build();

        return br.com.auth.dto.RequisicaoAnaliseContexto.builder()
                .usuarioId(usuario.getId())
                .enderecoIp(request.getIpAddress())
                .agenteUsuario(request.getUserAgent())
                .localizacao(request.getLocation())
                .impressaoDigitalDispositivo(deviceFingerprint)
                .dadosGeolocalizacao(dadosGeolocalizacao)
                .infoRede(infoRede)
                .dadosComportamentais(dadosComportamentais)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Implementação da interface IServicoAutenticacao
    
    @Override
    public AuthenticationResponse registrar(RegisterRequest requisicao) {
        // Converte RegisterRequest para RequisicaoRegistro
        RequisicaoRegistro req = RequisicaoRegistro.builder()
                .nome(requisicao.getName())
                .email(requisicao.getEmail())
                .senha(requisicao.getPassword())
                .perfis(requisicao.getRoles() != null ? Set.copyOf(requisicao.getRoles()) : null)
                .build();
        
        RespostaAutenticacao resposta = register(req);
        
        // Converte RespostaAutenticacao para AuthenticationResponse
        return AuthenticationResponse.builder()
                .token(resposta.getToken())
                .name(resposta.getNome())
                .email(resposta.getLogin())
                .build();
    }
    
    @Override
    public AuthenticationResponse autenticar(AuthenticationRequest requisicao) {
        // Converte AuthenticationRequest para RequisicaoAutenticacao
        RequisicaoAutenticacao req = RequisicaoAutenticacao.builder()
                .email(requisicao.getEmail())
                .senha(requisicao.getPassword())
                .enderecoIp(requisicao.getIpAddress())
                .agenteUsuario(requisicao.getUserAgent())
                .localizacao(requisicao.getLocation())
                .build();
        
        RespostaAutenticacao resposta = authenticate(req);
        
        // Converte RespostaAutenticacao para AuthenticationResponse
        return AuthenticationResponse.builder()
                .token(resposta.getToken())
                .name(resposta.getNome())
                .email(resposta.getLogin())
                .build();
    }
    
    @Override
    public boolean validarToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            Usuario usuario = repositorioUsuario.findByEmail(email).orElse(null);
            if (usuario == null) {
                return false;
            }
            return jwtService.isTokenValid(token, usuario);
        } catch (Exception e) {
            log.error("Erro ao validar token: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String renovarToken(String tokenExpirado) {
        try {
            String email = jwtService.extractUsername(tokenExpirado);
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            return jwtService.generateToken(usuario);
        } catch (Exception e) {
            log.error("Erro ao renovar token: {}", e.getMessage());
            throw new RuntimeException("Token inválido para renovação");
        }
    }
} 