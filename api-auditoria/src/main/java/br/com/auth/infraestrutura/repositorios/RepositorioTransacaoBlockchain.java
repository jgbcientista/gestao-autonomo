package br.com.auth.infraestrutura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.auth.dominio.entidades.TransacaoBlockchain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioTransacaoBlockchain extends JpaRepository<TransacaoBlockchain, Long> {

    Optional<TransacaoBlockchain> findByHashTransacao(String hashTransacao);

    List<TransacaoBlockchain> findByUsuarioId(Long usuarioId);

    List<TransacaoBlockchain> findByUsuarioEmail(String usuarioEmail);

    List<TransacaoBlockchain> findByTipoEvento(String tipoEvento);

    List<TransacaoBlockchain> findByEnderecoIp(String enderecoIp);

    List<TransacaoBlockchain> findByLocalizacao(String localizacao);

    List<TransacaoBlockchain> findByDecisao(String decisao);

    List<TransacaoBlockchain> findByNomeRede(String nomeRede);

    List<TransacaoBlockchain> findByStatusConfirmacao(TransacaoBlockchain.StatusConfirmacao status);

    List<TransacaoBlockchain> findByVerificado(Boolean verificado);

    @Query("SELECT t FROM TransacaoBlockchain t WHERE t.pontuacaoRisco >= :pontuacao")
    List<TransacaoBlockchain> findByPontuacaoRiscoGreaterThan(@Param("pontuacao") Double pontuacao);

    @Query("SELECT t FROM TransacaoBlockchain t WHERE t.usuarioId = :usuarioId AND t.pontuacaoRisco >= :pontuacao")
    List<TransacaoBlockchain> findByUsuarioIdAndPontuacaoRiscoGreaterThan(@Param("usuarioId") Long usuarioId, @Param("pontuacao") Double pontuacao);

    @Query("SELECT t FROM TransacaoBlockchain t WHERE t.pontuacaoRisco BETWEEN :min AND :max")
    List<TransacaoBlockchain> findByPontuacaoRiscoBetween(@Param("min") Double min, @Param("max") Double max);

    @Query("SELECT t FROM TransacaoBlockchain t WHERE t.criadoEm BETWEEN :inicio AND :fim")
    List<TransacaoBlockchain> findByCriadoEmBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT t FROM TransacaoBlockchain t WHERE t.usuarioId = :usuarioId AND t.criadoEm BETWEEN :inicio AND :fim")
    List<TransacaoBlockchain> findByUsuarioIdAndCriadoEmBetween(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT t FROM TransacaoBlockchain t WHERE t.enderecoIp = :ip AND t.criadoEm BETWEEN :inicio AND :fim")
    List<TransacaoBlockchain> findByEnderecoIpAndCriadoEmBetween(@Param("ip") String enderecoIp, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(t) FROM TransacaoBlockchain t WHERE t.usuarioId = :usuarioId")
    Long countByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(t) FROM TransacaoBlockchain t WHERE t.enderecoIp = :ip")
    Long countByEnderecoIp(@Param("ip") String enderecoIp);

    @Query("SELECT COUNT(t) FROM TransacaoBlockchain t WHERE t.pontuacaoRisco > :pontuacao")
    Long countByPontuacaoRiscoGreaterThan(@Param("pontuacao") Double pontuacao);

    @Query("SELECT AVG(t.pontuacaoRisco) FROM TransacaoBlockchain t WHERE t.usuarioId = :usuarioId")
    Double getMediaPontuacaoRiscoByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT t.enderecoIp FROM TransacaoBlockchain t WHERE t.usuarioId = :usuarioId")
    List<String> findDistinctEnderecoIpByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT t.localizacao FROM TransacaoBlockchain t WHERE t.usuarioId = :usuarioId")
    List<String> findDistinctLocalizacaoByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Métodos auxiliares para compatibilidade com código existente
    default Optional<TransacaoBlockchain> findByTransactionHash(String hashTransacao) {
        return findByHashTransacao(hashTransacao);
    }

    default List<TransacaoBlockchain> findByUserId(Long usuarioId) {
        return findByUsuarioId(usuarioId);
    }

    default List<TransacaoBlockchain> findByUserEmail(String usuarioEmail) {
        return findByUsuarioEmail(usuarioEmail);
    }

    default List<TransacaoBlockchain> findByEventType(String tipoEvento) {
        return findByTipoEvento(tipoEvento);
    }

    default List<TransacaoBlockchain> findByIpAddress(String enderecoIp) {
        return findByEnderecoIp(enderecoIp);
    }

    default List<TransacaoBlockchain> findByLocation(String localizacao) {
        return findByLocalizacao(localizacao);
    }

    default List<TransacaoBlockchain> findByDecision(String decisao) {
        return findByDecisao(decisao);
    }

    default List<TransacaoBlockchain> findByNetworkName(String nomeRede) {
        return findByNomeRede(nomeRede);
    }

    default List<TransacaoBlockchain> findByConfirmationStatus(TransacaoBlockchain.StatusConfirmacao status) {
        return findByStatusConfirmacao(status);
    }

    default List<TransacaoBlockchain> findByVerified(Boolean verificado) {
        return findByVerificado(verificado);
    }

    default List<TransacaoBlockchain> findByRiskScoreGreaterThan(Double pontuacao) {
        return findByPontuacaoRiscoGreaterThan(pontuacao);
    }

    default List<TransacaoBlockchain> findByRiskScoreBetween(Double min, Double max) {
        return findByPontuacaoRiscoBetween(min, max);
    }

    default List<TransacaoBlockchain> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim) {
        return findByCriadoEmBetween(inicio, fim);
    }

    default List<TransacaoBlockchain> findByUserIdAndCreatedAtBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        return findByUsuarioIdAndCriadoEmBetween(usuarioId, inicio, fim);
    }

    default List<TransacaoBlockchain> findByIpAddressAndCreatedAtBetween(String enderecoIp, LocalDateTime inicio, LocalDateTime fim) {
        return findByEnderecoIpAndCriadoEmBetween(enderecoIp, inicio, fim);
    }

    default Long countByUserId(Long usuarioId) {
        return countByUsuarioId(usuarioId);
    }

    default Long countByIpAddress(String enderecoIp) {
        return countByEnderecoIp(enderecoIp);
    }

    default Long countByRiskScoreGreaterThan(Double pontuacao) {
        return countByPontuacaoRiscoGreaterThan(pontuacao);
    }

    default Double getAverageRiskScoreByUserId(Long usuarioId) {
        return getMediaPontuacaoRiscoByUsuarioId(usuarioId);
    }

    default List<String> findDistinctIpAddressByUserId(Long usuarioId) {
        return findDistinctEnderecoIpByUsuarioId(usuarioId);
    }

    default List<String> findDistinctLocationByUserId(Long usuarioId) {
        return findDistinctLocalizacaoByUsuarioId(usuarioId);
    }
} 