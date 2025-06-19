package br.com.auth.dominio.interfaces;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;

import java.util.Map;

/**
 * Interface que define o contrato para estratégias de análise de risco
 * Implementa o padrão Strategy do GoF
 * 
 * Cada implementação representa uma estratégia específica de análise:
 * - Análise comportamental
 * - Análise de localização
 * - Análise de dispositivo
 * - Análise temporal
 * - Análise de rede
 */
public interface IEstrategiaAnaliseRisco {
    
    /**
     * Analisa o risco baseado na estratégia específica
     * @param usuario Usuário a ser analisado
     * @param padraoComportamento Padrão de comportamento do usuário
     * @param contexto Dados contextuais da requisição
     * @return Score de risco (0.0 a 1.0)
     */
    double analisarRisco(Usuario usuario, PadraoComportamentoUsuario padraoComportamento, Map<String, Object> contexto);
    
    /**
     * Retorna o nome da estratégia
     * @return Nome identificador da estratégia
     */
    String getNomeEstrategia();
    
    /**
     * Retorna o peso da estratégia no cálculo final
     * @return Peso (0.0 a 1.0)
     */
    double getPesoEstrategia();
} 