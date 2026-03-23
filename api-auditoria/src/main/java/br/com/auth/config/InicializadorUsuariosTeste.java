package br.com.auth.config;

import br.com.auth.dominio.entidades.ScoreConfianca;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioScoreConfianca;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class InicializadorUsuariosTeste implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioScoreConfianca repositorioScoreConfianca;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Garantir score bom e MFA desabilitado para o usuario principal
        repositorioUsuario.findByEmail("joaoguedesdebrito@gmail.com").ifPresent(usuario -> {
            // Desabilitar MFA
            usuario.setAutenticacaoDoisFatoresHabilitada(false);
            usuario.setSegredoDoisFatores(null);
            usuario.setContaBloqueada(false);
            usuario.setTentativasLoginFalhadas(0);
            repositorioUsuario.save(usuario);

            // Resetar trust score para valor alto
            ScoreConfianca score = repositorioScoreConfianca.findByUsuarioId(usuario.getId())
                    .orElse(ScoreConfianca.builder()
                            .usuario(usuario)
                            .build());
            score.setScoreAtual(0.85);
            score.setFatorAjuste(0.0);
            score.setEmObservacao(false);
            score.setObservacaoAte(null);
            score.setTotalLoginsSuspeitos(0);
            score.setMotivoAlteracao("Score resetado - inicializacao do sistema");
            repositorioScoreConfianca.save(score);

            log.info("Usuario principal resetado: MFA=false, TrustScore=0.85, Bloqueado=false");
        });

        // 2. Criar 5 usuarios de teste
        criarUsuarioSeNaoExiste("Maria Silva", "maria.silva@teste.com", "123456", "USER");
        criarUsuarioSeNaoExiste("Carlos Santos", "carlos.santos@teste.com", "123456", "USER");
        criarUsuarioSeNaoExiste("Ana Oliveira", "ana.oliveira@teste.com", "123456", "USER");
        criarUsuarioSeNaoExiste("Pedro Costa", "pedro.costa@teste.com", "123456", "USER");
        criarUsuarioSeNaoExiste("Juliana Ferreira", "juliana.ferreira@teste.com", "123456", "ADMIN");
    }

    private void criarUsuarioSeNaoExiste(String nome, String email, String senha, String role) {
        if (repositorioUsuario.existsByEmail(email)) {
            log.info("Usuario de teste ja existe: {}", email);
            return;
        }

        Set<String> perfis = new HashSet<>();
        perfis.add(role);
        if (!role.equals("USER")) {
            perfis.add("USER");
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .perfis(perfis)
                .tentativasLoginFalhadas(0)
                .contaBloqueada(false)
                .autenticacaoDoisFatoresHabilitada(false)
                .build();

        repositorioUsuario.save(usuario);

        // Criar score de confianca inicial
        ScoreConfianca score = ScoreConfianca.builder()
                .usuario(usuario)
                .scoreAtual(0.80)
                .fatorAjuste(0.0)
                .emObservacao(false)
                .totalLoginsSuspeitos(0)
                .motivoAlteracao("Score inicial - novo usuario")
                .build();
        repositorioScoreConfianca.save(score);

        log.info("Usuario de teste criado: {} ({}) - Role: {}", nome, email, role);
    }
}
