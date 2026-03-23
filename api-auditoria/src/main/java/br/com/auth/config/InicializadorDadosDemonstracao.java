package br.com.auth.config;

import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import br.com.auth.infraestrutura.repositorios.RepositorioPerfilComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioScoreConfianca;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
// @Component -- Desativado: dados de demonstração removidos
@Order(3)
@RequiredArgsConstructor
public class InicializadorDadosDemonstracao implements CommandLineRunner {

    private final RepositorioLogAuditoria repositorioLogAuditoria;
    private final RepositorioPerfilComportamentalIA repositorioPerfil;
    private final RepositorioScoreConfianca repositorioScoreConfianca;

    @Override
    public void run(String... args) {
        log.info("Limpando dados antigos de demonstração...");

        long logsRemovidos = repositorioLogAuditoria.count();
        repositorioLogAuditoria.deleteAll();

        long perfisRemovidos = repositorioPerfil.count();
        repositorioPerfil.deleteAll();

        long scoresRemovidos = repositorioScoreConfianca.count();
        repositorioScoreConfianca.deleteAll();

        log.info("Dados removidos: {} logs, {} perfis IA, {} scores", logsRemovidos, perfisRemovidos, scoresRemovidos);
    }
}
