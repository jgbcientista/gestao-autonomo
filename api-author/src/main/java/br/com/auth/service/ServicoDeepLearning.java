package br.com.auth.service;

import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosFeatures;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class ServicoDeepLearning {

    private RedeNeural redeNeural;
    private boolean modeloTreinado = false;
    
    // Arquitetura da rede neural
    private static final int INPUT_SIZE = 14;
    private static final int HIDDEN1_SIZE = 32;
    private static final int HIDDEN2_SIZE = 16;
    private static final int HIDDEN3_SIZE = 8;
    private static final int OUTPUT_SIZE = 1;
    
    // Parâmetros de treinamento
    private static final double LEARNING_RATE = 0.001;
    private static final int EPOCHS = 100;
    private static final int BATCH_SIZE = 32;

    public Double calcularScore(DadosFeatures features) {
        if (!modeloTreinado) {
            log.warn("Modelo Deep Learning não foi treinado. Usando score simulado.");
            return simularScore(features);
        }

        double[] input = converterFeaturesParaArray(features);
        double[] normalizedInput = normalizarInput(input);
        
        return redeNeural.predict(normalizedInput);
    }

    public void treinarModelo() {
        log.info("Iniciando treinamento do modelo Deep Learning");
        
        try {
            // Gerar dados de treinamento
            List<DadoTreinamento> dadosTreinamento = gerarDadosTreinamento();
            
            // Inicializar rede neural
            redeNeural = new RedeNeural(INPUT_SIZE, HIDDEN1_SIZE, HIDDEN2_SIZE, HIDDEN3_SIZE, OUTPUT_SIZE);
            
            // Treinar modelo
            treinarRedeNeural(dadosTreinamento);
            
            modeloTreinado = true;
            log.info("Modelo Deep Learning treinado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro no treinamento do modelo Deep Learning", e);
            throw new RuntimeException("Erro no treinamento do Deep Learning", e);
        }
    }

    public void ajustarComFeedback(PerfilComportamentalIA perfil, boolean acessoLegitimo) {
        log.debug("Ajustando modelo Deep Learning com feedback para perfil: {}", perfil.getId());
        
        // Log do feedback para análise
        if (acessoLegitimo && perfil.getDeepLearningScore() > 0.7) {
            log.info("Falso positivo detectado no Deep Learning - Score: {}", 
                perfil.getDeepLearningScore());
        } else if (!acessoLegitimo && perfil.getDeepLearningScore() < 0.3) {
            log.info("Falso negativo detectado no Deep Learning - Score: {}", 
                perfil.getDeepLearningScore());
        }
        
        // Em uma implementação real, seria usado fine-tuning online
    }

    private Double simularScore(DadosFeatures features) {
        // Score simulado usando lógica neural simplificada
        double[] input = converterFeaturesParaArray(features);
        double[] normalizedInput = normalizarInput(input);
        
        // Simular propagação através de camadas
        double[] hidden1 = aplicarCamadaSimulada(normalizedInput, HIDDEN1_SIZE, 0.1);
        double[] hidden2 = aplicarCamadaSimulada(hidden1, HIDDEN2_SIZE, 0.15);
        double[] hidden3 = aplicarCamadaSimulada(hidden2, HIDDEN3_SIZE, 0.2);
        double output = aplicarCamadaSimulada(hidden3, 1, 0.25)[0];
        
        // Aplicar função sigmoide para normalizar
        return sigmoid(output);
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

    private double[] normalizarInput(double[] input) {
        double[] normalized = new double[input.length];
        
        // Normalização Min-Max para cada feature
        double[] mins = {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double[] maxs = {23, 7, 50, 1, 1, 1, 1000, 12, 168, 20, 12, 20, 10, 1};
        
        for (int i = 0; i < input.length; i++) {
            normalized[i] = (input[i] - mins[i]) / (maxs[i] - mins[i]);
            normalized[i] = Math.max(0.0, Math.min(1.0, normalized[i])); // Clamp [0,1]
        }
        
        return normalized;
    }

    private List<DadoTreinamento> gerarDadosTreinamento() {
        List<DadoTreinamento> dados = new ArrayList<>();
        Random random = new Random();
        
        // Gerar 5000 amostras para treinamento
        for (int i = 0; i < 5000; i++) {
            double[] features = new double[14];
            double label;
            
            // 75% dados normais, 25% anômalos
            if (random.nextDouble() < 0.75) {
                // Comportamento normal
                features[0] = 8 + random.nextGaussian() * 2; // Horário comercial
                features[1] = 1 + random.nextInt(5); // Dias úteis
                features[2] = 5 + Math.abs(random.nextGaussian() * 3); // Frequência normal
                features[3] = 1.0; // IP conhecido
                features[4] = 1.0; // Dispositivo conhecido
                features[5] = 1.0; // Localização conhecida
                features[6] = Math.abs(random.nextGaussian() * 15); // Distância pequena
                features[7] = Math.abs(random.nextGaussian() * 1.5); // Diferença horária pequena
                features[8] = 2 + Math.abs(random.nextGaussian() * 10); // Tempo razoável
                features[9] = 3 + Math.abs(random.nextGaussian() * 1.5); // Sessões normais
                features[10] = 1.5 + Math.abs(random.nextGaussian() * 0.8); // Desvio baixo
                features[11] = 1 + random.nextInt(3); // Poucos IPs
                features[12] = 1 + random.nextInt(2); // Poucos dispositivos
                features[13] = 0.4 + random.nextDouble() * 0.3; // Navegação consistente
                label = 0.0; // Normal
            } else {
                // Comportamento anômalo
                features[0] = random.nextInt(24); // Qualquer horário
                features[1] = random.nextInt(7) + 1; // Qualquer dia
                features[2] = random.nextDouble() * 4; // Baixa frequência
                features[3] = random.nextDouble() < 0.3 ? 1.0 : 0.0; // IP possivelmente novo
                features[4] = random.nextDouble() < 0.3 ? 1.0 : 0.0; // Dispositivo possivelmente novo
                features[5] = random.nextDouble() < 0.2 ? 1.0 : 0.0; // Localização possivelmente nova
                features[6] = random.nextDouble() * 800; // Distância grande
                features[7] = random.nextDouble() * 10; // Grande diferença horária
                features[8] = random.nextDouble() * 150; // Tempo variável
                features[9] = random.nextDouble() * 2; // Poucas sessões
                features[10] = random.nextDouble() * 10; // Alto desvio
                features[11] = random.nextInt(12) + 1; // Muitos IPs
                features[12] = random.nextInt(6) + 1; // Muitos dispositivos
                features[13] = random.nextDouble(); // Navegação inconsistente
                label = 1.0; // Anômalo
            }
            
            dados.add(new DadoTreinamento(normalizarInput(features), label));
        }
        
        return dados;
    }

    private void treinarRedeNeural(List<DadoTreinamento> dados) {
        log.info("Treinando rede neural com {} amostras", dados.size());
        
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            Collections.shuffle(dados);
            double epochLoss = 0.0;
            
            // Mini-batch training
            for (int i = 0; i < dados.size(); i += BATCH_SIZE) {
                List<DadoTreinamento> batch = dados.subList(i, 
                    Math.min(i + BATCH_SIZE, dados.size()));
                
                double batchLoss = treinarBatch(batch);
                epochLoss += batchLoss;
            }
            
            if (epoch % 20 == 0) {
                log.debug("Epoch {}: Loss = {}", epoch, epochLoss / dados.size());
            }
        }
    }

    private double treinarBatch(List<DadoTreinamento> batch) {
        double totalLoss = 0.0;
        
        for (DadoTreinamento dado : batch) {
            double prediction = redeNeural.predict(dado.features);
            double loss = Math.pow(prediction - dado.label, 2); // MSE
            totalLoss += loss;
            
            // Backpropagation (simplificado)
            redeNeural.backpropagate(dado.features, dado.label, prediction);
        }
        
        return totalLoss / batch.size();
    }

    private double[] aplicarCamadaSimulada(double[] input, int outputSize, double bias) {
        double[] output = new double[outputSize];
        Random random = new Random(42); // Seed fixo para reprodutibilidade
        
        for (int i = 0; i < outputSize; i++) {
            double sum = bias;
            for (int j = 0; j < input.length; j++) {
                // Peso simulado baseado no índice
                double weight = (random.nextGaussian() * 0.1) + 
                               (input[j] * (j % 2 == 0 ? 0.3 : -0.2));
                sum += input[j] * weight;
            }
            output[i] = relu(sum);
        }
        
        return output;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double relu(double x) {
        return Math.max(0.0, x);
    }

    // Classe para dados de treinamento
    private static class DadoTreinamento {
        double[] features;
        double label;

        public DadoTreinamento(double[] features, double label) {
            this.features = features;
            this.label = label;
        }
    }

    // Implementação simplificada de rede neural
    private static class RedeNeural {
        private double[][][] weights; // [layer][neuron][input]
        private double[][] biases;    // [layer][neuron]
        private int[] layerSizes;

        public RedeNeural(int... sizes) {
            this.layerSizes = sizes;
            initializeWeights();
        }

        private void initializeWeights() {
            Random random = new Random();
            weights = new double[layerSizes.length - 1][][];
            biases = new double[layerSizes.length - 1][];

            for (int layer = 0; layer < layerSizes.length - 1; layer++) {
                int inputSize = layerSizes[layer];
                int outputSize = layerSizes[layer + 1];
                
                weights[layer] = new double[outputSize][inputSize];
                biases[layer] = new double[outputSize];
                
                // Xavier initialization
                double limit = Math.sqrt(6.0 / (inputSize + outputSize));
                
                for (int i = 0; i < outputSize; i++) {
                    biases[layer][i] = random.nextGaussian() * 0.1;
                    for (int j = 0; j < inputSize; j++) {
                        weights[layer][i][j] = (random.nextDouble() * 2 - 1) * limit;
                    }
                }
            }
        }

        public double predict(double[] input) {
            double[] activation = input.clone();
            
            for (int layer = 0; layer < weights.length; layer++) {
                activation = forwardLayer(activation, layer);
            }
            
            return activation[0]; // Assuming single output
        }

        private double[] forwardLayer(double[] input, int layerIndex) {
            int outputSize = layerSizes[layerIndex + 1];
            double[] output = new double[outputSize];
            
            for (int i = 0; i < outputSize; i++) {
                double sum = biases[layerIndex][i];
                for (int j = 0; j < input.length; j++) {
                    sum += input[j] * weights[layerIndex][i][j];
                }
                
                // Apply activation function
                if (layerIndex == weights.length - 1) {
                    output[i] = sigmoid(sum); // Sigmoid for output layer
                } else {
                    output[i] = relu(sum); // ReLU for hidden layers
                }
            }
            
            return output;
        }

        public void backpropagate(double[] input, double target, double prediction) {
            // Simplified backpropagation - just adjust output layer
            double error = target - prediction;
            double outputGradient = error * prediction * (1 - prediction); // Sigmoid derivative
            
            // Update output layer weights (simplified)
            int outputLayerIndex = weights.length - 1;
            for (int i = 0; i < weights[outputLayerIndex][0].length; i++) {
                weights[outputLayerIndex][0][i] += LEARNING_RATE * outputGradient * input[i];
            }
            biases[outputLayerIndex][0] += LEARNING_RATE * outputGradient;
        }

        private double sigmoid(double x) {
            return 1.0 / (1.0 + Math.exp(-Math.max(-500, Math.min(500, x))));
        }

        private double relu(double x) {
            return Math.max(0.0, x);
        }
    }
} 