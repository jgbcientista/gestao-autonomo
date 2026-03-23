package br.com.auth.config;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class InicializadorUsuarioPadrao implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String email = "joaoguedesdebrito@gmail.com";

        if (repositorioUsuario.existsByEmail(email)) {
            log.info("Usuário padrão já existe: {}", email);
            return;
        }

        Set<String> perfis = new HashSet<>();
        perfis.add("ADMIN");
        perfis.add("USER");

        Usuario usuario = Usuario.builder()
                .nome("João Guedes de Brito")
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .perfis(perfis)
                .tentativasLoginFalhadas(0)
                .contaBloqueada(false)
                .autenticacaoDoisFatoresHabilitada(false)
                .build();

        repositorioUsuario.save(usuario);
        log.info("Usuário padrão criado com sucesso: {} (ADMIN)", email);
    }
}
