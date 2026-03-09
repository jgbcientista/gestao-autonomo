package br.com.auth.service;

import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dto.RequisicaoAnaliseContexto;
import br.com.auth.dto.RespostaAnaliseContexto;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import br.com.auth.infraestrutura.repositorios.RepositorioPadraoComportamentoUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiContextAnalysisService {

    private final RepositorioPadraoComportamentoUsuario repositorioPadraoComportamento;
    private final RepositorioLogAuditoria repositorioLogAuditoria;

    @Value("${risk.threshold.high:0.8}")
    private double highRiskThreshold;

    @Transactional
    public RespostaAnaliseContexto analyzeContext(Usuario usuario, RequisicaoAnaliseContexto request) {
        try {
            String analysisId = UUID.randomUUID().toString();
            log.info("Starting context analysis for user {} with ID {}", usuario.getEmail(), analysisId);

            // Busca ou cria padrão de comportamento do usuário
            PadraoComportamentoUsuario padraoComportamento = getOrCreateBehaviorPattern(usuario);

            // Análise simplificada
            double overallRiskScore = calculateSimplifiedRiskScore(request);
            String decision = determineDecision(overallRiskScore, usuario);
            String riskLevel = determineRiskLevel(overallRiskScore);

            return RespostaAnaliseContexto.builder()
                    .idAnalise(analysisId)
                    .usuarioId(usuario.getId())
                    .pontuacaoRiscoGeral(overallRiskScore)
                    .nivelRisco(riskLevel)
                    .decisao(decision)
                    .pontuacaoConfianca(0.8)
                    .recomendacoes(Arrays.asList("Monitor user activity", "Apply security measures"))
                    .processadoEm(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error analyzing context for user {}", usuario.getEmail(), e);
            throw new RuntimeException("Context analysis failed", e);
        }
    }

    private PadraoComportamentoUsuario getOrCreateBehaviorPattern(Usuario usuario) {
        return repositorioPadraoComportamento.findByUsuario(usuario).orElseGet(() -> {
            PadraoComportamentoUsuario novoPadrao = PadraoComportamentoUsuario.builder()
                    .usuario(usuario)
                    .pontuacaoFrequenciaLogin(0.5)
                    .pontuacaoPadraoHorario(0.5)
                    .pontuacaoLocalizacaoAcesso(0.5)
                    .pontuacaoDispositivoAcesso(0.5)
                    .pontuacaoRiscoGlobal(0.5)
                    .localizacoesTipicas(new HashMap<>())
                    .dispositivosTipicos(new HashMap<>())
                    .frequenciaHorariosLogin(new HashMap<>())
                    .numeroTotalLogins(0)
                    .numeroLoginsSuspeitos(0)
                    .statusPerfilRisco(PadraoComportamentoUsuario.StatusPerfilRisco.BAIXO)
                    .build();
            return repositorioPadraoComportamento.save(novoPadrao);
        });
    }

    private double calculateSimplifiedRiskScore(RequisicaoAnaliseContexto request) {
        double riskScore = 0.3; // Base score
        
        // Análise básica do IP
        if (request.getEnderecoIp() != null && request.getEnderecoIp().startsWith("10.")) {
            riskScore += 0.1; // IP privado = menos risco
        } else {
            riskScore += 0.2; // IP público = mais risco
        }
        
        // Análise básica do horário
        int currentHour = LocalDateTime.now().getHour();
        if (currentHour >= 0 && currentHour < 6) {
            riskScore += 0.3; // Horário suspeito
        }
        
        return Math.min(1.0, riskScore);
    }

    private String determineDecision(double riskScore, Usuario usuario) {
        if (riskScore > highRiskThreshold) {
            return "DENY";
        } else if (riskScore > 0.6) {
            return "REQUIRE_MFA";
        } else {
            return "ALLOW";
        }
    }

    private String determineRiskLevel(double riskScore) {
        if (riskScore > 0.8) return "HIGH";
        if (riskScore > 0.6) return "MEDIUM";
        return "LOW";
    }
} 