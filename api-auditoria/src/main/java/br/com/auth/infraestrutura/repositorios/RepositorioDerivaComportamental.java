package br.com.auth.infraestrutura.repositorios;

import br.com.auth.dominio.entidades.DerivaComportamental;
import br.com.auth.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioDerivaComportamental extends JpaRepository<DerivaComportamental, Long> {

    List<DerivaComportamental> findByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    Optional<DerivaComportamental> findTopByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    List<DerivaComportamental> findByUsuarioAndCriadoEmBetween(Usuario usuario, LocalDateTime inicio, LocalDateTime fim);
}
