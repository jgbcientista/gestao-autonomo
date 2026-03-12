package br.com.auth.infraestrutura.repositorios;

import br.com.auth.dominio.entidades.ScoreConfianca;
import br.com.auth.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioScoreConfianca extends JpaRepository<ScoreConfianca, Long> {

    /**
     * Busca o score de confiança atual de um usuário
     */
    Optional<ScoreConfianca> findFirstByUsuarioOrderByIdDesc(Usuario usuario);

    /**
     * Busca o score de confiança por ID do usuário
     */
    @Query("SELECT s FROM ScoreConfianca s WHERE s.usuario.id = :usuarioId")
    Optional<ScoreConfianca> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca usuários com score de confiança baixo
     */
    @Query("SELECT s FROM ScoreConfianca s WHERE s.scoreAtual < :threshold")
    List<ScoreConfianca> findByScoreAbaixoDe(@Param("threshold") Double threshold);

    /**
     * Busca usuários em observação
     */
    @Query("SELECT s FROM ScoreConfianca s WHERE s.emObservacao = true AND s.observacaoAte > :agora")
    List<ScoreConfianca> findUsuariosEmObservacao(@Param("agora") LocalDateTime agora);

    /**
     * Busca usuários por nível de confiança
     */
    @Query("SELECT s FROM ScoreConfianca s WHERE s.nivelConfianca = :nivel")
    List<ScoreConfianca> findByNivelConfianca(@Param("nivel") ScoreConfianca.NivelConfianca nivel);

    /**
     * Busca usuários que não foram atualizados há mais de X dias
     */
    @Query("SELECT s FROM ScoreConfianca s WHERE s.ultimaAtualizacao < :dataLimite")
    List<ScoreConfianca> findScoresDesatualizados(@Param("dataLimite") LocalDateTime dataLimite);

    /**
     * Estatísticas de scores por nível
     */
    @Query("SELECT s.nivelConfianca, COUNT(s) FROM ScoreConfianca s GROUP BY s.nivelConfianca")
    List<Object[]> obterEstatisticasPorNivel();

    /**
     * Score médio geral do sistema
     */
    @Query("SELECT AVG(s.scoreAtual) FROM ScoreConfianca s WHERE s.scoreAtual IS NOT NULL")
    Double obterScoreMedioGeral();

    /**
     * Usuários com mais bloqueios
     */
    @Query("SELECT s FROM ScoreConfianca s WHERE s.totalBloqueios > :limite ORDER BY s.totalBloqueios DESC")
    List<ScoreConfianca> findUsuariosComMaisBloqueios(@Param("limite") Integer limite);

    /**
     * Usuários confiáveis (score alto e não em observação)
     */
    @Query("SELECT s FROM ScoreConfianca s WHERE s.scoreAtual >= 0.7 AND (s.emObservacao = false OR s.emObservacao IS NULL)")
    List<ScoreConfianca> findUsuariosConfiaveis();

    /**
     * Verifica se existe score para o usuário
     */
    boolean existsByUsuario(Usuario usuario);

    /**
     * Remove observação de usuários com período expirado
     */
    @Query("UPDATE ScoreConfianca s SET s.emObservacao = false, s.observacaoAte = null WHERE s.observacaoAte < :agora")
    int removerObservacaoExpirada(@Param("agora") LocalDateTime agora);
} 