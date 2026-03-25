package br.com.auth.infraestrutura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.auth.dominio.entidades.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    long countAllByUltimoLoginDataIsNotNull();
    List<Usuario> findByStatusConta(String statusConta);
} 