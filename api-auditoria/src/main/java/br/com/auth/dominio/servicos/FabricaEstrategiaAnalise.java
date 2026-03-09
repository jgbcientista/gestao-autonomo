package br.com.auth.dominio.servicos;

import br.com.auth.dominio.interfaces.IEstrategiaAnaliseRisco;
import br.com.auth.infraestrutura.ia.EstrategiaAnaliseLocalizacao;
import br.com.auth.infraestrutura.ia.EstrategiaAnaliseDispositivo;
import br.com.auth.infraestrutura.ia.EstrategiaAnaliseTemporal;
import br.com.auth.infraestrutura.ia.EstrategiaAnaliseComportamental;
import br.com.auth.infraestrutura.ia.EstrategiaAnaliseRede;
import br.com.auth.service.ServicoGeolocalizacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory Pattern para criar diferentes estratégias de análise de risco
 * Centraliza a criação e gerenciamento das estratégias
 */
@Component
public class FabricaEstrategiaAnalise {
    
    private final ServicoGeolocalizacao servicoGeolocalizacao;
    private final Map<String, IEstrategiaAnaliseRisco> estrategias = new HashMap<>();
    
    public FabricaEstrategiaAnalise(ServicoGeolocalizacao servicoGeolocalizacao) {
        this.servicoGeolocalizacao = servicoGeolocalizacao;
        inicializarEstrategias();
    }
    
    private void inicializarEstrategias() {
        estrategias.put("LOCALIZACAO", new EstrategiaAnaliseLocalizacao(servicoGeolocalizacao));
        estrategias.put("DISPOSITIVO", new EstrategiaAnaliseDispositivo());
        estrategias.put("TEMPORAL", new EstrategiaAnaliseTemporal());
        estrategias.put("COMPORTAMENTAL", new EstrategiaAnaliseComportamental());
        estrategias.put("REDE", new EstrategiaAnaliseRede());
    }
    
    /**
     * Obtém estratégia específica por tipo
     * @param tipo Tipo da estratégia
     * @return Estratégia correspondente
     */
    public IEstrategiaAnaliseRisco obterEstrategia(String tipo) {
        IEstrategiaAnaliseRisco estrategia = estrategias.get(tipo.toUpperCase());
        if (estrategia == null) {
            throw new IllegalArgumentException("Estratégia não encontrada: " + tipo);
        }
        return estrategia;
    }
    
    /**
     * Obtém todas as estratégias disponíveis
     * @return Lista com todas as estratégias
     */
    public List<IEstrategiaAnaliseRisco> obterTodasEstrategias() {
        return List.copyOf(estrategias.values());
    }
    
    /**
     * Registra nova estratégia dinamicamente
     * @param tipo Tipo da estratégia
     * @param estrategia Implementação da estratégia
     */
    public void registrarEstrategia(String tipo, IEstrategiaAnaliseRisco estrategia) {
        estrategias.put(tipo.toUpperCase(), estrategia);
    }
} 