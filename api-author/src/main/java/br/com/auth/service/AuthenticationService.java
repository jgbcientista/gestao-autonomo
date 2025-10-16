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
import br.com.auth.exception.RegistrationException;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    @Autowired(required = false)
    private JavaBlockchainService javaBlockchainService;
    private final ContextAnalysisService contextAnalysisService;
    private final AiContextAnalysisService aiContextAnalysisService;
    private final ServicoAnaliseComportamentalIA servicoAnaliseComportamentalIA;
    private final ServicoGeolocalizacao servicoGeolocalizacao;
    private final ServicoScoreConfianca servicoScoreConfianca;
    private final GerenciadorSessaoService gerenciadorSessaoService;

    @Value("${blockchain.native.enabled:false}")
    private boolean useNativeBlockchain;

    @Transactional
    public RespostaAutenticacao register(RequisicaoRegistro request) {
        if (repositorioUsuario.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Define roles baseado no email e nome do usuário
        Set<String> userRoles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            userRoles.addAll(request.getRoles());
        } else {
            // Lógica inteligente para definir roles
            userRoles.addAll(determineUserRoles(request.getEmail(), request.getName()));
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
        log.info("Usuário {} registrado com sucesso, Roles: {}", usuario.getEmail(), usuario.getPerfis());

        var jwtToken = jwtService.generateToken(usuario);
        return RespostaAutenticacao.builder()
                .token(jwtToken)
                .nome(usuario.getName())
                .login(usuario.getEmail())
                .trustScore(0.8)
                .trustLevel("HIGH")
                .requiresMfa(false)
                .role(usuario.getPerfis().stream().findFirst().orElse("USER"))
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpRequest) {
        try {
            var usuario = repositorioUsuario.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            if (usuario.getAccountLocked() != null && usuario.getAccountLocked()) {
                log.warn("Tentativa de login em conta bloqueada: {}", request.getEmail());
                throw new BadCredentialsException("Conta bloqueada. Tente novamente mais tarde.");
            }

            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            // Gerar token JWT
            var jwtToken = jwtService.generateToken(usuario);

            // Criar nova sessão
            gerenciadorSessaoService.criarSessao(usuario, jwtToken, httpRequest);

            // Atualizar dados do usuário
            atualizarDadosLogin(usuario, httpRequest);

            // Registrar log de auditoria
            registrarLogAuditoria(usuario, "LOGIN_SUCCESS", httpRequest);

            // Registrar no blockchain
            try {
                Map<String, String> localizacao = servicoGeolocalizacao.obterLocalizacao(httpRequest.getRemoteAddr());
                String locationStr = String.format("%s, %s", localizacao.get("cidade"), localizacao.get("pais"));
                
                recordBlockchainEvent(usuario, "LOGIN_SUCCESS", "ALLOWED", 0.8, 
                    httpRequest.getRemoteAddr(), 
                    locationStr,
                    httpRequest.getHeader("User-Agent"));
            } catch (Exception e) {
                log.error("Erro ao registrar evento no blockchain", e);
            }

            return buildAuthResponse(usuario, jwtToken);
        } catch (Exception e) {
            log.error("Erro durante autenticação: {}", e.getMessage());
            throw new BadCredentialsException("Falha na autenticação: " + e.getMessage());
        }
    }

    private void registrarLogAuditoria(Usuario usuario, String evento, HttpServletRequest request) {
        try {
            Map<String, String> localizacao = servicoGeolocalizacao.obterLocalizacao(request.getRemoteAddr());
            String locationStr = String.format("%s, %s", localizacao.get("cidade"), localizacao.get("pais"));
            
            LogAuditoria log = LogAuditoria.builder()
                .usuario(usuario)
                .tipoEvento(evento)
                .enderecoIp(request.getRemoteAddr())
                .agenteUsuario(request.getHeader("User-Agent"))
                .localizacao(locationStr)
                .infoDispositivo(request.getHeader("User-Agent"))
                .dataHora(LocalDateTime.now())
                .sucesso(true)
                .build();
            
            repositorioLogAuditoria.save(log);
        } catch (Exception e) {
            log.error("Erro ao registrar log de auditoria", e);
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

            // Define roles baseado no email e nome do usuário
            Set<String> userRoles = new HashSet<>();
            if (requisicao.getRoles() != null && !requisicao.getRoles().isEmpty()) {
                userRoles.addAll(requisicao.getRoles());
            } else {
                // Lógica inteligente para definir roles
                userRoles.addAll(determineUserRoles(requisicao.getEmail(), requisicao.getName()));
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
            log.info("Usuário {} salvo com sucesso, ID: {}, Roles: {}", 
                usuarioSalvo.getEmail(), usuarioSalvo.getId(), usuarioSalvo.getPerfis());

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
    
    /**
     * Determina as roles do usuário baseado no email e nome
     */
    private Set<String> determineUserRoles(String email, String name) {
        Set<String> roles = new HashSet<>();
        
        // Emails específicos de admin
        Set<String> adminEmails = Set.of(
            "admin@empresa.com", 
            "admin@teste.com", 
            "joao@teste.com",
            "joao.guedes@empresa.com",
            "joaoguedesdebrito@gmail.com"
        );
        
        // Verifica por email específico
        if (adminEmails.contains(email.toLowerCase())) {
            roles.add("ADMIN");
            roles.add("USER");
            log.info("Usuário {} definido como ADMIN por email específico", email);
            return roles;
        }
        
        // Verifica se email contém "admin"
        if (email.toLowerCase().contains("admin")) {
            roles.add("ADMIN");
            roles.add("USER");
            log.info("Usuário {} definido como ADMIN por conter 'admin' no email", email);
            return roles;
        }
        
        // Verifica por nome específico
        if (name != null && name.toLowerCase().contains("joão guedes")) {
            roles.add("ADMIN");
            roles.add("USER");
            log.info("Usuário {} definido como ADMIN por nome específico", name);
            return roles;
        }
        
        // Usuário padrão
        roles.add("USER");
        log.info("Usuário {} definido como USER padrão", email);
        return roles;
    }
    
    @Override
    public AuthenticationResponse autenticar(AuthenticationRequest requisicao) {
        try {
            var usuario = repositorioUsuario.findByEmail(requisicao.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            if (usuario.getAccountLocked() != null && usuario.getAccountLocked()) {
                log.warn("Tentativa de login em conta bloqueada: {}", requisicao.getEmail());
                throw new BadCredentialsException("Conta bloqueada. Tente novamente mais tarde.");
            }

            // Autenticar usuário
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    requisicao.getEmail(),
                    requisicao.getPassword()
                )
            );
            
            // Gerar token JWT
            var jwtToken = jwtService.generateToken(usuario);

            // Atualizar último login sem HttpServletRequest
            usuario.setUltimoLoginData(LocalDateTime.now());
            usuario.setUltimoLoginIp("127.0.0.1");
            usuario.setUltimoLoginLocalizacao("Unknown");
            usuario.setUltimoLoginDispositivo("Web");
            repositorioUsuario.save(usuario);

            log.info("Login bem-sucedido para usuário: {}", requisicao.getEmail());

            return buildAuthResponse(usuario, jwtToken);
        } catch (Exception e) {
            log.error("Erro durante autenticação: {}", e.getMessage());
            throw new BadCredentialsException("Falha na autenticação: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validarToken(String token) {
        try {
            var userEmail = jwtService.extractUsername(token);
            var userDetails = repositorioUsuario.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            log.error("Erro ao validar token: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String renovarToken(String tokenExpirado) {
        String email = jwtService.extractUsername(tokenExpirado);
        var usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String novoToken = jwtService.generateToken(usuario);
        
        // Encerrar sessão antiga e criar nova
        gerenciadorSessaoService.encerrarSessao(tokenExpirado);
        gerenciadorSessaoService.criarSessao(usuario, novoToken, null);

        return novoToken;
    }

    /**
     * Método para registrar evento no blockchain apropriado
     * Escolhe entre JavaBlockchainService ou BlockchainService baseado na configuração
     */
    private void recordBlockchainEvent(Usuario usuario, String eventType, String decision, 
                                     Double riskScore, String ipAddress, String location, 
                                     String deviceFingerprint) {
        try {
            if (useNativeBlockchain && javaBlockchainService != null) {
                log.info("🔗 Usando JavaBlockchainService (blockchain nativo)");
                javaBlockchainService.recordAuthenticationEvent(
                    usuario, eventType, decision, riskScore, 
                    ipAddress, location, deviceFingerprint
                );
            } else {
                log.info("🔗 Usando BlockchainService (Hyperledger/Ethereum)");
                blockchainService.recordAuthenticationEvent(
                    usuario, eventType, decision, riskScore, 
                    ipAddress, location, deviceFingerprint
                );
            }
        } catch (Exception e) {
            log.error("🔗 Erro ao registrar no blockchain: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Atualiza as roles de um usuário existente baseado na lógica inteligente
     */
    @Transactional
    public void updateUserRoles(String email) {
        var usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Determina as novas roles baseado no email e nome
        Set<String> newRoles = determineUserRoles(usuario.getEmail(), usuario.getName());
        
        // Atualiza as roles
        usuario.setPerfis(newRoles);
        repositorioUsuario.save(usuario);
        
        log.info("Roles do usuário {} atualizadas para: {}", email, newRoles);
    }
    
    /**
     * Atualiza as roles de todos os usuários baseado na lógica inteligente
     */
    @Transactional
    public void updateAllUserRoles() {
        var usuarios = repositorioUsuario.findAll();
        
        for (Usuario usuario : usuarios) {
            Set<String> newRoles = determineUserRoles(usuario.getEmail(), usuario.getName());
            
            // Só atualiza se as roles mudaram
            if (!usuario.getPerfis().equals(newRoles)) {
                usuario.setPerfis(newRoles);
                repositorioUsuario.save(usuario);
                log.info("Roles do usuário {} atualizadas de {} para {}", 
                    usuario.getEmail(), usuario.getPerfis(), newRoles);
            }
        }
        
        log.info("Atualização de roles concluída para {} usuários", usuarios.size());
    }

    /**
     * Atualiza as roles de um usuário específico
     */
    @Transactional
    public void atualizarRoles(String email, String novaRole) {
        var usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Cria novo conjunto de roles
        Set<String> newRoles = new HashSet<>();
        newRoles.add(novaRole);
        if (!novaRole.equals("USER")) {
            newRoles.add("USER"); // Todo usuário deve ter role USER
        }
        
        // Atualiza as roles
        usuario.setPerfis(newRoles);
        repositorioUsuario.save(usuario);
        
        log.info("Roles do usuário {} atualizadas para: {}", email, newRoles);
    }

    private AuthenticationResponse buildAuthResponse(Usuario usuario, String token) {
        return AuthenticationResponse.builder()
            .token(token)
            .name(usuario.getNome())
            .email(usuario.getEmail())
            .role(usuario.getPerfis().isEmpty() ? "USER" : usuario.getPerfis().iterator().next())
            .build();
    }

    private void atualizarDadosLogin(Usuario usuario, HttpServletRequest request) {
        usuario.setUltimoLoginIp(request.getRemoteAddr());
        usuario.setUltimoLoginDispositivo(request.getHeader("User-Agent"));
        usuario.setUltimoLoginData(LocalDateTime.now());
        usuario.setTentativasLoginFalhadas(0);
        
        // Tenta obter localização do header X-Location ou usa "Local"
        String localizacao = request.getHeader("X-Location");
        usuario.setUltimoLoginLocalizacao(localizacao != null ? localizacao : "Local");
        
        repositorioUsuario.save(usuario);
    }

    private void validarDadosRegistro(RegisterRequest request) {
        // Validar e-mail único
        if (repositorioUsuario.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        
        // Validar senha
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres");
        }
        
        // Validar nome
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }
    }

    private Usuario criarNovoUsuario(RegisterRequest request) {
        Set<String> perfis = new HashSet<>();
        
        // Define perfil baseado no email
        if (request.getEmail().toLowerCase().contains("admin") || 
            request.getEmail().toLowerCase().contains("administrador") ||
            request.getEmail().toLowerCase().equals("admin@auth.com.br")) {
            perfis.add("ADMIN");
        } else {
            perfis.add("USER");
        }
        
        return Usuario.builder()
            .nome(request.getName())
            .email(request.getEmail())
            .senha(passwordEncoder.encode(request.getPassword()))
            .perfis(perfis)
            .contaBloqueada(false)
            .tentativasLoginFalhadas(0)
            .autenticacaoDoisFatoresHabilitada(false)
            .build();
    }

    public AuthenticationResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            // Validar dados
            validarDadosRegistro(request);
            
            // Criar novo usuário
            var usuario = criarNovoUsuario(request);
            
            // Salvar usuário
            repositorioUsuario.save(usuario);
            
            // Gerar token JWT
            var jwtToken = jwtService.generateToken(usuario);
            
            // Atualizar dados do primeiro login
            atualizarDadosLogin(usuario, httpRequest);
            
            // Construir resposta
            return buildAuthResponse(usuario, jwtToken);
            
        } catch (Exception e) {
            log.error("Erro ao registrar usuário: {}", e.getMessage());
            throw new RegistrationException("Erro ao registrar usuário: " + e.getMessage());
        }
    }

    @Transactional
    public void logout(String token) {
        gerenciadorSessaoService.encerrarSessao(token);
    }
} 