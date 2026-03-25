package br.com.auth.config;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class InicializadorGestor implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String emailGestor = "joaoguedesdebrito@gmail.com";

        if (repositorioUsuario.existsByEmail(emailGestor)) {
            log.info("Gestor {} já existe no sistema", emailGestor);
            // Garantir que o gestor tenha o perfil correto e status ATIVO
            repositorioUsuario.findByEmail(emailGestor).ifPresent(gestor -> {
                boolean atualizado = false;
                if (!gestor.getPerfis().contains("GESTOR")) {
                    gestor.getPerfis().add("GESTOR");
                    atualizado = true;
                }
                if (!gestor.getPerfis().contains("ADMIN")) {
                    gestor.getPerfis().add("ADMIN");
                    atualizado = true;
                }
                if (!"ATIVO".equals(gestor.getStatusConta())) {
                    gestor.setStatusConta("ATIVO");
                    atualizado = true;
                }
                if (atualizado) {
                    repositorioUsuario.save(gestor);
                    log.info("Perfis do gestor {} atualizados: {}", emailGestor, gestor.getPerfis());
                }
            });
            return;
        }

        Set<String> perfis = new HashSet<>();
        perfis.add("GESTOR");
        perfis.add("ADMIN");
        perfis.add("USER");

        Usuario gestor = Usuario.builder()
                .nome("Joao Guedes")
                .email(emailGestor)
                .senha(passwordEncoder.encode("123456"))
                .perfis(perfis)
                .statusConta("ATIVO")
                .tentativasLoginFalhadas(0)
                .contaBloqueada(false)
                .autenticacaoDoisFatoresHabilitada(false)
                .build();

        repositorioUsuario.save(gestor);
        log.info("Gestor {} criado com sucesso com perfis: {}", emailGestor, perfis);
    }
}
