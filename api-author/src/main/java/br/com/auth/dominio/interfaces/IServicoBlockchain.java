package br.com.auth.dominio.interfaces;

import br.com.auth.dominio.entidades.TransacaoBlockchain;
import br.com.auth.dominio.entidades.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface para operações de blockchain relacionadas à autenticação
 */
public interface IServicoBlockchain {

    /**
     * Registra uma transação de autenticação no blockchain
     * @param usuario usuário que está se autenticando
     * @param tipoEvento tipo do evento (LOGIN, LOGOUT, etc.)
     * @param enderecoIp endereço IP do usuário
     * @param localizacao localização do usuário
     * @param decisao decisão do sistema (APROVADO, NEGADO, etc.)
     * @param pontuacaoRisco pontuação de risco calculada
     * @return transação blockchain criada
     */
    CompletableFuture<TransacaoBlockchain> registrarTransacaoAutenticacao(
            Usuario usuario,
            String tipoEvento,
            String enderecoIp,
            String localizacao,
            String decisao,
            Double pontuacaoRisco
    );

    /**
     * Busca transações por hash
     * @param hashTransacao hash da transação
     * @return transação blockchain se encontrada
     */
    Optional<TransacaoBlockchain> buscarTransacaoPorHash(String hashTransacao);

    /**
     * Busca transações por usuário
     * @param usuarioId ID do usuário
     * @return lista de transações do usuário
     */
    List<TransacaoBlockchain> buscarTransacoesPorUsuario(Long usuarioId);

    /**
     * Verifica se uma transação foi confirmada na rede
     * @param hashTransacao hash da transação
     * @return true se confirmada, false caso contrário
     */
    CompletableFuture<Boolean> verificarConfirmacaoTransacao(String hashTransacao);

    /**
     * Busca transações por pontuação de risco
     * @param pontuacaoMinima pontuação mínima de risco
     * @return lista de transações com risco acima do limite
     */
    List<TransacaoBlockchain> buscarTransacoesPorRisco(Double pontuacaoMinima);
} 