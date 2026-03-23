package br.com.auth.service;

import br.com.auth.dominio.entidades.ScoreConfianca;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA;
import br.com.auth.infraestrutura.repositorios.RepositorioScoreConfianca;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servico responsavel por simular cenarios de ataque para demonstracao
 * do sistema de score de confianca e decisoes adaptativas de autenticacao.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulacaoAtaqueService {

    private final ServicoScoreConfianca servicoScoreConfianca;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioScoreConfianca repositorioScoreConfianca;
    private final ServicoGeolocalizacao servicoGeolocalizacao;

    /**
     * Simula um ataque de forca bruta com 10 tentativas rapidas de login
     * a partir do mesmo IP.
     */
    public Map<String, Object> simularForcaBruta(String email) {
        log.info("Iniciando simulacao de forca bruta para: {}", email);
        long inicio = System.currentTimeMillis();

        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + email));

        ScoreConfianca score = servicoScoreConfianca.obterOuCriarScore(usuario);
        score.setScoreAtual(0.85);
        score.setTotalLoginsSuspeitos(0);
        repositorioScoreConfianca.save(score);

        String ip = "187.45.123.10";
        String localizacao = "Sao Paulo, Brasil";
        List<Map<String, Object>> etapas = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            score.setScoreAtual(Math.max(0.0, score.getScoreAtual() - 0.08));
            score.setTotalLoginsSuspeitos(score.getTotalLoginsSuspeitos() + 1);
            score.setMotivoAlteracao("Simulacao forca bruta - tentativa " + i);
            repositorioScoreConfianca.save(score);

            String decisao;
            if (score.getScoreAtual() < 0.2) {
                decisao = "BLOQUEAR";
            } else if (i >= 8) {
                decisao = "BLOQUEAR";
            } else if (score.getScoreAtual() < 0.5 || i >= 5) {
                decisao = "EXIGIR_MFA";
            } else if (score.getScoreAtual() < 0.7) {
                decisao = "EXIGIR_MFA";
            } else {
                decisao = "PERMITIR";
            }

            Map<String, Object> etapa = new HashMap<>();
            etapa.put("numero", i);
            etapa.put("descricao", "Tentativa de login #" + i + " - senha incorreta");
            etapa.put("scoreConfianca", Math.round(score.getScoreAtual() * 1000.0) / 1000.0);
            etapa.put("decisao", decisao);
            etapa.put("ip", ip);
            etapa.put("localizacao", localizacao);
            etapa.put("timestamp", LocalDateTime.now().plusSeconds(i * 2L)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            etapas.add(etapa);
        }

        long duracao = System.currentTimeMillis() - inicio;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("cenario", "Forca Bruta - 10 tentativas rapidas do mesmo IP");
        resultado.put("etapas", etapas);
        resultado.put("duracaoTotalMs", duracao);

        log.info("Simulacao de forca bruta concluida para: {}", email);
        return resultado;
    }

    /**
     * Simula um ataque de credential stuffing com logins de 8 IPs internacionais diferentes.
     */
    public Map<String, Object> simularCredentialStuffing(String email) {
        log.info("Iniciando simulacao de credential stuffing para: {}", email);
        long inicio = System.currentTimeMillis();

        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + email));

        ScoreConfianca score = servicoScoreConfianca.obterOuCriarScore(usuario);
        score.setScoreAtual(0.85);
        score.setTotalLoginsSuspeitos(0);
        repositorioScoreConfianca.save(score);

        String[][] ipsLocalizacoes = {
                {"45.33.32.156", "San Francisco, EUA"},
                {"103.21.244.0", "Sydney, Australia"},
                {"185.220.101.1", "Berlin, Alemanha"},
                {"91.108.56.100", "Moscow, Russia"},
                {"177.54.145.78", "Sao Paulo, Brasil"},
                {"41.215.241.50", "Nairobi, Quenia"},
                {"103.152.220.44", "Tokyo, Japao"},
                {"185.56.83.83", "Amsterdam, Holanda"}
        };

        List<Map<String, Object>> etapas = new ArrayList<>();

        for (int i = 0; i < ipsLocalizacoes.length; i++) {
            int numero = i + 1;
            score.setScoreAtual(Math.max(0.0, score.getScoreAtual() - 0.10));
            score.setTotalLoginsSuspeitos(score.getTotalLoginsSuspeitos() + 1);
            score.setMotivoAlteracao("Simulacao credential stuffing - IP " + ipsLocalizacoes[i][0]);
            repositorioScoreConfianca.save(score);

            String decisao;
            if (score.getScoreAtual() < 0.2) {
                decisao = "BLOQUEAR";
            } else if (score.getScoreAtual() < 0.5) {
                decisao = "EXIGIR_MFA";
            } else if (score.getScoreAtual() < 0.7) {
                decisao = "EXIGIR_MFA";
            } else {
                decisao = "PERMITIR";
            }

            Map<String, Object> etapa = new HashMap<>();
            etapa.put("numero", numero);
            etapa.put("descricao", "Login de IP internacional #" + numero + " (" + ipsLocalizacoes[i][1] + ")");
            etapa.put("scoreConfianca", Math.round(score.getScoreAtual() * 1000.0) / 1000.0);
            etapa.put("decisao", decisao);
            etapa.put("ip", ipsLocalizacoes[i][0]);
            etapa.put("localizacao", ipsLocalizacoes[i][1]);
            etapa.put("timestamp", LocalDateTime.now().plusSeconds(numero * 3L)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            etapas.add(etapa);
        }

        long duracao = System.currentTimeMillis() - inicio;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("cenario", "Credential Stuffing - Logins de 8 IPs internacionais diferentes");
        resultado.put("etapas", etapas);
        resultado.put("duracaoTotalMs", duracao);

        log.info("Simulacao de credential stuffing concluida para: {}", email);
        return resultado;
    }

    /**
     * Simula um cenario de viagem impossivel: login em Sao Paulo seguido de
     * login em Tokyo 5 minutos depois.
     */
    public Map<String, Object> simularViagemImpossivel(String email) {
        log.info("Iniciando simulacao de viagem impossivel para: {}", email);
        long inicio = System.currentTimeMillis();

        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + email));

        ScoreConfianca score = servicoScoreConfianca.obterOuCriarScore(usuario);
        score.setScoreAtual(0.85);
        score.setTotalLoginsSuspeitos(0);
        repositorioScoreConfianca.save(score);

        double latSP = -23.5505;
        double lonSP = -46.6333;
        double latTokyo = 35.6762;
        double lonTokyo = 139.6503;

        double distanciaKm = servicoGeolocalizacao.calcularDistancia(latSP, lonSP, latTokyo, lonTokyo);
        double tempoHoras = 5.0 / 60.0; // 5 minutos em horas
        double velocidadeKmH = distanciaKm / tempoHoras;

        List<Map<String, Object>> etapas = new ArrayList<>();

        // Etapa 1: Login normal de Sao Paulo
        score.setScoreAtual(score.getScoreAtual() - 0.02);
        score.setMotivoAlteracao("Simulacao viagem impossivel - login Sao Paulo");
        repositorioScoreConfianca.save(score);

        Map<String, Object> etapa1 = new HashMap<>();
        etapa1.put("numero", 1);
        etapa1.put("descricao", "Login normal de Sao Paulo, Brasil");
        etapa1.put("scoreConfianca", Math.round(score.getScoreAtual() * 1000.0) / 1000.0);
        etapa1.put("decisao", "PERMITIR");
        etapa1.put("ip", "177.54.145.78");
        etapa1.put("localizacao", "Sao Paulo, Brasil");
        etapa1.put("latitude", latSP);
        etapa1.put("longitude", lonSP);
        etapa1.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        etapas.add(etapa1);

        // Etapa 2: Login de Tokyo 5 minutos depois
        score.setScoreAtual(Math.max(0.0, score.getScoreAtual() - 0.60));
        score.setTotalLoginsSuspeitos(score.getTotalLoginsSuspeitos() + 1);
        score.setEmObservacao(true);
        score.setMotivoAlteracao("Viagem impossivel detectada - Sao Paulo para Tokyo em 5 minutos");
        repositorioScoreConfianca.save(score);

        Map<String, Object> etapa2 = new HashMap<>();
        etapa2.put("numero", 2);
        etapa2.put("descricao", "Login de Tokyo, Japao - 5 minutos apos login em Sao Paulo");
        etapa2.put("scoreConfianca", Math.round(score.getScoreAtual() * 1000.0) / 1000.0);
        etapa2.put("decisao", "BLOQUEAR");
        etapa2.put("ip", "103.152.220.44");
        etapa2.put("localizacao", "Tokyo, Japao");
        etapa2.put("latitude", latTokyo);
        etapa2.put("longitude", lonTokyo);
        etapa2.put("distanciaKm", Math.round(distanciaKm * 100.0) / 100.0);
        etapa2.put("velocidadeKmH", Math.round(velocidadeKmH * 100.0) / 100.0);
        etapa2.put("tempoEntreLoginsMinutos", 5);
        etapa2.put("timestamp", LocalDateTime.now().plusMinutes(5)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        etapas.add(etapa2);

        long duracao = System.currentTimeMillis() - inicio;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("cenario", "Viagem Impossivel - Sao Paulo para Tokyo em 5 minutos");
        resultado.put("etapas", etapas);
        resultado.put("distanciaKm", Math.round(distanciaKm * 100.0) / 100.0);
        resultado.put("velocidadeKmH", Math.round(velocidadeKmH * 100.0) / 100.0);
        resultado.put("duracaoTotalMs", duracao);

        log.info("Simulacao de viagem impossivel concluida para: {} (distancia: {} km, velocidade: {} km/h)",
                email, Math.round(distanciaKm), Math.round(velocidadeKmH));
        return resultado;
    }

    /**
     * Simula um cenario de sequestro de dispositivo com mudanca de dispositivo
     * e localizacao em 3 etapas.
     */
    public Map<String, Object> simularSequestroDispositivo(String email) {
        log.info("Iniciando simulacao de sequestro de dispositivo para: {}", email);
        long inicio = System.currentTimeMillis();

        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + email));

        ScoreConfianca score = servicoScoreConfianca.obterOuCriarScore(usuario);
        score.setScoreAtual(0.85);
        score.setTotalLoginsSuspeitos(0);
        repositorioScoreConfianca.save(score);

        List<Map<String, Object>> etapas = new ArrayList<>();

        // Etapa 1: Acesso normal do dispositivo conhecido
        score.setScoreAtual(score.getScoreAtual() - 0.02);
        score.setMotivoAlteracao("Simulacao sequestro dispositivo - acesso normal");
        repositorioScoreConfianca.save(score);

        Map<String, Object> etapa1 = new HashMap<>();
        etapa1.put("numero", 1);
        etapa1.put("descricao", "Acesso normal de dispositivo e localizacao conhecidos");
        etapa1.put("scoreConfianca", Math.round(score.getScoreAtual() * 1000.0) / 1000.0);
        etapa1.put("decisao", "PERMITIR");
        etapa1.put("ip", "177.54.145.78");
        etapa1.put("localizacao", "Sao Paulo, Brasil");
        etapa1.put("dispositivo", "Chrome 120 / Windows 11");
        etapa1.put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0");
        etapa1.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        etapas.add(etapa1);

        // Etapa 2: Acesso de dispositivo e localizacao completamente novos
        score.setScoreAtual(Math.max(0.0, score.getScoreAtual() - 0.40));
        score.setTotalLoginsSuspeitos(score.getTotalLoginsSuspeitos() + 1);
        score.setMotivoAlteracao("Sequestro de dispositivo - novo dispositivo + nova localizacao (Nigeria)");
        repositorioScoreConfianca.save(score);

        Map<String, Object> etapa2 = new HashMap<>();
        etapa2.put("numero", 2);
        etapa2.put("descricao", "Acesso de dispositivo desconhecido em localizacao suspeita (Nigeria)");
        etapa2.put("scoreConfianca", Math.round(score.getScoreAtual() * 1000.0) / 1000.0);
        etapa2.put("decisao", "EXIGIR_MFA");
        etapa2.put("ip", "41.58.100.23");
        etapa2.put("localizacao", "Lagos, Nigeria");
        etapa2.put("dispositivo", "Firefox 119 / Linux");
        etapa2.put("userAgent", "Mozilla/5.0 (X11; Linux x86_64; rv:119.0) Gecko/20100101 Firefox/119.0");
        etapa2.put("timestamp", LocalDateTime.now().plusMinutes(15)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        etapas.add(etapa2);

        // Etapa 3: Sistema bloqueia o acesso
        score.setScoreAtual(Math.max(0.0, score.getScoreAtual() - 0.30));
        score.setTotalLoginsSuspeitos(score.getTotalLoginsSuspeitos() + 1);
        score.setEmObservacao(true);
        score.setMotivoAlteracao("Sequestro de dispositivo confirmado - conta bloqueada");
        repositorioScoreConfianca.save(score);

        Map<String, Object> etapa3 = new HashMap<>();
        etapa3.put("numero", 3);
        etapa3.put("descricao", "Sistema detecta padrao de sequestro e bloqueia a conta");
        etapa3.put("scoreConfianca", Math.round(score.getScoreAtual() * 1000.0) / 1000.0);
        etapa3.put("decisao", "BLOQUEAR");
        etapa3.put("ip", "41.58.100.23");
        etapa3.put("localizacao", "Lagos, Nigeria");
        etapa3.put("dispositivo", "Firefox 119 / Linux");
        etapa3.put("userAgent", "Mozilla/5.0 (X11; Linux x86_64; rv:119.0) Gecko/20100101 Firefox/119.0");
        etapa3.put("timestamp", LocalDateTime.now().plusMinutes(16)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        etapas.add(etapa3);

        long duracao = System.currentTimeMillis() - inicio;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("cenario", "Sequestro de Dispositivo - Mudanca de dispositivo e localizacao");
        resultado.put("etapas", etapas);
        resultado.put("duracaoTotalMs", duracao);

        log.info("Simulacao de sequestro de dispositivo concluida para: {}", email);
        return resultado;
    }

    /**
     * Reseta o score de confianca do usuario para o valor inicial.
     */
    public Map<String, Object> resetarSimulacao(String email) {
        log.info("Resetando simulacao para: {}", email);

        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + email));

        ScoreConfianca score = servicoScoreConfianca.obterOuCriarScore(usuario);
        score.setScoreAtual(0.5);
        score.setFatorAjuste(0.0);
        score.setEmObservacao(false);
        score.setObservacaoAte(null);
        score.setMotivoAlteracao("Score resetado apos simulacao");
        repositorioScoreConfianca.save(score);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("cenario", "Reset de Simulacao");
        resultado.put("email", email);
        resultado.put("scoreAtual", score.getScoreAtual());
        resultado.put("fatorAjuste", score.getFatorAjuste());
        resultado.put("emObservacao", score.getEmObservacao());
        resultado.put("motivoAlteracao", score.getMotivoAlteracao());
        resultado.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        log.info("Simulacao resetada para: {}", email);
        return resultado;
    }
}
