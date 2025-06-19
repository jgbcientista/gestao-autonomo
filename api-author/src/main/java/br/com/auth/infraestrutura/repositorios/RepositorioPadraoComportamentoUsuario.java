package br.com.auth.infraestrutura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.auth.dominio.entidades.PadraoComportamentoUsuario;
import br.com.auth.dominio.entidades.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPadraoComportamentoUsuario extends JpaRepository<PadraoComportamentoUsuario, Long> {

    Optional<PadraoComportamentoUsuario> findByUsuario(Usuario usuario);

    List<PadraoComportamentoUsuario> findByStatusPerfilRisco(PadraoComportamentoUsuario.StatusPerfilRisco status);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.pontuacaoRiscoGlobal > :pontuacao")
    List<PadraoComportamentoUsuario> findByPontuacaoRiscoGlobalGreaterThan(@Param("pontuacao") Double pontuacao);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.pontuacaoRiscoGlobal BETWEEN :min AND :max")
    List<PadraoComportamentoUsuario> findByPontuacaoRiscoGlobalBetween(@Param("min") Double min, @Param("max") Double max);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.numeroLoginsSuspeitos > :numero")
    List<PadraoComportamentoUsuario> findByNumeroLoginsSuspeitosGreaterThan(@Param("numero") Integer numero);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.ultimoLoginSuspeito BETWEEN :inicio AND :fim")
    List<PadraoComportamentoUsuario> findByUltimoLoginSuspeitoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.numeroTotalLogins > :numero")
    List<PadraoComportamentoUsuario> findByNumeroTotalLoginsGreaterThan(@Param("numero") Integer numero);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.pontuacaoFrequenciaLogin > :pontuacao")
    List<PadraoComportamentoUsuario> findByPontuacaoFrequenciaLoginGreaterThan(@Param("pontuacao") Double pontuacao);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.pontuacaoLocalizacaoAcesso > :pontuacao")
    List<PadraoComportamentoUsuario> findByPontuacaoLocalizacaoAcessoGreaterThan(@Param("pontuacao") Double pontuacao);

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.pontuacaoDispositivoAcesso > :pontuacao")
    List<PadraoComportamentoUsuario> findByPontuacaoDispositivoAcessoGreaterThan(@Param("pontuacao") Double pontuacao);

    @Query("SELECT COUNT(p) FROM PadraoComportamentoUsuario p WHERE p.statusPerfilRisco = :status")
    Long countByStatusPerfilRisco(@Param("status") PadraoComportamentoUsuario.StatusPerfilRisco status);

    @Query("SELECT AVG(p.pontuacaoRiscoGlobal) FROM PadraoComportamentoUsuario p")
    Double getMediaPontuacaoRiscoGlobal();

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.atualizadoEm BETWEEN :inicio AND :fim")
    List<PadraoComportamentoUsuario> findByAtualizadoEmBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT p FROM PadraoComportamentoUsuario p ORDER BY p.pontuacaoRiscoGlobal DESC")
    List<PadraoComportamentoUsuario> findAllOrderByPontuacaoRiscoGlobalDesc();

    @Query("SELECT p FROM PadraoComportamentoUsuario p WHERE p.statusPerfilRisco IN :statuses")
    List<PadraoComportamentoUsuario> findByStatusPerfilRiscoIn(@Param("statuses") List<PadraoComportamentoUsuario.StatusPerfilRisco> statuses);

    // Métodos auxiliares para compatibilidade com código existente
    default Optional<PadraoComportamentoUsuario> findByUser(Usuario usuario) {
        return findByUsuario(usuario);
    }

    default List<PadraoComportamentoUsuario> findByRiskProfileStatus(PadraoComportamentoUsuario.StatusPerfilRisco status) {
        return findByStatusPerfilRisco(status);
    }

    default List<PadraoComportamentoUsuario> findByOverallRiskScoreGreaterThan(Double pontuacao) {
        return findByPontuacaoRiscoGlobalGreaterThan(pontuacao);
    }

    default List<PadraoComportamentoUsuario> findByOverallRiskScoreBetween(Double min, Double max) {
        return findByPontuacaoRiscoGlobalBetween(min, max);
    }

    default List<PadraoComportamentoUsuario> findBySuspiciousLoginsGreaterThan(Integer numero) {
        return findByNumeroLoginsSuspeitosGreaterThan(numero);
    }

    default List<PadraoComportamentoUsuario> findByLastSuspiciousLoginBetween(LocalDateTime inicio, LocalDateTime fim) {
        return findByUltimoLoginSuspeitoBetween(inicio, fim);
    }

    default List<PadraoComportamentoUsuario> findByTotalLoginsGreaterThan(Integer numero) {
        return findByNumeroTotalLoginsGreaterThan(numero);
    }

    default List<PadraoComportamentoUsuario> findByLoginFrequencyScoreGreaterThan(Double pontuacao) {
        return findByPontuacaoFrequenciaLoginGreaterThan(pontuacao);
    }

    default List<PadraoComportamentoUsuario> findByLocationScoreGreaterThan(Double pontuacao) {
        return findByPontuacaoLocalizacaoAcessoGreaterThan(pontuacao);
    }

    default List<PadraoComportamentoUsuario> findByDeviceScoreGreaterThan(Double pontuacao) {
        return findByPontuacaoDispositivoAcessoGreaterThan(pontuacao);
    }

    default Long countByRiskProfileStatus(PadraoComportamentoUsuario.StatusPerfilRisco status) {
        return countByStatusPerfilRisco(status);
    }

    default Double getAverageOverallRiskScore() {
        return getMediaPontuacaoRiscoGlobal();
    }

    default List<PadraoComportamentoUsuario> findByUpdatedAtBetween(LocalDateTime inicio, LocalDateTime fim) {
        return findByAtualizadoEmBetween(inicio, fim);
    }

    default List<PadraoComportamentoUsuario> findAllOrderByOverallRiskScoreDesc() {
        return findAllOrderByPontuacaoRiscoGlobalDesc();
    }

    default List<PadraoComportamentoUsuario> findByRiskProfileStatusIn(List<PadraoComportamentoUsuario.StatusPerfilRisco> statuses) {
        return findByStatusPerfilRiscoIn(statuses);
    }
} 