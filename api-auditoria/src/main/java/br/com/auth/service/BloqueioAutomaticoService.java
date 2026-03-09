package br.com.auth.service;

import br.com.auth.dominio.entidades.SessaoAtiva;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioSessaoAtiva;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloqueioAutomaticoService {

    private final RepositorioSessaoAtiva repositorioSessaoAtiva;
    private final RepositorioUsuario repositorioUsuario;
    private final NotificacaoService notificacaoService;
    private final GerenciadorSessaoService gerenciadorSessaoService;

    @Value("${security.auto-block.max-suspicious-sessions:3}")
    private int maxSuspiciousSessions;

    @Value("${security.auto-block.min-trust-score:0.3}")
    private double minTrustScore;

    @Value("${security.auto-block.suspicious-locations-threshold:2}")
    private int suspiciousLocationsThreshold;

    @Scheduled(fixedRateString = "${security.auto-block.check-interval:60000}")
    @Transactional
    public void verificarSessoesSuspeitas() {
        log.info("Iniciando verificação de sessões suspeitas...");

        // Buscar todas as sessões ativas
        List<SessaoAtiva> sessoesAtivas = repositorioSessaoAtiva.findByStatus("ATIVA");

        // Agrupar sessões por usuário
        Map<Usuario, List<SessaoAtiva>> sessoesPorUsuario = sessoesAtivas.stream()
            .collect(Collectors.groupingBy(SessaoAtiva::getUsuario));

        // Analisar cada usuário
        sessoesPorUsuario.forEach(this::analisarSessoesUsuario);
    }

    private void analisarSessoesUsuario(Usuario usuario, List<SessaoAtiva> sessoes) {
        // Contar sessões de alto risco
        long sessoesAltoRisco = sessoes.stream()
            .filter(s -> "ALTO".equals(s.getRiskLevel()))
            .count();

        // Contar localizações únicas
        long locacoesUnicas = sessoes.stream()
            .map(SessaoAtiva::getLocation)
            .distinct()
            .count();

        // Verificar score médio de confiança
        double scoreConfiancaMedio = sessoes.stream()
            .mapToDouble(SessaoAtiva::getTrustScore)
            .average()
            .orElse(1.0);

        boolean bloquearConta = false;
        String motivoBloqueio = "";

        // Regras de bloqueio
        if (sessoesAltoRisco >= maxSuspiciousSessions) {
            bloquearConta = true;
            motivoBloqueio = "Múltiplas sessões de alto risco detectadas";
        } else if (scoreConfiancaMedio < minTrustScore) {
            bloquearConta = true;
            motivoBloqueio = "Score de confiança muito baixo";
        } else if (locacoesUnicas >= suspiciousLocationsThreshold) {
            bloquearConta = true;
            motivoBloqueio = "Múltiplas localizações suspeitas";
        }

        if (bloquearConta) {
            bloquearContaUsuario(usuario, sessoes, motivoBloqueio);
        }
    }

    @Transactional
    public void bloquearContaUsuario(Usuario usuario, List<SessaoAtiva> sessoes, String motivo) {
        log.warn("Bloqueando conta do usuário {} por {}", usuario.getEmail(), motivo);

        // Bloquear a conta
        usuario.setAccountLocked(true);
        usuario.setAccountLockedUntil(LocalDateTime.now().plusHours(24));
        usuario.setLockReason(motivo);
        repositorioUsuario.save(usuario);

        // Encerrar todas as sessões
        gerenciadorSessaoService.encerrarSessoesUsuario(usuario);

        // Registrar no log de auditoria (implementar conforme necessário)
        log.info("Conta bloqueada e sessões encerradas para o usuário: {}", usuario.getEmail());

        // Enviar notificação ao usuário
        if (!sessoes.isEmpty()) {
            notificacaoService.notificarSessaoSuspeita(sessoes.get(0));
        }
    }

    @Transactional
    public void desbloquearConta(Usuario usuario) {
        usuario.setAccountLocked(false);
        usuario.setAccountLockedUntil(null);
        usuario.setLockReason(null);
        usuario.setFailedLoginAttempts(0);
        repositorioUsuario.save(usuario);
        log.info("Conta desbloqueada para o usuário: {}", usuario.getEmail());
    }
} 