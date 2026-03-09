package br.com.auth.service;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosFeatures;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class ServicoIsolationForest {

    private List<IsolationTree> floresta;
    private static final int NUMERO_ARVORES = 100;
    private static final int TAMANHO_AMOSTRA = 256;
    private boolean modeloTreinado = false;

    public Double calcularScore(DadosFeatures features) {
        if (!modeloTreinado) {
            log.warn("Modelo Isolation Forest não foi treinado. Usando score simulado.");
            return simularScore(features);
        }

        double[] featureArray = converterFeaturesParaArray(features);
        double somaScores = 0.0;

        for (IsolationTree arvore : floresta) {
            somaScores += arvore.calcularScore(featureArray);
        }

        double scoreMedia = somaScores / floresta.size();
        
        // Normalizar score para [0,1]
        return Math.max(0.0, Math.min(1.0, scoreMedia));
    }

    public void treinarModelo() {
        log.info("Iniciando treinamento do modelo Isolation Forest");
        
        try {
            // Simular dados de treinamento (em produção viria do banco)
            List<double[]> dadosTreinamento = gerarDadosTreinamentoSimulados();
            
            floresta = new ArrayList<>();
            
            for (int i = 0; i < NUMERO_ARVORES; i++) {
                // Amostragem aleatória dos dados
                List<double[]> amostra = selecionarAmostraAleatoria(dadosTreinamento, TAMANHO_AMOSTRA);
                
                // Criar e treinar árvore de isolamento
                IsolationTree arvore = new IsolationTree();
                arvore.treinar(amostra);
                
                floresta.add(arvore);
            }
            
            modeloTreinado = true;
            log.info("Modelo Isolation Forest treinado com sucesso. {} árvores criadas.", NUMERO_ARVORES);
            
        } catch (Exception e) {
            log.error("Erro no treinamento do modelo Isolation Forest", e);
            throw new RuntimeException("Erro no treinamento do Isolation Forest", e);
        }
    }

    public void ajustarComFeedback(PerfilComportamentalIA perfil, boolean acessoLegitimo) {
        log.debug("Ajustando modelo Isolation Forest com feedback para perfil: {}", perfil.getId());
        
        // Em uma implementação real, seria usado aprendizado online
        // Por enquanto, apenas log do feedback
        if (acessoLegitimo && perfil.getIsolationForestScore() > 0.7) {
            log.info("Falso positivo detectado no Isolation Forest - Score: {}", 
                perfil.getIsolationForestScore());
        } else if (!acessoLegitimo && perfil.getIsolationForestScore() < 0.3) {
            log.info("Falso negativo detectado no Isolation Forest - Score: {}", 
                perfil.getIsolationForestScore());
        }
    }

    private Double simularScore(DadosFeatures features) {
        // Score simulado baseado em algumas heurísticas
        double score = 0.0;
        
        // Penalizar horários incomuns
        if (features.horaAcesso() < 6 || features.horaAcesso() > 23) {
            score += 0.3;
        }
        
        // Penalizar fins de semana
        if (features.diaSemana() == 6 || features.diaSemana() == 7) {
            score += 0.2;
        }
        
        // Penalizar baixa frequência de acesso
        if (features.frequenciaAcessoSemanal() < 2.0) {
            score += 0.3;
        }
        
        // Penalizar novos IPs/dispositivos
        if (!features.ipJaUtilizado()) {
            score += 0.2;
        }
        
        if (!features.dispositivoJaUtilizado()) {
            score += 0.2;
        }
        
        // Adicionar ruído aleatório
        score += ThreadLocalRandom.current().nextGaussian() * 0.1;
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double[] converterFeaturesParaArray(DadosFeatures features) {
        return new double[] {
            features.horaAcesso(),
            features.diaSemana(),
            features.frequenciaAcessoSemanal(),
            features.ipJaUtilizado() ? 1.0 : 0.0,
            features.dispositivoJaUtilizado() ? 1.0 : 0.0,
            features.localizacaoJaUtilizada() ? 1.0 : 0.0,
            features.distanciaLocalizacaoHabitualKm(),
            features.diferencaHorarioHabitualHoras(),
            features.tempoDesdeUltimoAcessoHoras(),
            features.mediaSessoesDiarias(),
            features.desvioPadraoHorarios(),
            features.totalIpsDistintos().doubleValue(),
            features.totalDispositivosDistintos().doubleValue(),
            features.padroesNavegacaoScore()
        };
    }

    private List<double[]> gerarDadosTreinamentoSimulados() {
        List<double[]> dados = new ArrayList<>();
        Random random = new Random();
        
        // Gerar 1000 amostras simuladas
        for (int i = 0; i < 1000; i++) {
            double[] amostra = new double[14];
            
            // Simular dados normais (80% dos casos)
            if (random.nextDouble() < 0.8) {
                amostra[0] = 8 + random.nextGaussian() * 3; // Hora normal de trabalho
                amostra[1] = 1 + random.nextInt(5); // Dias úteis
                amostra[2] = 5 + random.nextGaussian() * 2; // Frequência normal
                amostra[3] = 1.0; // IP conhecido
                amostra[4] = 1.0; // Dispositivo conhecido
                amostra[5] = 1.0; // Localização conhecida
                amostra[6] = random.nextGaussian() * 10; // Distância pequena
                amostra[7] = random.nextGaussian() * 2; // Diferença horária pequena
                amostra[8] = 1 + random.nextGaussian() * 8; // Tempo desde último acesso
                amostra[9] = 3 + random.nextGaussian() * 1; // Sessões por dia
                amostra[10] = 2 + random.nextGaussian() * 1; // Desvio padrão horários
                amostra[11] = 1 + random.nextInt(3); // Poucos IPs
                amostra[12] = 1 + random.nextInt(2); // Poucos dispositivos
                amostra[13] = 0.5 + random.nextGaussian() * 0.2; // Padrão navegação
            } else {
                // Simular dados anômalos (20% dos casos)
                amostra[0] = random.nextInt(24); // Qualquer hora
                amostra[1] = random.nextInt(7) + 1; // Qualquer dia
                amostra[2] = random.nextDouble() * 2; // Baixa frequência
                amostra[3] = random.nextDouble() < 0.3 ? 1.0 : 0.0; // IP possivelmente novo
                amostra[4] = random.nextDouble() < 0.3 ? 1.0 : 0.0; // Dispositivo possivelmente novo
                amostra[5] = random.nextDouble() < 0.3 ? 1.0 : 0.0; // Localização possivelmente nova
                amostra[6] = random.nextDouble() * 1000; // Distância grande
                amostra[7] = random.nextDouble() * 12; // Grande diferença horária
                amostra[8] = random.nextDouble() * 168; // Muito tempo desde último acesso
                amostra[9] = random.nextDouble() * 2; // Poucas sessões
                amostra[10] = random.nextDouble() * 12; // Alto desvio padrão
                amostra[11] = random.nextInt(10) + 1; // Muitos IPs
                amostra[12] = random.nextInt(5) + 1; // Muitos dispositivos
                amostra[13] = random.nextDouble(); // Padrão navegação aleatório
            }
            
            dados.add(amostra);
        }
        
        return dados;
    }

    private List<double[]> selecionarAmostraAleatoria(List<double[]> dados, int tamanho) {
        List<double[]> amostra = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < Math.min(tamanho, dados.size()); i++) {
            int indice = random.nextInt(dados.size());
            amostra.add(dados.get(indice));
        }
        
        return amostra;
    }

    // Classe interna para representar uma árvore de isolamento
    private static class IsolationTree {
        private No raiz;
        private int alturaMaxima;

        public void treinar(List<double[]> dados) {
            alturaMaxima = (int) Math.ceil(Math.log(dados.size()) / Math.log(2));
            raiz = construirArvore(dados, 0);
        }

        public double calcularScore(double[] amostra) {
            if (raiz == null) return 0.5;
            
            int profundidade = calcularProfundidade(amostra, raiz, 0);
            
            // Normalizar baseado na altura esperada
            double alturaEsperada = 2.0 * (Math.log(256) + 0.5772156649) - (2.0 * 255.0 / 256.0);
            return Math.pow(2, -profundidade / alturaEsperada);
        }

        private No construirArvore(List<double[]> dados, int profundidade) {
            if (dados.size() <= 1 || profundidade >= alturaMaxima) {
                return new No(dados.size());
            }

            Random random = new Random();
            int atributo = random.nextInt(dados.get(0).length);
            
            double min = dados.stream().mapToDouble(d -> d[atributo]).min().orElse(0.0);
            double max = dados.stream().mapToDouble(d -> d[atributo]).max().orElse(1.0);
            
            if (min >= max) {
                return new No(dados.size());
            }
            
            double divisor = min + random.nextDouble() * (max - min);
            
            List<double[]> esquerda = new ArrayList<>();
            List<double[]> direita = new ArrayList<>();
            
            for (double[] amostra : dados) {
                if (amostra[atributo] < divisor) {
                    esquerda.add(amostra);
                } else {
                    direita.add(amostra);
                }
            }
            
            No no = new No(atributo, divisor);
            no.esquerda = construirArvore(esquerda, profundidade + 1);
            no.direita = construirArvore(direita, profundidade + 1);
            
            return no;
        }

        private int calcularProfundidade(double[] amostra, No no, int profundidade) {
            if (no.ehFolha()) {
                return profundidade;
            }
            
            if (amostra[no.atributo] < no.divisor) {
                return calcularProfundidade(amostra, no.esquerda, profundidade + 1);
            } else {
                return calcularProfundidade(amostra, no.direita, profundidade + 1);
            }
        }
    }

    // Classe interna para representar um nó da árvore
    private static class No {
        int atributo;
        double divisor;
        int tamanho;
        No esquerda;
        No direita;

        // Construtor para nó interno
        public No(int atributo, double divisor) {
            this.atributo = atributo;
            this.divisor = divisor;
        }

        // Construtor para folha
        public No(int tamanho) {
            this.tamanho = tamanho;
            this.atributo = -1;
        }

        public boolean ehFolha() {
            return atributo == -1;
        }
    }
} 