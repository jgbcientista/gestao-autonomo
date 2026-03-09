package br.com.auth.infraestrutura.ia;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;
import br.com.auth.dominio.interfaces.IEstrategiaAnaliseRisco;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Estratégia de análise de risco baseada em informações de rede
 * Analisa: IP, User-Agent, provedor de internet, etc.
 */
@Slf4j
@Component
public class EstrategiaAnaliseRede implements IEstrategiaAnaliseRisco {
    
    private static final double PESO_ESTRATEGIA = 0.2; // 20% do peso total
    private static final Pattern IP_PRIVADO_PATTERN = Pattern.compile(
        "^(10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.)"
    );
    private static final Pattern IP_SUSPEITO_PATTERN = Pattern.compile(
        "^(169\\.254\\.|127\\.)"
    );

    @Override
    public double analisarRisco(Usuario usuario, PadraoComportamentoUsuario padraoComportamento, Map<String, Object> contexto) {
        try {
            double scoreRisco = 0.0;
            
            // Análise do endereço IP
            String enderecoIp = (String) contexto.get("enderecoIp");
            if (enderecoIp != null) {
                scoreRisco += analisarEnderecoIp(enderecoIp, usuario);
            }
            
            // Análise do User-Agent
            String userAgent = (String) contexto.get("userAgent");
            if (userAgent != null) {
                scoreRisco += analisarUserAgent(userAgent, usuario);
            }
            
            // Análise do provedor de internet
            String provedor = (String) contexto.get("provedorInternet");
            if (provedor != null) {
                scoreRisco += analisarProvedor(provedor);
            }
            
            // Análise de proxy/VPN
            Boolean isProxy = (Boolean) contexto.get("isProxy");
            if (Boolean.TRUE.equals(isProxy)) {
                scoreRisco += 0.3; // Proxy/VPN aumenta o risco
            }
            
            log.debug("Análise de rede - Usuário: {}, Score: {}", usuario.getEmail(), scoreRisco);
            return Math.min(scoreRisco, 1.0);
            
        } catch (Exception e) {
            log.error("Erro na análise de rede para usuário: {}", usuario.getEmail(), e);
            return 0.5; // Score médio em caso de erro
        }
    }
    
    private double analisarEnderecoIp(String enderecoIp, Usuario usuario) {
        double score = 0.0;
        
        // Verifica se é IP suspeito
        if (IP_SUSPEITO_PATTERN.matcher(enderecoIp).find()) {
            score += 0.4;
        }
        
        // Verifica se é IP privado (pode indicar proxy)
        if (IP_PRIVADO_PATTERN.matcher(enderecoIp).find()) {
            score += 0.1;
        }
        
        // Compara com IPs anteriores do usuário
        if (usuario.getUltimoLoginIp() != null && !usuario.getUltimoLoginIp().equals(enderecoIp)) {
            score += 0.2; // IP diferente aumenta risco
        }
        
        return score;
    }
    
    private double analisarUserAgent(String userAgent, Usuario usuario) {
        double score = 0.0;
        
        // Verifica se User-Agent é suspeito (muito simples ou automatizado)
        if (userAgent.length() < 20) {
            score += 0.3;
        }
        
        // Verifica padrões de bots
        String userAgentLower = userAgent.toLowerCase();
        if (userAgentLower.contains("bot") || userAgentLower.contains("crawler") || 
            userAgentLower.contains("spider")) {
            score += 0.5;
        }
        
        return score;
    }
    
    private double analisarProvedor(String provedor) {
        // Lista de provedores conhecidos por permitir VPNs/proxies
        String[] provedoresRisco = {"tor", "vpn", "proxy", "hosting", "datacenter"};
        
        String provedorLower = provedor.toLowerCase();
        for (String risco : provedoresRisco) {
            if (provedorLower.contains(risco)) {
                return 0.4;
            }
        }
        
        return 0.0;
    }
    
    @Override
    public String getNomeEstrategia() {
        return "ANALISE_REDE";
    }
    
    @Override
    public double getPesoEstrategia() {
        return PESO_ESTRATEGIA;
    }
} 