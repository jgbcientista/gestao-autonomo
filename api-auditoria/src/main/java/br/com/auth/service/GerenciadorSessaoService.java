package br.com.auth.service;

import br.com.auth.dominio.entidades.SessaoAtiva;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioSessaoAtiva;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GerenciadorSessaoService {

    private final RepositorioSessaoAtiva repositorioSessaoAtiva;
    private final ServicoGeolocalizacao servicoGeolocalizacao;
    private final ServicoScoreConfianca servicoScoreConfianca;
    private final NotificacaoService notificacaoService;

    @Value("${session.timeout.minutes:30}")
    private int sessionTimeoutMinutes;

    @Value("${session.max-concurrent:3}")
    private int maxConcurrentSessions;

    @Transactional
    public SessaoAtiva criarSessao(Usuario usuario, String token, HttpServletRequest request) {
        log.info("Criando nova sessão para usuário: {}", usuario.getEmail());

        // Verificar limite de sessões
        List<SessaoAtiva> sessoesAtivas = repositorioSessaoAtiva.findByUsuario(usuario)
            .stream()
            .filter(s -> "ATIVA".equals(s.getStatus()))
            .toList();

        if (sessoesAtivas.size() >= maxConcurrentSessions) {
            log.warn("Limite de sessões atingido para usuário: {}. Encerrando sessão mais antiga.", usuario.getEmail());
            SessaoAtiva sessaoMaisAntiga = sessoesAtivas.stream()
                .min((s1, s2) -> s1.getLoginTime().compareTo(s2.getLoginTime()))
                .orElseThrow();
            encerrarSessao(sessaoMaisAntiga.getTokenJwt());
        }

        // Obter informações do dispositivo e localização
        String deviceInfo = extrairInformacoesDispositivo(request);
        String browserInfo = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        Map<String, String> locationInfo = servicoGeolocalizacao.obterLocalizacao(ipAddress);
        String location = String.format("%s, %s", locationInfo.get("cidade"), locationInfo.get("pais"));

        // Calcular score de confiança
        Double trustScore = servicoScoreConfianca.calcularScore(usuario, request);
        String riskLevel = determinarNivelRisco(trustScore);

        // Criar nova sessão
        var sessao = SessaoAtiva.builder()
                .usuario(usuario)
                .tokenJwt(token)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .browserInfo(browserInfo)
                .location(location)
                .status("ATIVA")
                .trustScore(trustScore)
                .riskLevel(riskLevel)
                .loginTime(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .build();

        sessao = repositorioSessaoAtiva.save(sessao);

        // Enviar notificações
        notificacaoService.notificarNovaSessao(sessao);
        if ("ALTO".equals(riskLevel)) {
            notificacaoService.notificarSessaoSuspeita(sessao);
        }

        return sessao;
    }

    @Transactional
    public void atualizarAtividade(String token) {
        repositorioSessaoAtiva.findByTokenJwt(token).ifPresent(sessao -> {
            sessao.setLastActivity(LocalDateTime.now());
            repositorioSessaoAtiva.save(sessao);
        });
    }

    @Transactional
    public void encerrarSessao(String token) {
        repositorioSessaoAtiva.findByTokenJwt(token).ifPresent(sessao -> {
            sessao.setStatus("ENCERRADA");
            repositorioSessaoAtiva.save(sessao);
            log.info("Sessão encerrada para o token: {}", token);
        });
    }

    @Transactional
    public void encerrarSessoesUsuario(Usuario usuario) {
        List<SessaoAtiva> sessoes = repositorioSessaoAtiva.findByUsuario(usuario);
        sessoes.forEach(sessao -> {
            sessao.setStatus("ENCERRADA");
            repositorioSessaoAtiva.save(sessao);
        });
        log.info("Todas as sessões encerradas para o usuário: {}", usuario.getEmail());
    }

    @Scheduled(fixedRateString = "${session.cleanup.interval:300000}")
    @Transactional
    public void limparSessoesExpiradas() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(sessionTimeoutMinutes);
        List<SessaoAtiva> sessoesAtivas = repositorioSessaoAtiva.findActiveSessions(threshold);
        
        sessoesAtivas.stream()
            .filter(sessao -> sessao.getLastActivity().isBefore(threshold))
            .forEach(sessao -> {
                sessao.setStatus("EXPIRADA");
                repositorioSessaoAtiva.save(sessao);
                log.info("Sessão expirada para o usuário: {}", sessao.getUsuario().getEmail());
            });
    }

    public Map<String, Object> obterEstatisticasSessoes() {
        long sessoesAtivas = repositorioSessaoAtiva.countActiveSessions();
        long dispositivosUnicos = repositorioSessaoAtiva.countUniqueDevices();
        long locacoesUnicas = repositorioSessaoAtiva.countUniqueLocations();
        List<SessaoAtiva> sessoesAltoRisco = repositorioSessaoAtiva.findHighRiskSessions();

        return Map.of(
            "totalSessions", sessoesAtivas,
            "activeSessions", sessoesAtivas,
            "suspiciousSessions", sessoesAltoRisco.size(),
            "uniqueDevices", dispositivosUnicos,
            "uniqueLocations", locacoesUnicas
        );
    }

    private String extrairInformacoesDispositivo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // Lógica simplificada - em produção usar uma biblioteca como user-agent-utils
        if (userAgent == null) return "Desconhecido";
        
        if (userAgent.contains("Mobile")) return "Mobile";
        if (userAgent.contains("Tablet")) return "Tablet";
        return "Desktop";
    }

    private String determinarNivelRisco(Double trustScore) {
        if (trustScore >= 0.8) return "BAIXO";
        if (trustScore >= 0.5) return "MEDIO";
        return "ALTO";
    }
} 