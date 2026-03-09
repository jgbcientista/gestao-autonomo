package br.com.auth.infraestrutura.repositorios;

import br.com.auth.dominio.entidades.DadosTreinamentoIA;
import br.com.auth.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepositorioDadosTreinamentoIA extends JpaRepository<DadosTreinamentoIA, Long> {

    List<DadosTreinamentoIA> findByUsuario(Usuario usuario);

    List<DadosTreinamentoIA> findByLabelComportamento(DadosTreinamentoIA.LabelComportamento label);

    @Query("SELECT d FROM DadosTreinamentoIA d WHERE d.usadoParaTreinamento = false")
    List<DadosTreinamentoIA> findDadosNaoUtilizados();

    @Query("SELECT d FROM DadosTreinamentoIA d WHERE d.validadoPorEspecialista = true")
    List<DadosTreinamentoIA> findDadosValidados();

    @Query("SELECT d FROM DadosTreinamentoIA d WHERE d.criadoEm >= :dataInicio AND d.criadoEm <= :dataFim")
    List<DadosTreinamentoIA> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                           @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT COUNT(d) FROM DadosTreinamentoIA d WHERE d.labelComportamento = :label")
    Long countByLabel(@Param("label") DadosTreinamentoIA.LabelComportamento label);

    @Query("SELECT d FROM DadosTreinamentoIA d WHERE d.versaoDataset = :versao")
    List<DadosTreinamentoIA> findByVersaoDataset(@Param("versao") String versao);
} 