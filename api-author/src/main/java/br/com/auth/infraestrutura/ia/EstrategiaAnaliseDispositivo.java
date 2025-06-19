package br.com.auth.infraestrutura.ia;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;
import br.com.auth.dominio.interfaces.IEstrategiaAnaliseRisco;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Estratégia de análise de risco baseada em dispositivo
 * Analisa: impressão digital do dispositivo, integridade, reputação
 */
@Slf4j
@Component
public class EstrategiaAnaliseDispositivo implements IEstrategiaAnaliseRisco {
    
    private static final double PESO_ESTRATEGIA = 0.25; // 25% do peso total
    
    @Override
    public double analisarRisco(Usuario usuario, PadraoComportamentoUsuario padraoComportamento, Map<String, Object> contexto) {
        try {
            double scoreRisco = 0.0;
            
            // Análise da impressão digital do dispositivo
            String impressaoDispositivo = (String) contexto.get("impressaoDispositivo");
            if (impressaoDispositivo != null) {
                scoreRisco += analisarImpressaoDispositivo(impressaoDispositivo, padraoComportamento);
            }
            
            // Análise do sistema operacional
            String sistemaOperacional = (String) contexto.get("sistemaOperacional");
            if (sistemaOperacional != null) {
                scoreRisco += analisarSistemaOperacional(sistemaOperacional);
            }
            
            // Análise do navegador
            String navegador = (String) contexto.get("navegador");
            if (navegador != null) {
                scoreRisco += analisarNavegador(navegador);
            }
            
            // Análise de plugins/extensões suspeitas
            Boolean pluginsSuspeitos = (Boolean) contexto.get("pluginsSuspeitos");
            if (Boolean.TRUE.equals(pluginsSuspeitos)) {
                scoreRisco += 0.3;
            }
            
            log.debug("Análise de dispositivo - Usuário: {}, Score: {}", usuario.getEmail(), scoreRisco);
            return Math.min(scoreRisco, 1.0);
            
        } catch (Exception e) {
            log.error("Erro na análise de dispositivo para usuário: {}", usuario.getEmail(), e);
            return 0.5; // Score médio em caso de erro
        }
    }
    
    private double analisarImpressaoDispositivo(String impressaoDispositivo, PadraoComportamentoUsuario padraoComportamento) {
        double score = 0.0;
        
        // Verifica se é um dispositivo conhecido
        if (padraoComportamento != null) {
            // Simulação - em produção compararia com dispositivos conhecidos
            // Se não for dispositivo conhecido, aumenta o risco
            score += 0.2;
        }
        
        // Verifica se a impressão digital é muito simples (possível bot)
        if (impressaoDispositivo.length() < 50) {
            score += 0.3;
        }
        
        return score;
    }
    
    private double analisarSistemaOperacional(String sistemaOperacional) {
        // Sistemas operacionais raros ou muito antigos aumentam o risco
        String soLower = sistemaOperacional.toLowerCase();
        
        if (soLower.contains("windows xp") || soLower.contains("windows vista")) {
            return 0.4; // Sistemas muito antigos
        }
        
        if (soLower.contains("kali") || soLower.contains("parrot")) {
            return 0.3; // Distribuições focadas em segurança/hacking
        }
        
        return 0.0;
    }
    
    private double analisarNavegador(String navegador) {
        String navegadorLower = navegador.toLowerCase();
        
        // Navegadores automatizados ou raros
        if (navegadorLower.contains("headless") || navegadorLower.contains("phantom")) {
            return 0.5;
        }
        
        if (navegadorLower.contains("selenium") || navegadorLower.contains("webdriver")) {
            return 0.6;
        }
        
        return 0.0;
    }
    
    @Override
    public String getNomeEstrategia() {
        return "ANALISE_DISPOSITIVO";
    }
    
    @Override
    public double getPesoEstrategia() {
        return PESO_ESTRATEGIA;
    }
} 