package br.com.auth.config;

import br.com.auth.service.ServicoIsolationForest;
import br.com.auth.service.ServicoRandomForest;
import br.com.auth.service.ServicoDeepLearning;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializa e treina os modelos de IA na inicialização da aplicação.
 * Os modelos são treinados com dados do banco (se disponíveis) ou dados simulados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InicializadorModelosIA implements ApplicationRunner {

    private final ServicoIsolationForest servicoIsolationForest;
    private final ServicoRandomForest servicoRandomForest;
    private final ServicoDeepLearning servicoDeepLearning;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Iniciando treinamento dos modelos de IA...");
        long inicio = System.currentTimeMillis();

        try {
            servicoIsolationForest.treinarModelo();
            log.info("Isolation Forest treinado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao treinar Isolation Forest: {}", e.getMessage());
        }

        try {
            servicoRandomForest.treinarModelo();
            log.info("Random Forest treinado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao treinar Random Forest: {}", e.getMessage());
        }

        try {
            servicoDeepLearning.treinarModelo();
            log.info("Deep Learning treinado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao treinar Deep Learning: {}", e.getMessage());
        }

        long duracao = System.currentTimeMillis() - inicio;
        log.info("Treinamento dos modelos de IA concluído em {}ms", duracao);
    }
}
