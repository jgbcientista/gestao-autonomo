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
    private final ServicoAnaliseComportamentalIA servicoAnaliseComportamentalIA;
    private final ServicoGeolocalizacao servicoGeolocalizacao;
    private final ServicoScoreConfianca servicoScoreConfianca;

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

            // Análise simplificada (IA desabilitada temporariamente para teste)
            log.info("Login simples - análise de IA desabilitada para teste");

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

            // Registra sucesso no blockchain (simplificado)
            try {
                blockchainService.recordAuthenticationEvent(
                    usuario, "LOGIN_SUCCESS", "ALLOWED", 
                    0.8, // Score padrão
                    request.getIpAddress(), request.getLocation(),
                    "simple-device"
                );
            } catch (Exception e) {
                log.warn("Erro ao registrar no blockchain: {}", e.getMessage());
            }

            var jwtToken = jwtService.generateToken(usuario);
            return RespostaAutenticacao.builder()
                    .token(jwtToken)
                    .nome(usuario.getName())
                    .login(usuario.getEmail())
                    .trustScore(0.8)
                    .trustLevel("HIGH")
                    .requiresMfa(false)
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

    private br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosContextoAcesso criarDadosContextoIA(RequisicaoAutenticacao request) {
        return new br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosContextoAcesso(
            request.getIpAddress(),
            request.getUserAgent(),
            request.getLocation(),
            "America/Sao_Paulo", // timezone padrão
            "pt-BR", // idioma padrão
            "1920x1080", // resolução padrão
            1 // tentativas padrão
        );
    }

    private br.com.auth.dto.RequisicaoAnaliseContexto createContextAnalysisRequest(Usuario usuario, RequisicaoAutenticacao request) {
        // Cria fingerprint do dispositivo baseado no User-Agent
        String deviceFingerprint = request.getUserAgent() != null ? 
            Integer.toHexString(request.getUserAgent().hashCode()) : "unknown";

        // Obter dados reais de geolocalização via IP
        ServicoGeolocalizacao.DadosGeolocalizacao geolocalizacao = 
            servicoGeolocalizacao.obterLocalizacaoPorIp(request.getIpAddress());
        
        var dadosGeolocalizacao = br.com.auth.dto.RequisicaoAnaliseContexto.DadosGeolocalizacao.builder()
                .pais(geolocalizacao.getPais() != null ? geolocalizacao.getPais() : "Brasil")
                .regiao(geolocalizacao.getEstado() != null ? geolocalizacao.getEstado() : "São Paulo")
                .cidade(geolocalizacao.getCidade() != null ? geolocalizacao.getCidade() : "São Paulo")
                .fusoHorario(geolocalizacao.getFusoHorario() != null ? geolocalizacao.getFusoHorario() : "America/Sao_Paulo")
                .provedor(geolocalizacao.getProvedor() != null ? geolocalizacao.getProvedor() : "Unknown ISP")
                .latitude(geolocalizacao.getLatitude())
                .longitude(geolocalizacao.getLongitude())
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
        try {
            log.info("Iniciando registro para email: {}", requisicao.getEmail());
            
            // Verifica se o email já existe
            if (repositorioUsuario.existsByEmail(requisicao.getEmail())) {
                throw new RuntimeException("Email já cadastrado");
            }

            // Define roles padrão se não fornecidas
            Set<String> userRoles = new HashSet<>();
            if (requisicao.getRoles() != null && !requisicao.getRoles().isEmpty()) {
                userRoles.addAll(requisicao.getRoles());
            } else {
                userRoles.add("USUARIO_PADRAO");
            }

            // Cria o usuário
            var usuario = Usuario.builder()
                    .nome(requisicao.getName())
                    .email(requisicao.getEmail())
                    .senha(passwordEncoder.encode(requisicao.getPassword()))
                    .perfis(userRoles)
                    .tentativasLoginFalhadas(0)
                    .contaBloqueada(false)
                    .autenticacaoDoisFatoresHabilitada(false)
                    .build();

            // Salva o usuário
            Usuario usuarioSalvo = repositorioUsuario.save(usuario);
            log.info("Usuário {} salvo com sucesso, ID: {}", usuarioSalvo.getEmail(), usuarioSalvo.getId());

            // Gera o token JWT
            var jwtToken = jwtService.generateToken(usuarioSalvo);
            
            // Retorna a resposta
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .name(usuarioSalvo.getName())
                    .email(usuarioSalvo.getEmail())
                    .build();
                    
        } catch (Exception e) {
            log.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Erro no registro: " + e.getMessage(), e);
        }
    }
    
    @Override
    public AuthenticationResponse autenticar(AuthenticationRequest requisicao) {
        try {
            log.info("Iniciando autenticação para usuário: {}", requisicao.getEmail());
            
            // Converte AuthenticationRequest para RequisicaoAutenticacao
            RequisicaoAutenticacao req = RequisicaoAutenticacao.builder()
                    .email(requisicao.getEmail())
                    .senha(requisicao.getPassword())
                    .enderecoIp(requisicao.getIpAddress())
                    .agenteUsuario(requisicao.getUserAgent())
                    .localizacao(requisicao.getLocation())
                    .build();
            
            RespostaAutenticacao resposta = authenticate(req);
            
            log.info("=== DEBUG RESPOSTA AUTENTICACAO ===");
            log.info("Token existe: {}", resposta.getToken() != null);
            log.info("Token length: {}", resposta.getToken() != null ? resposta.getToken().length() : 0);
            log.info("Nome: '{}'", resposta.getNome());
            log.info("Login: '{}'", resposta.getLogin());
            log.info("RequiresMfa: {}", resposta.getRequiresMfa());
            log.info("================================");
            
            // Verifica se é resposta de MFA
            if (Boolean.TRUE.equals(resposta.getRequiresMfa())) {
                // Se MFA é exigido, retorna resposta apropriada
                return AuthenticationResponse.builder()
                        .token(null) // Token não é fornecido até completar MFA
                        .name("MFA_REQUIRED")
                        .email(requisicao.getEmail())
                        .build();
            }
            
            // Converte RespostaAutenticacao para AuthenticationResponse
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(resposta.getToken())
                    .name(resposta.getNome())
                    .email(resposta.getLogin())
                    .build();
            
            log.info("=== DEBUG RESPONSE FINAL ===");
            log.info("Response token existe: {}", response.getToken() != null);
            log.info("Response token length: {}", response.getToken() != null ? response.getToken().length() : 0);
            log.info("Response name: '{}'", response.getName());
            log.info("Response email: '{}'", response.getEmail());
            log.info("============================");
                
            return response;
            
        } catch (Exception e) {
            log.error("Erro durante autenticação para usuário {}: {}", requisicao.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erro na autenticação: " + e.getMessage(), e);
        }
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