package br.com.auth.infraestrutura.ia;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;
import br.com.auth.dominio.interfaces.IEstrategiaAnaliseRisco;
import br.com.auth.service.ServicoGeolocalizacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Estratégia de análise de risco baseada em localização
 * Analisa: país, cidade, distância de locais habituais, VPN/proxy
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstrategiaAnaliseLocalizacao implements IEstrategiaAnaliseRisco {
    
    private final ServicoGeolocalizacao servicoGeolocalizacao;
    
    private static final double PESO_ESTRATEGIA = 0.3; // 30% do peso total
    
    @Override
    public double analisarRisco(Usuario usuario, PadraoComportamentoUsuario padraoComportamento, Map<String, Object> contexto) {
        try {
            double scoreRisco = 0.0;
            
            // Análise do país
            String pais = (String) contexto.get("pais");
            if (pais != null) {
                scoreRisco += analisarPais(pais, usuario);
            }
            
            // Análise da cidade
            String cidade = (String) contexto.get("cidade");
            if (cidade != null) {
                scoreRisco += analisarCidade(cidade, usuario);
            }
            
            // Análise de distância de locais habituais
            Double distanciaKm = (Double) contexto.get("distanciaKm");
            if (distanciaKm != null) {
                scoreRisco += analisarDistancia(distanciaKm);
            }
            
            // Análise de uso de VPN/Proxy
            Boolean isVpn = (Boolean) contexto.get("isVpn");
            if (Boolean.TRUE.equals(isVpn)) {
                scoreRisco += 0.4; // VPN aumenta significativamente o risco
            }
            
            // Análise de país de alto risco usando serviço de geolocalização
            String codigoPais = (String) contexto.get("codigoPais");
            if (codigoPais != null && servicoGeolocalizacao.isPaisAltoRisco(codigoPais)) {
                scoreRisco += 0.5;
                log.debug("País de alto risco detectado: {}", codigoPais);
            }
            
            log.debug("Análise de localização - Usuário: {}, Score: {}", usuario.getEmail(), scoreRisco);
            return Math.min(scoreRisco, 1.0);
            
        } catch (Exception e) {
            log.error("Erro na análise de localização para usuário: {}", usuario.getEmail(), e);
            return 0.5; // Score médio em caso de erro
        }
    }
    
    private double analisarPais(String pais, Usuario usuario) {
        // Se o usuário nunca fez login deste país, aumenta o risco
        if (usuario.getUltimoLoginLocalizacao() != null && 
            !usuario.getUltimoLoginLocalizacao().contains(pais)) {
            return 0.3;
        }
        
        // Países com alta incidência de fraude
        String[] paisesAltoRisco = {"CN", "RU", "NG", "PK", "BD"};
        for (String paisRisco : paisesAltoRisco) {
            if (paisRisco.equalsIgnoreCase(pais)) {
                return 0.4;
            }
        }
        
        return 0.0;
    }
    
    private double analisarCidade(String cidade, Usuario usuario) {
        // Verifica se é uma cidade conhecida do usuário
        if (usuario.getUltimoLoginLocalizacao() != null && 
            !usuario.getUltimoLoginLocalizacao().contains(cidade)) {
            return 0.2; // Cidade nova aumenta um pouco o risco
        }
        
        return 0.0;
    }
    
    private double analisarDistancia(Double distanciaKm) {
        // Quanto maior a distância, maior o risco
        if (distanciaKm > 5000) { // Mais de 5000km
            return 0.4;
        } else if (distanciaKm > 1000) { // Mais de 1000km
            return 0.2;
        } else if (distanciaKm > 100) { // Mais de 100km
            return 0.1;
        }
        
        return 0.0;
    }
    
    @Override
    public String getNomeEstrategia() {
        return "ANALISE_LOCALIZACAO";
    }
    
    @Override
    public double getPesoEstrategia() {
        return PESO_ESTRATEGIA;
    }
} 