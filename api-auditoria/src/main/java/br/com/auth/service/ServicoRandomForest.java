package br.com.auth.service;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosFeatures;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class ServicoRandomForest {

    private List<ArvoreDecisao> floresta;
    private static final int NUMERO_ARVORES = 50;
    private static final double PROPORCAO_FEATURES = 0.6;
    private boolean modeloTreinado = false;

    public Double calcularScore(DadosFeatures features) {
        if (!modeloTreinado) {
            log.warn("Modelo Random Forest não foi treinado. Usando score simulado.");
            return simularScore(features);
        }

        double[] featureArray = converterFeaturesParaArray(features);
        double somaVotos = 0.0;

        for (ArvoreDecisao arvore : floresta) {
            somaVotos += arvore.classificar(featureArray);
        }

        // Retorna a proporção de votos para "anômalo"
        return somaVotos / floresta.size();
    }

    public void treinarModelo() {
        log.info("Iniciando treinamento do modelo Random Forest");
        
        try {
            // Simular dados de treinamento com labels
            List<DadoTreinamento> dadosTreinamento = gerarDadosTreinamentoComLabels();
            
            floresta = new ArrayList<>();
            
            for (int i = 0; i < NUMERO_ARVORES; i++) {
                // Bootstrap sampling
                List<DadoTreinamento> amostraBootstrap = criarAmostraBootstrap(dadosTreinamento);
                
                // Feature sampling
                List<Integer> featuresEscolhidas = selecionarFeaturesAleatorias(14);
                
                // Criar e treinar árvore
                ArvoreDecisao arvore = new ArvoreDecisao(featuresEscolhidas);
                arvore.treinar(amostraBootstrap);
                
                floresta.add(arvore);
            }
            
            modeloTreinado = true;
            log.info("Modelo Random Forest treinado com sucesso. {} árvores criadas.", NUMERO_ARVORES);
            
        } catch (Exception e) {
            log.error("Erro no treinamento do modelo Random Forest", e);
            throw new RuntimeException("Erro no treinamento do Random Forest", e);
        }
    }

    public void ajustarComFeedback(PerfilComportamentalIA perfil, boolean acessoLegitimo) {
        log.debug("Ajustando modelo Random Forest com feedback para perfil: {}", perfil.getId());
        
        // Log do feedback para análise
        if (acessoLegitimo && perfil.getRandomForestScore() > 0.7) {
            log.info("Falso positivo detectado no Random Forest - Score: {}", 
                perfil.getRandomForestScore());
        } else if (!acessoLegitimo && perfil.getRandomForestScore() < 0.3) {
            log.info("Falso negativo detectado no Random Forest - Score: {}", 
                perfil.getRandomForestScore());
        }
    }

    private Double simularScore(DadosFeatures features) {
        double score = 0.0;
        
        // Análise baseada em regras heurísticas
        
        // 1. Padrão temporal
        if (features.horaAcesso() < 6 || features.horaAcesso() > 22) {
            score += 0.25;
        }
        
        if (features.diaSemana() > 5) { // Final de semana
            score += 0.15;
        }
        
        // 2. Padrão de frequência
        if (features.frequenciaAcessoSemanal() < 1.0) {
            score += 0.3;
        } else if (features.frequenciaAcessoSemanal() > 15.0) {
            score += 0.2;
        }
        
        // 3. Novidade de contexto
        if (!features.ipJaUtilizado()) {
            score += 0.25;
        }
        
        if (!features.dispositivoJaUtilizado()) {
            score += 0.2;
        }
        
        if (!features.localizacaoJaUtilizada()) {
            score += 0.3;
        }
        
        // 4. Distância de padrões habituais
        if (features.distanciaLocalizacaoHabitualKm() > 100) {
            score += 0.2;
        }
        
        if (features.diferencaHorarioHabitualHoras() > 4) {
            score += 0.15;
        }
        
        // 5. Tempo desde último acesso
        if (features.tempoDesdeUltimoAcessoHoras() > 72) {
            score += 0.2;
        }
        
        // 6. Diversidade de dispositivos/IPs
        if (features.totalIpsDistintos() > 5) {
            score += 0.15;
        }
        
        if (features.totalDispositivosDistintos() > 3) {
            score += 0.1;
        }
        
        // Normalizar e adicionar ruído
        score = Math.min(1.0, score);
        score += ThreadLocalRandom.current().nextGaussian() * 0.05;
        
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

    private List<DadoTreinamento> gerarDadosTreinamentoComLabels() {
        List<DadoTreinamento> dados = new ArrayList<>();
        Random random = new Random();
        
        // Gerar 2000 amostras simuladas
        for (int i = 0; i < 2000; i++) {
            double[] features = new double[14];
            boolean ehAnomalo;
            
            // 70% dados normais, 30% anômalos
            if (random.nextDouble() < 0.7) {
                // Dados normais
                features[0] = 8 + random.nextGaussian() * 2; // Horário comercial
                features[1] = 1 + random.nextInt(5); // Dias úteis
                features[2] = 3 + random.nextGaussian() * 2; // Frequência normal
                features[3] = 1.0; // IP conhecido
                features[4] = 1.0; // Dispositivo conhecido
                features[5] = 1.0; // Localização conhecida
                features[6] = Math.abs(random.nextGaussian() * 20); // Distância pequena
                features[7] = Math.abs(random.nextGaussian() * 2); // Diferença horária pequena
                features[8] = 1 + Math.abs(random.nextGaussian() * 12); // Tempo razoável
                features[9] = 2 + Math.abs(random.nextGaussian() * 1); // Sessões normais
                features[10] = 1 + Math.abs(random.nextGaussian() * 1); // Desvio baixo
                features[11] = 1 + random.nextInt(3); // Poucos IPs
                features[12] = 1 + random.nextInt(2); // Poucos dispositivos
                features[13] = 0.3 + random.nextDouble() * 0.4; // Navegação consistente
                ehAnomalo = false;
            } else {
                // Dados anômalos
                features[0] = random.nextInt(24); // Qualquer horário
                features[1] = random.nextInt(7) + 1; // Qualquer dia
                features[2] = random.nextDouble() * 3; // Baixa frequência
                features[3] = random.nextDouble() < 0.4 ? 1.0 : 0.0; // IP possivelmente novo
                features[4] = random.nextDouble() < 0.4 ? 1.0 : 0.0; // Dispositivo possivelmente novo
                features[5] = random.nextDouble() < 0.3 ? 1.0 : 0.0; // Localização possivelmente nova
                features[6] = random.nextDouble() * 1000; // Distância variável
                features[7] = random.nextDouble() * 12; // Grande diferença horária
                features[8] = random.nextDouble() * 200; // Tempo variável
                features[9] = random.nextDouble() * 3; // Sessões variáveis
                features[10] = random.nextDouble() * 8; // Alto desvio
                features[11] = random.nextInt(15) + 1; // Muitos IPs
                features[12] = random.nextInt(8) + 1; // Muitos dispositivos
                features[13] = random.nextDouble(); // Navegação inconsistente
                ehAnomalo = true;
            }
            
            dados.add(new DadoTreinamento(features, ehAnomalo));
        }
        
        return dados;
    }

    private List<DadoTreinamento> criarAmostraBootstrap(List<DadoTreinamento> dados) {
        List<DadoTreinamento> amostra = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < dados.size(); i++) {
            int indice = random.nextInt(dados.size());
            amostra.add(dados.get(indice));
        }
        
        return amostra;
    }

    private List<Integer> selecionarFeaturesAleatorias(int totalFeatures) {
        int numFeatures = (int) Math.ceil(totalFeatures * PROPORCAO_FEATURES);
        List<Integer> todasFeatures = new ArrayList<>();
        for (int i = 0; i < totalFeatures; i++) {
            todasFeatures.add(i);
        }
        
        Collections.shuffle(todasFeatures);
        return todasFeatures.subList(0, numFeatures);
    }

    // Classe para dados de treinamento com label
    private static class DadoTreinamento {
        double[] features;
        boolean ehAnomalo;

        public DadoTreinamento(double[] features, boolean ehAnomalo) {
            this.features = features;
            this.ehAnomalo = ehAnomalo;
        }
    }

    // Implementação simplificada de árvore de decisão
    private static class ArvoreDecisao {
        private No raiz;
        private List<Integer> featuresDisponiveis;
        private static final int PROFUNDIDADE_MAXIMA = 10;
        private static final int MIN_AMOSTRAS_FOLHA = 5;

        public ArvoreDecisao(List<Integer> featuresDisponiveis) {
            this.featuresDisponiveis = featuresDisponiveis;
        }

        public void treinar(List<DadoTreinamento> dados) {
            raiz = construirArvore(dados, 0);
        }

        public double classificar(double[] features) {
            return classificarRecursivo(features, raiz);
        }

        private No construirArvore(List<DadoTreinamento> dados, int profundidade) {
            // Condições de parada
            if (dados.size() < MIN_AMOSTRAS_FOLHA || profundidade >= PROFUNDIDADE_MAXIMA || 
                ehPuro(dados)) {
                return new No(calcularProbabilidadeAnomalo(dados));
            }

            // Encontrar melhor divisão
            MelhorDivisao melhorDivisao = encontrarMelhorDivisao(dados);
            
            if (melhorDivisao == null) {
                return new No(calcularProbabilidadeAnomalo(dados));
            }

            // Dividir dados
            List<DadoTreinamento> esquerda = new ArrayList<>();
            List<DadoTreinamento> direita = new ArrayList<>();
            
            for (DadoTreinamento dado : dados) {
                if (dado.features[melhorDivisao.feature] <= melhorDivisao.threshold) {
                    esquerda.add(dado);
                } else {
                    direita.add(dado);
                }
            }

            // Criar nó
            No no = new No(melhorDivisao.feature, melhorDivisao.threshold);
            no.esquerda = construirArvore(esquerda, profundidade + 1);
            no.direita = construirArvore(direita, profundidade + 1);
            
            return no;
        }

        private MelhorDivisao encontrarMelhorDivisao(List<DadoTreinamento> dados) {
            double melhorGini = Double.MAX_VALUE;
            MelhorDivisao melhorDivisao = null;

            for (int feature : featuresDisponiveis) {
                Set<Double> valoresUnicos = new HashSet<>();
                for (DadoTreinamento dado : dados) {
                    valoresUnicos.add(dado.features[feature]);
                }

                for (double valor : valoresUnicos) {
                    double gini = calcularGiniDivisao(dados, feature, valor);
                    if (gini < melhorGini) {
                        melhorGini = gini;
                        melhorDivisao = new MelhorDivisao(feature, valor);
                    }
                }
            }

            return melhorDivisao;
        }

        private double calcularGiniDivisao(List<DadoTreinamento> dados, int feature, double threshold) {
            List<DadoTreinamento> esquerda = new ArrayList<>();
            List<DadoTreinamento> direita = new ArrayList<>();
            
            for (DadoTreinamento dado : dados) {
                if (dado.features[feature] <= threshold) {
                    esquerda.add(dado);
                } else {
                    direita.add(dado);
                }
            }

            if (esquerda.isEmpty() || direita.isEmpty()) {
                return Double.MAX_VALUE;
            }

            double giniEsquerda = calcularGini(esquerda);
            double giniDireita = calcularGini(direita);
            
            double pesoEsquerda = (double) esquerda.size() / dados.size();
            double pesoDireita = (double) direita.size() / dados.size();
            
            return pesoEsquerda * giniEsquerda + pesoDireita * giniDireita;
        }

        private double calcularGini(List<DadoTreinamento> dados) {
            if (dados.isEmpty()) return 0.0;
            
            long anomalos = dados.stream().mapToLong(d -> d.ehAnomalo ? 1 : 0).sum();
            double propAnomalo = (double) anomalos / dados.size();
            double propNormal = 1.0 - propAnomalo;
            
            return 1.0 - (propAnomalo * propAnomalo + propNormal * propNormal);
        }

        private boolean ehPuro(List<DadoTreinamento> dados) {
            if (dados.isEmpty()) return true;
            
            boolean primeiroLabel = dados.get(0).ehAnomalo;
            return dados.stream().allMatch(d -> d.ehAnomalo == primeiroLabel);
        }

        private double calcularProbabilidadeAnomalo(List<DadoTreinamento> dados) {
            if (dados.isEmpty()) return 0.0;
            
            long anomalos = dados.stream().mapToLong(d -> d.ehAnomalo ? 1 : 0).sum();
            return (double) anomalos / dados.size();
        }

        private double classificarRecursivo(double[] features, No no) {
            if (no.ehFolha()) {
                return no.probabilidadeAnomalo;
            }
            
            if (features[no.feature] <= no.threshold) {
                return classificarRecursivo(features, no.esquerda);
            } else {
                return classificarRecursivo(features, no.direita);
            }
        }
    }

    // Classe para representar um nó da árvore
    private static class No {
        int feature;
        double threshold;
        double probabilidadeAnomalo;
        No esquerda;
        No direita;

        // Construtor para nó interno
        public No(int feature, double threshold) {
            this.feature = feature;
            this.threshold = threshold;
        }

        // Construtor para folha
        public No(double probabilidadeAnomalo) {
            this.probabilidadeAnomalo = probabilidadeAnomalo;
            this.feature = -1;
        }

        public boolean ehFolha() {
            return feature == -1;
        }
    }

    // Classe para representar a melhor divisão
    private static class MelhorDivisao {
        int feature;
        double threshold;

        public MelhorDivisao(int feature, double threshold) {
            this.feature = feature;
            this.threshold = threshold;
        }
    }
}