package br.com.auth.infraestrutura.repositorios;

import br.com.auth.dominio.entidades.PadraoDigitacao;
import br.com.auth.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPadraoDigitacao extends JpaRepository<PadraoDigitacao, Long> {

    Optional<PadraoDigitacao> findTopByUsuarioAndEhBaselineTrueOrderByCriadoEmDesc(Usuario usuario);

    List<PadraoDigitacao> findByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    Optional<PadraoDigitacao> findTopByUsuarioOrderByCriadoEmDesc(Usuario usuario);

    long countByUsuario(Usuario usuario);
}
