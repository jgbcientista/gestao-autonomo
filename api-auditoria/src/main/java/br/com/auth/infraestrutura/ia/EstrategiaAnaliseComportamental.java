package br.com.auth.infraestrutura.ia;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;
import br.com.auth.dominio.interfaces.IEstrategiaAnaliseRisco;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Estratégia de análise de risco baseada em comportamento do usuário
 * Analisa: padrões históricos, desvios comportamentais, consistência
 */
@Slf4j
@Component
public class EstrategiaAnaliseComportamental implements IEstrategiaAnaliseRisco {
    
    private static final double PESO_ESTRATEGIA = 0.3; // 30% do peso total
    
    @Override
    public double analisarRisco(Usuario usuario, PadraoComportamentoUsuario padraoComportamento, Map<String, Object> contexto) {
        try {
            double scoreRisco = 0.0;
            
            if (padraoComportamento == null) {
                // Usuário novo ou sem histórico suficiente
                return 0.3; // Risco moderado para usuários sem histórico
            }
            
            // Análise da pontuação de risco global
            scoreRisco += analisarPontuacaoRiscoGlobal(padraoComportamento);
            
            // Análise de logins suspeitos
            scoreRisco += analisarLoginsSuspeitos(padraoComportamento);
            
            // Análise de frequência de login
            scoreRisco += analisarFrequenciaLogin(padraoComportamento);
            
            // Análise de consistência comportamental
            Double consistenciaBehavioral = (Double) contexto.get("consistenciaBehavioral");
            if (consistenciaBehavioral != null) {
                scoreRisco += analisarConsistencia(consistenciaBehavioral);
            }
            
            // Análise de desvio padrão comportamental
            Double desvioPadrao = (Double) contexto.get("desvioPadrao");
            if (desvioPadrao != null) {
                scoreRisco += analisarDesvioPadrao(desvioPadrao);
            }
            
            log.debug("Análise comportamental - Usuário: {}, Score: {}", usuario.getEmail(), scoreRisco);
            return Math.min(scoreRisco, 1.0);
            
        } catch (Exception e) {
            log.error("Erro na análise comportamental para usuário: {}", usuario.getEmail(), e);
            return 0.5; // Score médio em caso de erro
        }
    }
    
    private double analisarPontuacaoRiscoGlobal(PadraoComportamentoUsuario padraoComportamento) {
        Double pontuacaoRisco = padraoComportamento.getPontuacaoRiscoGeral();
        
        if (pontuacaoRisco == null) {
            return 0.2; // Risco moderado se não há pontuação
        }
        
        // Converte pontuação existente para score de risco
        if (pontuacaoRisco > 0.8) {
            return 0.5; // Alto risco
        } else if (pontuacaoRisco > 0.6) {
            return 0.3; // Risco moderado
        } else if (pontuacaoRisco > 0.4) {
            return 0.1; // Baixo risco
        }
        
        return 0.0; // Risco muito baixo
    }
    
    private double analisarLoginsSuspeitos(PadraoComportamentoUsuario padraoComportamento) {
        Long loginsSuspeitos = padraoComportamento.getContagemAtividadesSuspeitas();
        Long totalLogins = padraoComportamento.getTotalLogins();
        
        if (loginsSuspeitos == null || totalLogins == null || totalLogins == 0) {
            return 0.1;
        }
        
        double percentualSuspeitos = (double) loginsSuspeitos / totalLogins;
        
        if (percentualSuspeitos > 0.3) { // Mais de 30% suspeitos
            return 0.4;
        } else if (percentualSuspeitos > 0.1) { // Mais de 10% suspeitos
            return 0.2;
        }
        
        return 0.0;
    }
    
    private double analisarFrequenciaLogin(PadraoComportamentoUsuario padraoComportamento) {
        Double pontuacaoFrequencia = padraoComportamento.getPontuacaoFrequenciaLogin();
        
        if (pontuacaoFrequencia == null) {
            return 0.1;
        }
        
        // Frequência muito alta ou muito baixa pode ser suspeita
        if (pontuacaoFrequencia > 0.9 || pontuacaoFrequencia < 0.1) {
            return 0.2;
        }
        
        return 0.0;
    }
    
    private double analisarConsistencia(Double consistenciaBehavioral) {
        // Baixa consistência indica comportamento anômalo
        if (consistenciaBehavioral < 0.3) {
            return 0.4; // Alta inconsistência
        } else if (consistenciaBehavioral < 0.6) {
            return 0.2; // Média inconsistência
        }
        
        return 0.0; // Comportamento consistente
    }
    
    private double analisarDesvioPadrao(Double desvioPadrao) {
        // Alto desvio padrão indica comportamento irregular
        if (desvioPadrao > 2.0) {
            return 0.3;
        } else if (desvioPadrao > 1.5) {
            return 0.1;
        }
        
        return 0.0;
    }
    
    @Override
    public String getNomeEstrategia() {
        return "ANALISE_COMPORTAMENTAL";
    }
    
    @Override
    public double getPesoEstrategia() {
        return PESO_ESTRATEGIA;
    }
} 