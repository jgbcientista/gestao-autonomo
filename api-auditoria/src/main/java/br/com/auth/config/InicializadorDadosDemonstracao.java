package br.com.auth.config;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class InicializadorDadosDemonstracao implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioLogAuditoria repositorioLogAuditoria;

    @Override
    public void run(String... args) {
        String email = "joaoguedesdebrito@gmail.com";

        Usuario usuario = repositorioUsuario.findByEmail(email).orElse(null);
        if (usuario == null) {
            log.info("Usuário padrão não encontrado, pulando dados de demonstração");
            return;
        }

        List<LogAuditoria> logsExistentes = repositorioLogAuditoria.findByUsuario(usuario);
        if (!logsExistentes.isEmpty()) {
            log.info("Dados de demonstração já existem ({} registros)", logsExistentes.size());
            return;
        }

        log.info("Criando dados de demonstração de acessos para: {}", email);

        LocalDateTime agora = LocalDateTime.now();

        Object[][] acessos = {
            {"187.45.123.10", "São Paulo, BR", "Chrome 120 / Windows 11", true, 0},
            {"187.45.123.10", "São Paulo, BR", "Chrome 120 / Windows 11", true, 1},
            {"201.17.89.45", "Rio de Janeiro, BR", "Firefox 121 / Ubuntu", true, 2},
            {"177.92.34.67", "Salvador, BR", "Chrome 120 / Android 14", true, 3},
            {"45.227.15.89", "Brasília, BR", "Safari 17 / macOS Sonoma", true, 5},
            {"200.198.45.12", "Belo Horizonte, BR", "Edge 120 / Windows 11", true, 6},
            {"104.28.210.55", "New York, US", "Chrome 120 / Windows 11", true, 8},
            {"52.14.88.201", "Columbus, US", "Chrome 120 / Windows 11", true, 9},
            {"35.180.12.99", "Paris, FR", "Firefox 121 / Windows 10", true, 11},
            {"18.192.45.67", "Frankfurt, DE", "Chrome 120 / Linux", true, 12},
            {"13.235.78.90", "Mumbai, IN", "Chrome 119 / Android 13", true, 14},
            {"54.250.12.34", "Tokyo, JP", "Safari 17 / iOS 17", true, 15},
            {"177.92.34.67", "Salvador, BR", "Chrome 120 / Android 14", true, 16},
            {"187.45.123.10", "São Paulo, BR", "Chrome 120 / Windows 11", true, 17},
            {"91.189.94.40", "London, GB", "Firefox 121 / Ubuntu", true, 19},
            {"203.0.113.50", "Sydney, AU", "Chrome 120 / macOS", true, 20},
            {"45.55.99.10", "Amsterdam, NL", "Edge 120 / Windows 11", true, 22},
            {"177.38.210.45", "Recife, BR", "Chrome 120 / Android 14", true, 23},
            {"200.147.35.12", "Curitiba, BR", "Chrome 120 / Windows 11", true, 24},
            {"187.45.123.10", "São Paulo, BR", "Chrome 120 / Windows 11", false, 25},
            {"104.28.210.55", "New York, US", "Chrome 120 / Windows 11", false, 26},
            {"187.45.123.10", "São Paulo, BR", "Chrome 120 / Windows 11", true, 27},
            {"170.82.45.90", "Porto Alegre, BR", "Firefox 121 / Linux", true, 28},
            {"45.227.15.89", "Brasília, BR", "Chrome 120 / Android 14", true, 29},
            {"52.78.12.45", "Seoul, KR", "Chrome 120 / Windows 11", true, 30},
            {"157.240.1.35", "Menlo Park, US", "Chrome 120 / macOS", true, 32},
            {"187.45.123.10", "São Paulo, BR", "Chrome 120 / Windows 11", true, 33},
            {"201.17.89.45", "Rio de Janeiro, BR", "Firefox 121 / Ubuntu", true, 34},
            {"177.92.34.67", "Salvador, BR", "Chrome 120 / Android 14", true, 35},
            {"35.180.12.99", "Paris, FR", "Firefox 121 / Windows 10", false, 36},
        };

        for (Object[] acesso : acessos) {
            LogAuditoria logEntry = LogAuditoria.builder()
                    .usuario(usuario)
                    .tipoEvento("LOGIN")
                    .enderecoIp((String) acesso[0])
                    .localizacao((String) acesso[1])
                    .agenteUsuario((String) acesso[2])
                    .sucesso((Boolean) acesso[3])
                    .dataHora(agora.minusDays((Integer) acesso[4]))
                    .criadoEm(agora.minusDays((Integer) acesso[4]))
                    .descricao((Boolean) acesso[3] ? "Login bem-sucedido" : "Tentativa de login falhada")
                    .motivoFalha((Boolean) acesso[3] ? null : "Credenciais inválidas")
                    .build();

            repositorioLogAuditoria.save(logEntry);
        }

        log.info("Criados {} registros de acesso de demonstração", acessos.length);
    }
}
