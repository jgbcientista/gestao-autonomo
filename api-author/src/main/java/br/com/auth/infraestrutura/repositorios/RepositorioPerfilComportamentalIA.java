package br.com.auth.infraestrutura.repositorios;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPerfilComportamentalIA extends JpaRepository<PerfilComportamentalIA, Long> {

    List<PerfilComportamentalIA> findByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    Optional<PerfilComportamentalIA> findTopByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    List<PerfilComportamentalIA> findByClassificacaoAcesso(PerfilComportamentalIA.ClassificacaoAcesso classificacao);

    @Query("SELECT p FROM PerfilComportamentalIA p WHERE p.usuario = :usuario AND p.criadoEm >= :dataInicio")
    List<PerfilComportamentalIA> findByUsuarioAndDataMaiorQue(@Param("usuario") Usuario usuario, 
                                                               @Param("dataInicio") LocalDateTime dataInicio);

    @Query("SELECT p FROM PerfilComportamentalIA p WHERE p.scoreAnomalia > :threshold ORDER BY p.scoreAnomalia DESC")
    List<PerfilComportamentalIA> findAnomalias(@Param("threshold") Double threshold);

    @Query("SELECT AVG(p.scoreAnomalia) FROM PerfilComportamentalIA p WHERE p.usuario = :usuario")
    Optional<Double> findMediaScoreAnomaliaByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT p FROM PerfilComportamentalIA p WHERE p.algoritmoUtilizado = :algoritmo AND p.criadoEm >= :dataInicio")
    List<PerfilComportamentalIA> findByAlgoritmoAndDataMaiorQue(@Param("algoritmo") PerfilComportamentalIA.AlgoritmoIA algoritmo,
                                                                @Param("dataInicio") LocalDateTime dataInicio);

    @Query("SELECT COUNT(p) FROM PerfilComportamentalIA p WHERE p.usuario = :usuario AND p.classificacaoAcesso = :classificacao")
    Long countByUsuarioAndClassificacao(@Param("usuario") Usuario usuario, 
                                        @Param("classificacao") PerfilComportamentalIA.ClassificacaoAcesso classificacao);

    @Query("SELECT p FROM PerfilComportamentalIA p WHERE p.enderecoIp = :ip AND p.criadoEm >= :dataInicio")
    List<PerfilComportamentalIA> findByIpAndDataMaiorQue(@Param("ip") String ip, 
                                                         @Param("dataInicio") LocalDateTime dataInicio);
} 