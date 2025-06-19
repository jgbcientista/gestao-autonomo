package br.com.auth.dominio.interfaces;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dto.RequisicaoAnaliseContexto;
import br.com.auth.dto.RespostaAnaliseContexto;

import java.util.List;
import java.util.Map;

/**
 * Interface para serviços de análise de contexto
 * Define o contrato para análise de contexto de autenticação
 */
public interface IServicoAnaliseContexto {
    
    /**
     * Executa análise completa de contexto
     * @param requisicao Dados da requisição de análise
     * @return Resultado da análise de contexto
     */
    RespostaAnaliseContexto analisarContexto(RequisicaoAnaliseContexto requisicao);
    
    /**
     * Analisa padrão de comportamento do usuário
     * @param usuario Usuário a ser analisado
     * @param contexto Dados contextuais
     * @return Score de risco comportamental
     */
    double analisarPadraoComportamento(Usuario usuario, Map<String, Object> contexto);
    
    /**
     * Atualiza padrão de comportamento do usuário
     * @param usuario Usuário a ser atualizado
     * @param dadosComportamento Novos dados comportamentais
     */
    void atualizarPadraoComportamento(Usuario usuario, Map<String, Object> dadosComportamento);
    
    /**
     * Obtém histórico de análises de um usuário
     * @param usuarioId ID do usuário
     * @return Lista de análises anteriores
     */
    List<RespostaAnaliseContexto> obterHistoricoAnalises(Long usuarioId);
} 