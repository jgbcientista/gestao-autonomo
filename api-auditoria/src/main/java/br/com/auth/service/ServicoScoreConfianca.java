package br.com.auth.service;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.ScoreConfianca;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import br.com.auth.infraestrutura.repositorios.RepositorioPerfilComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioScoreConfianca;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável por calcular e gerenciar scores de confiança dos usuários
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoScoreConfianca {

    private final RepositorioScoreConfianca repositorioScoreConfianca;
    private final RepositorioLogAuditoria repositorioLogAuditoria;
    private final RepositorioPerfilComportamentalIA repositorioPerfilIA;

    // Configurações de peso para cálculo do score
    private static final double PESO_HISTORICO_SUCESSO = 0.4;
    private static final double PESO_ANALISE_IA = 0.3;
    private static final double PESO_COMPORTAMENTO_RECENTE = 0.2;
    private static final double PESO_FATORES_EXTERNOS = 0.1;

    // Thresholds para decisões
    private static final double THRESHOLD_CONFIAVEL = 0.7;
    private static final double THRESHOLD_MFA = 0.5;
    private static final double THRESHOLD_BLOQUEIO = 0.2;

    /**
     * Obtém ou cria o score de confiança para um usuário
     */
    @Transactional
    public ScoreConfianca obterOuCriarScore(Usuario usuario) {
        Optional<ScoreConfianca> scoreExistente = repositorioScoreConfianca.findFirstByUsuarioOrderByIdDesc(usuario);
        
        if (scoreExistente.isPresent()) {
            return scoreExistente.get();
        }

        // Criar novo score com valores iniciais
        ScoreConfianca novoScore = ScoreConfianca.builder()
            .usuario(usuario)
            .scoreAtual(0.5) // Score inicial neutro
            .scoreBase(0.5)
            .fatorAjuste(0.0)
            .totalLoginsSucesso(0)
            .totalLoginsSuspeitos(0)
            .totalBloqueios(0)
            .totalMfaExigido(0)
            .emObservacao(false)
            .motivoAlteracao("Score inicial criado")
            .build();

        return repositorioScoreConfianca.save(novoScore);
    }

    /**
     * Calcula o score de confiança baseado no histórico e análise de IA
     */
    @Transactional
    public ScoreConfianca calcularScore(Usuario usuario, PerfilComportamentalIA perfilIA) {
        log.debug("Calculando score de confiança para usuário: {}", usuario.getEmail());

        ScoreConfianca score = obterOuCriarScore(usuario);

        // 1. Score baseado no histórico de sucesso
        double scoreHistorico = calcularScoreHistorico(usuario);
        
        // 2. Score baseado na análise de IA atual
        double scoreIA = calcularScoreIA(perfilIA);
        
        // 3. Score baseado no comportamento recente
        double scoreComportamentoRecente = calcularScoreComportamentoRecente(usuario);
        
        // 4. Fatores externos (tempo desde último login, etc.)
        double scoreFatoresExternos = calcularScoreFatoresExternos(usuario);

        // Calcular score final ponderado
        double scoreCalculado = 
            (scoreHistorico * PESO_HISTORICO_SUCESSO) +
            (scoreIA * PESO_ANALISE_IA) +
            (scoreComportamentoRecente * PESO_COMPORTAMENTO_RECENTE) +
            (scoreFatoresExternos * PESO_FATORES_EXTERNOS);

        // Aplicar fator de ajuste
        scoreCalculado += score.getFatorAjuste() != null ? score.getFatorAjuste() : 0.0;
        
        // Garantir que o score está no intervalo [0, 1]
        scoreCalculado = Math.max(0.0, Math.min(1.0, scoreCalculado));

        // Atualizar o score
        score.setScoreAtual(scoreCalculado);
        score.setScoreBase(scoreHistorico);
                 score.setMotivoAlteracao(String.format(
             "Histórico: %.3f, IA: %.3f, Recente: %.3f, Externos: %.3f",
             scoreHistorico, scoreIA, scoreComportamentoRecente, scoreFatoresExternos
         ));

        log.info("Score de confiança calculado para {}: {:.3f (Nível: {})", 
            usuario.getEmail(), scoreCalculado, score.getNivelConfianca());

        return repositorioScoreConfianca.save(score);
    }

    /**
     * Sobrecarga do método calcularScore que aceita HttpServletRequest
     */
    public Double calcularScore(Usuario usuario, HttpServletRequest request) {
        // Criar um perfil comportamental simplificado baseado no request
        PerfilComportamentalIA perfil = PerfilComportamentalIA.builder()
            .usuario(usuario)
            .ipAcesso(request.getRemoteAddr())
            .userAgent(request.getHeader("User-Agent"))
            .dataHoraAcesso(LocalDateTime.now())
            .classificacaoAcesso(PerfilComportamentalIA.ClassificacaoAcesso.ESPERADO)
            .scoreAnomalia(0.2)
            .build();

        // Calcular o score completo
        ScoreConfianca scoreConfianca = calcularScore(usuario, perfil);
        return scoreConfianca.getScoreAtual();
    }

    /**
     * Atualiza o score após um evento de login
     */
    @Transactional
    public ScoreConfianca atualizarAposLogin(Usuario usuario, TipoEventoLogin tipoEvento, 
                                           PerfilComportamentalIA perfilIA) {
        ScoreConfianca score = obterOuCriarScore(usuario);

        // Atualizar contadores
        switch (tipoEvento) {
            case SUCESSO_NORMAL:
                score.setTotalLoginsSucesso(score.getTotalLoginsSucesso() + 1);
                break;
            case SUCESSO_SUSPEITO:
                score.setTotalLoginsSucesso(score.getTotalLoginsSucesso() + 1);
                score.setTotalLoginsSuspeitos(score.getTotalLoginsSuspeitos() + 1);
                break;
            case BLOQUEADO:
                score.setTotalBloqueios(score.getTotalBloqueios() + 1);
                // Colocar em observação por 24 horas
                score.setEmObservacao(true);
                score.setObservacaoAte(LocalDateTime.now().plusHours(24));
                break;
            case MFA_EXIGIDO:
                score.setTotalMfaExigido(score.getTotalMfaExigido() + 1);
                break;
        }

        // Recalcular o score
        return calcularScore(usuario, perfilIA);
    }

    /**
     * Determina a decisão de autenticação baseada no score
     */
    public DecisaoAutenticacao determinarDecisao(ScoreConfianca score, PerfilComportamentalIA perfilIA) {
        if (score.deveBloquear() || 
            perfilIA.getClassificacaoAcesso() == PerfilComportamentalIA.ClassificacaoAcesso.ALTAMENTE_SUSPEITO) {
            return DecisaoAutenticacao.BLOQUEAR;
        }

        if (score.requerMfa() || 
            perfilIA.getClassificacaoAcesso() == PerfilComportamentalIA.ClassificacaoAcesso.ANOMALO) {
            return DecisaoAutenticacao.EXIGIR_MFA;
        }

        if (score.isConfiavel() && 
            perfilIA.getClassificacaoAcesso() == PerfilComportamentalIA.ClassificacaoAcesso.ESPERADO) {
            return DecisaoAutenticacao.PERMITIR;
        }

        // Decisão baseada em score combinado
        double scoreCombinado = (score.getScoreAtual() + (1.0 - perfilIA.getScoreAnomalia())) / 2.0;

        if (scoreCombinado >= THRESHOLD_CONFIAVEL) {
            return DecisaoAutenticacao.PERMITIR;
        } else if (scoreCombinado >= THRESHOLD_MFA) {
            return DecisaoAutenticacao.EXIGIR_MFA;
        } else {
            return DecisaoAutenticacao.BLOQUEAR;
        }
    }

    /**
     * Ajusta o score manualmente (para casos especiais)
     */
    @Transactional
    public ScoreConfianca ajustarScore(Usuario usuario, double ajuste, String motivo) {
        ScoreConfianca score = obterOuCriarScore(usuario);
        
        double novoAjuste = (score.getFatorAjuste() != null ? score.getFatorAjuste() : 0.0) + ajuste;
        score.setFatorAjuste(novoAjuste);
        score.setMotivoAlteracao(motivo);

        // Recalcular score
        double novoScore = score.getScoreBase() + novoAjuste;
        novoScore = Math.max(0.0, Math.min(1.0, novoScore));
        score.setScoreAtual(novoScore);

        log.info("Score ajustado manualmente para {}: {} (motivo: {})", 
            usuario.getEmail(), novoScore, motivo);

        return repositorioScoreConfianca.save(score);
    }

    // Métodos privados para cálculo de scores

    private double calcularScoreHistorico(Usuario usuario) {
        List<LogAuditoria> historico = repositorioLogAuditoria.findByUsuario(usuario);
        
        if (historico.isEmpty()) {
            return 0.5; // Score neutro para usuários sem histórico
        }

        // Calcular taxa de sucesso
        long loginsSucesso = historico.stream()
            .filter(log -> log.isSucesso())
            .count();

        double taxaSucesso = (double) loginsSucesso / historico.size();

        // Ajustar baseado no volume de histórico
        double fatorVolume = Math.min(1.0, historico.size() / 100.0);
        
        return taxaSucesso * fatorVolume;
    }

    private double calcularScoreIA(PerfilComportamentalIA perfilIA) {
        if (perfilIA == null) {
            return 0.5; // Score neutro se não há análise de IA
        }

        // Inverter o score de anomalia (quanto menor a anomalia, maior a confiança)
        return 1.0 - perfilIA.getScoreAnomalia();
    }

    private double calcularScoreComportamentoRecente(Usuario usuario) {
        LocalDateTime dataInicio = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        LocalDateTime agora = LocalDateTime.now();
        List<LogAuditoria> historicoRecente = repositorioLogAuditoria.findByUsuarioAndCriadoEmBetween(usuario, dataInicio, agora);

        if (historicoRecente.isEmpty()) {
            return 0.5;
        }

        // Analisar consistência de horários, IPs e dispositivos
        long ipsDistintos = historicoRecente.stream()
            .map(LogAuditoria::getEnderecoIp)
            .distinct()
            .count();

        long dispositivosDistintos = historicoRecente.stream()
            .map(LogAuditoria::getUserAgent)
            .distinct()
            .count();

        // Score baseado na consistência (menos variação = mais confiança)
        double scoreConsistencia = 1.0 - Math.min(0.5, (ipsDistintos + dispositivosDistintos) * 0.05);

        return Math.max(0.0, scoreConsistencia);
    }

    private double calcularScoreFatoresExternos(Usuario usuario) {
        double score = 0.5;

        // Fator tempo desde último login
        if (usuario.getLastLoginTime() != null) {
            long horasDesdeUltimoLogin = ChronoUnit.HOURS.between(usuario.getLastLoginTime(), LocalDateTime.now());
            
            if (horasDesdeUltimoLogin < 24) {
                score += 0.2; // Usuário ativo recentemente
            } else if (horasDesdeUltimoLogin > 168) { // 1 semana
                score -= 0.2; // Usuário inativo há muito tempo
            }
        }

        // Fator conta verificada/completa (assumindo que contas não bloqueadas são verificadas)
        if (!Boolean.TRUE.equals(usuario.getAccountLocked())) {
            score += 0.1;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    // Enums e classes auxiliares

    public enum TipoEventoLogin {
        SUCESSO_NORMAL,
        SUCESSO_SUSPEITO,
        BLOQUEADO,
        MFA_EXIGIDO
    }

    public enum DecisaoAutenticacao {
        PERMITIR,
        EXIGIR_MFA,
        BLOQUEAR
    }

    // Método auxiliar para limpar observações expiradas
    @Transactional
    public int limparObservacoesExpiradas() {
        return repositorioScoreConfianca.removerObservacaoExpirada(LocalDateTime.now());
    }
} 