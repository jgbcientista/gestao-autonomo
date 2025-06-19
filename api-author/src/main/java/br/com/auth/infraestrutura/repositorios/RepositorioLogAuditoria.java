package br.com.auth.infraestrutura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.Usuario;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepositorioLogAuditoria extends JpaRepository<LogAuditoria, Long> {

    List<LogAuditoria> findByUsuario(Usuario usuario);

    List<LogAuditoria> findByTipoEvento(String tipoEvento);

    List<LogAuditoria> findByEnderecoIp(String enderecoIp);

    List<LogAuditoria> findBySucesso(Boolean sucesso);

    @Query("SELECT l FROM LogAuditoria l WHERE l.criadoEm BETWEEN :inicio AND :fim")
    List<LogAuditoria> findByCriadoEmBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT l FROM LogAuditoria l WHERE l.usuario = :usuario AND l.criadoEm BETWEEN :inicio AND :fim")
    List<LogAuditoria> findByUsuarioAndCriadoEmBetween(@Param("usuario") Usuario usuario, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT l FROM LogAuditoria l WHERE l.tipoEvento = :tipo AND l.criadoEm BETWEEN :inicio AND :fim")
    List<LogAuditoria> findByTipoEventoAndCriadoEmBetween(@Param("tipo") String tipoEvento, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT l FROM LogAuditoria l WHERE l.enderecoIp = :ip AND l.criadoEm BETWEEN :inicio AND :fim")
    List<LogAuditoria> findByEnderecoIpAndCriadoEmBetween(@Param("ip") String enderecoIp, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(l) FROM LogAuditoria l WHERE l.usuario = :usuario AND l.sucesso = false")
    Long countByUsuarioAndSucessoFalse(@Param("usuario") Usuario usuario);

    @Query("SELECT COUNT(l) FROM LogAuditoria l WHERE l.enderecoIp = :ip AND l.sucesso = false")
    Long countByEnderecoIpAndSucessoFalse(@Param("ip") String enderecoIp);

    @Query("SELECT l FROM LogAuditoria l WHERE l.localizacao = :localizacao")
    List<LogAuditoria> findByLocalizacao(@Param("localizacao") String localizacao);

    @Query("SELECT DISTINCT l.enderecoIp FROM LogAuditoria l WHERE l.usuario = :usuario")
    List<String> findDistinctEnderecoIpByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT DISTINCT l.localizacao FROM LogAuditoria l WHERE l.usuario = :usuario")
    List<String> findDistinctLocalizacaoByUsuario(@Param("usuario") Usuario usuario);

    // Métodos auxiliares para compatibilidade com código existente
    default List<LogAuditoria> findByUser(Usuario usuario) {
        return findByUsuario(usuario);
    }

    default List<LogAuditoria> findByEventType(String tipoEvento) {
        return findByTipoEvento(tipoEvento);
    }

    default List<LogAuditoria> findByIpAddress(String enderecoIp) {
        return findByEnderecoIp(enderecoIp);
    }

    default List<LogAuditoria> findBySuccess(Boolean sucesso) {
        return findBySucesso(sucesso);
    }

    default List<LogAuditoria> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim) {
        return findByCriadoEmBetween(inicio, fim);
    }

    default List<LogAuditoria> findByUserAndCreatedAtBetween(Usuario usuario, LocalDateTime inicio, LocalDateTime fim) {
        return findByUsuarioAndCriadoEmBetween(usuario, inicio, fim);
    }

    default List<LogAuditoria> findByEventTypeAndCreatedAtBetween(String tipoEvento, LocalDateTime inicio, LocalDateTime fim) {
        return findByTipoEventoAndCriadoEmBetween(tipoEvento, inicio, fim);
    }

    default List<LogAuditoria> findByIpAddressAndCreatedAtBetween(String enderecoIp, LocalDateTime inicio, LocalDateTime fim) {
        return findByEnderecoIpAndCriadoEmBetween(enderecoIp, inicio, fim);
    }

    default Long countByUserAndSuccessFalse(Usuario usuario) {
        return countByUsuarioAndSucessoFalse(usuario);
    }

    default Long countByIpAddressAndSuccessFalse(String enderecoIp) {
        return countByEnderecoIpAndSucessoFalse(enderecoIp);
    }

    default List<LogAuditoria> findByLocation(String localizacao) {
        return findByLocalizacao(localizacao);
    }

    default List<String> findDistinctIpAddressByUser(Usuario usuario) {
        return findDistinctEnderecoIpByUsuario(usuario);
    }

    default List<String> findDistinctLocationByUser(Usuario usuario) {
        return findDistinctLocalizacaoByUsuario(usuario);
    }
} 