package br.com.auth.infraestrutura.ia;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;
import br.com.auth.dominio.interfaces.IEstrategiaAnaliseRisco;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * Estratégia de análise de risco baseada em padrões temporais
 * Analisa: horários habituais, frequência de login, intervalos suspeitos
 */
@Slf4j
@Component
public class EstrategiaAnaliseTemporal implements IEstrategiaAnaliseRisco {
    
    private static final double PESO_ESTRATEGIA = 0.15; // 15% do peso total
    
    @Override
    public double analisarRisco(Usuario usuario, PadraoComportamentoUsuario padraoComportamento, Map<String, Object> contexto) {
        try {
            double scoreRisco = 0.0;
            
            LocalDateTime agora = LocalDateTime.now();
            LocalTime horaAtual = agora.toLocalTime();
            
            // Análise do horário de acesso
            scoreRisco += analisarHorarioAcesso(horaAtual, padraoComportamento);
            
            // Análise da frequência de login
            Integer loginsUltimas24h = (Integer) contexto.get("loginsUltimas24h");
            if (loginsUltimas24h != null) {
                scoreRisco += analisarFrequenciaLogin(loginsUltimas24h);
            }
            
            // Análise do intervalo desde último login
            LocalDateTime ultimoLogin = usuario.getUltimoLoginData();
            if (ultimoLogin != null) {
                scoreRisco += analisarIntervaloUltimoLogin(ultimoLogin, agora);
            }
            
            // Análise de padrão de fim de semana
            Boolean isFimDeSemana = (Boolean) contexto.get("isFimDeSemana");
            if (Boolean.TRUE.equals(isFimDeSemana)) {
                scoreRisco += analisarAcessoFimDeSemana(padraoComportamento);
            }
            
            log.debug("Análise temporal - Usuário: {}, Score: {}", usuario.getEmail(), scoreRisco);
            return Math.min(scoreRisco, 1.0);
            
        } catch (Exception e) {
            log.error("Erro na análise temporal para usuário: {}", usuario.getEmail(), e);
            return 0.5; // Score médio em caso de erro
        }
    }
    
    private double analisarHorarioAcesso(LocalTime horaAtual, PadraoComportamentoUsuario padraoComportamento) {
        int hora = horaAtual.getHour();
        
        // Horários suspeitos (madrugada)
        if (hora >= 2 && hora <= 5) {
            return 0.3; // Acesso na madrugada é mais suspeito
        }
        
        // Horário comercial normal
        if (hora >= 8 && hora <= 18) {
            return 0.0; // Horário normal, sem risco adicional
        }
        
        // Horário noturno
        if (hora >= 22 || hora <= 1) {
            return 0.1; // Leve aumento no risco
        }
        
        return 0.0;
    }
    
    private double analisarFrequenciaLogin(Integer loginsUltimas24h) {
        // Muitos logins em 24h podem indicar ataque
        if (loginsUltimas24h > 20) {
            return 0.5; // Frequência muito alta
        } else if (loginsUltimas24h > 10) {
            return 0.3; // Frequência alta
        } else if (loginsUltimas24h > 5) {
            return 0.1; // Frequência moderada
        }
        
        return 0.0;
    }
    
    private double analisarIntervaloUltimoLogin(LocalDateTime ultimoLogin, LocalDateTime agora) {
        long minutosDesdeUltimoLogin = java.time.Duration.between(ultimoLogin, agora).toMinutes();
        
        // Login muito rápido após o anterior pode indicar bot
        if (minutosDesdeUltimoLogin < 2) {
            return 0.4;
        }
        
        // Login após muito tempo inativo pode ser suspeito
        if (minutosDesdeUltimoLogin > 43200) { // Mais de 30 dias
            return 0.2;
        }
        
        return 0.0;
    }
    
    private double analisarAcessoFimDeSemana(PadraoComportamentoUsuario padraoComportamento) {
        // Se o usuário raramente acessa no fim de semana, aumenta o risco
        if (padraoComportamento != null) {
            // Simulação - em produção analisaria padrões históricos
            return 0.1;
        }
        
        return 0.0;
    }
    
    @Override
    public String getNomeEstrategia() {
        return "ANALISE_TEMPORAL";
    }
    
    @Override
    public double getPesoEstrategia() {
        return PESO_ESTRATEGIA;
    }
} 