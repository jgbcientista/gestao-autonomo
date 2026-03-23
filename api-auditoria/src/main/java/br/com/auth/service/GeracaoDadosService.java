package br.com.auth.service;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.PerfilComportamentalIA;
import br.com.auth.dominio.entidades.ScoreConfianca;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosContextoAcesso;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeracaoDadosService {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioLogAuditoria repositorioLogAuditoria;
    private final ServicoAnaliseComportamentalIA servicoAnaliseComportamentalIA;
    private final ServicoScoreConfianca servicoScoreConfianca;
    private final BlockchainService blockchainService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private JavaBlockchainService javaBlockchainService;

    @Value("${blockchain.native.enabled:false}")
    private boolean useNativeBlockchain;

    // Mapa de paises com IPs reais e localizacoes
    private static final Map<String, DadosPais> PAISES = new LinkedHashMap<>();

    static {
        PAISES.put("RU", new DadosPais("Russia", "Moscow, Russia",
                new String[]{"95.173.136.70", "77.88.55.60", "5.255.255.70", "46.175.224.1", "178.248.233.1"},
                new String[]{"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36",
                        "Mozilla/5.0 (X11; Linux x86_64; rv:119.0) Gecko/20100101 Firefox/119.0",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edge/120.0.0.0",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/605.1.15 Safari/17.0",
                        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 Chrome/119.0.0.0"}));

        PAISES.put("CN", new DadosPais("China", "Beijing, China",
                new String[]{"123.125.114.144", "180.101.50.242", "36.152.44.96", "111.206.221.17", "220.181.38.148"},
                new String[]{"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
                        "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 Chrome/119.0.0.0 Mobile",
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/537.36 Chrome/120.0.0.0"}));

        PAISES.put("JP", new DadosPais("Japao", "Tokyo, Japan",
                new String[]{"210.171.226.40", "202.32.227.189", "103.5.140.141", "133.242.0.1", "163.49.213.1"},
                new String[]{"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148",
                        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_1) AppleWebKit/605.1.15 Safari/17.1",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edge/120.0.0.0"}));

        PAISES.put("KP", new DadosPais("Coreia do Norte", "Pyongyang, North Korea",
                new String[]{"175.45.176.1", "175.45.176.2", "175.45.176.3", "175.45.176.67", "175.45.176.15"},
                new String[]{"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/110.0.0.0",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:109.0) Gecko/20100101 Firefox/115.0",
                        "Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/115.0.0.0",
                        "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 Chrome/109.0.0.0"}));

        PAISES.put("US", new DadosPais("Estados Unidos", "New York, USA",
                new String[]{"8.8.8.8", "1.1.1.1", "208.67.222.222", "4.2.2.1", "64.6.64.6"},
                new String[]{"Mozilla/5.0 (Macintosh; Intel Mac OS X 14_2) AppleWebKit/605.1.15 Safari/17.2",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/121.0.0.0",
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148",
                        "Mozilla/5.0 (X11; CrOS x86_64 14541.0.0) AppleWebKit/537.36 Chrome/120.0.0.0",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"}));

        PAISES.put("BR", new DadosPais("Brasil", "Sao Paulo, Brasil",
                new String[]{"200.160.2.3", "177.54.145.78", "189.1.162.1", "187.45.123.10", "179.184.0.1"},
                new String[]{"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
                        "Mozilla/5.0 (Linux; Android 13; SM-A546B) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile",
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/537.36 Chrome/120.0.0.0"}));

        PAISES.put("DE", new DadosPais("Alemanha", "Berlin, Germany",
                new String[]{"194.25.134.72", "195.135.220.3", "80.67.169.12", "46.182.19.48", "85.214.20.141"},
                new String[]{"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
                        "Mozilla/5.0 (X11; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_2) AppleWebKit/605.1.15 Safari/17.2",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edge/120.0.0.0",
                        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64) AppleWebKit/537.36 Chrome/120.0.0.0"}));

        PAISES.put("NG", new DadosPais("Nigeria", "Lagos, Nigeria",
                new String[]{"105.112.0.1", "197.210.0.1", "41.58.100.23", "41.215.241.50", "102.89.1.1"},
                new String[]{"Mozilla/5.0 (Linux; Android 12; SM-A127F) AppleWebKit/537.36 Chrome/119.0.0.0 Mobile",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/118.0.0.0",
                        "Mozilla/5.0 (Linux; Android 13; Infinix X6819) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile",
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0"}));

        PAISES.put("IN", new DadosPais("India", "Mumbai, India",
                new String[]{"115.248.200.1", "103.21.126.1", "49.44.0.1", "106.51.0.1", "117.194.0.1"},
                new String[]{"Mozilla/5.0 (Linux; Android 13; Redmi Note 12) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
                        "Mozilla/5.0 (Linux; Android 14; OnePlus 12) AppleWebKit/537.36 Chrome/121.0.0.0 Mobile",
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0"}));

        PAISES.put("IR", new DadosPais("Ira", "Tehran, Iran",
                new String[]{"5.160.0.1", "91.98.0.1", "2.190.0.1", "5.120.0.1", "78.39.0.1"},
                new String[]{"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/119.0.0.0",
                        "Mozilla/5.0 (X11; Linux x86_64; rv:115.0) Gecko/20100101 Firefox/115.0",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edge/119.0.0.0",
                        "Mozilla/5.0 (Linux; Android 13; Samsung Galaxy A54) AppleWebKit/537.36 Chrome/119.0.0.0 Mobile",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 Chrome/109.0.0.0"}));
    }

    public List<Map<String, String>> listarPaisesDisponiveis() {
        List<Map<String, String>> lista = new ArrayList<>();
        PAISES.forEach((codigo, dados) -> {
            Map<String, String> pais = new LinkedHashMap<>();
            pais.put("codigo", codigo);
            pais.put("nome", dados.nome);
            pais.put("localizacao", dados.localizacao);
            pais.put("totalIps", String.valueOf(dados.ips.length));
            lista.add(pais);
        });
        return lista;
    }

    public List<Map<String, String>> listarUsuarios() {
        List<Map<String, String>> lista = new ArrayList<>();
        repositorioUsuario.findAll().forEach(u -> {
            Map<String, String> user = new LinkedHashMap<>();
            user.put("id", String.valueOf(u.getId()));
            user.put("email", u.getEmail());
            user.put("nome", u.getNome());
            lista.add(user);
        });
        return lista;
    }

    @Transactional
    public Map<String, Object> gerarRegistros(String email, List<String> codigosPaises, int registrosPorPais, String tipoRegistro) {
        log.info("Iniciando geracao de dados para usuario: {}, paises: {}, registros/pais: {}, tipo: {}",
                email, codigosPaises, registrosPorPais, tipoRegistro);

        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado: " + email));

        List<Map<String, Object>> registrosGerados = new ArrayList<>();
        int totalGerados = 0;
        int totalErros = 0;

        for (String codigoPais : codigosPaises) {
            DadosPais dadosPais = PAISES.get(codigoPais.toUpperCase());
            if (dadosPais == null) {
                log.warn("Pais nao encontrado: {}", codigoPais);
                continue;
            }

            for (int i = 0; i < registrosPorPais && i < dadosPais.ips.length; i++) {
                try {
                    // Determinar se este registro sera suspeito
                    boolean suspeito = determinarSeSuspeito(tipoRegistro, codigoPais, i);

                    Map<String, Object> registro = gerarRegistroUnico(
                            usuario, dadosPais, i, totalGerados, suspeito);
                    registrosGerados.add(registro);
                    totalGerados++;

                    // Pequeno delay para variar timestamps
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Erro ao gerar registro {}/{} para pais {}: {}",
                            i + 1, registrosPorPais, codigoPais, e.getMessage());
                    totalErros++;
                }
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("totalGerados", totalGerados);
        resultado.put("totalErros", totalErros);
        resultado.put("usuario", email);
        resultado.put("paisesProcessados", codigosPaises.size());
        resultado.put("tipoRegistro", tipoRegistro);
        resultado.put("registros", registrosGerados);
        resultado.put("timestamp", LocalDateTime.now().toString());

        log.info("Geracao de dados concluida: {} registros gerados, {} erros, tipo: {}", totalGerados, totalErros, tipoRegistro);
        return resultado;
    }

    private static final Set<String> PAISES_ALTO_RISCO = Set.of("RU", "CN", "KP", "IR", "NG");

    private boolean determinarSeSuspeito(String tipoRegistro, String codigoPais, int indice) {
        switch (tipoRegistro.toUpperCase()) {
            case "NORMAL":
                return false;
            case "SUSPEITO":
                return true;
            case "MISTO":
            default:
                // Paises de alto risco geram registros suspeitos; demais geram normais
                return PAISES_ALTO_RISCO.contains(codigoPais.toUpperCase());
        }
    }

    private Map<String, Object> gerarRegistroUnico(Usuario usuario, DadosPais dadosPais, int indice, int offsetMinutos, boolean suspeito) {
        String ip = dadosPais.ips[indice];
        String userAgent = dadosPais.userAgents[indice];
        String localizacao = dadosPais.localizacao;

        // Para registros suspeitos, simular tentativas falhadas antes
        int tentativasFalhadas = 0;
        String tipoEvento = "LOGIN_SUCCESS";
        boolean sucesso = true;

        if (suspeito) {
            tentativasFalhadas = 3 + new Random().nextInt(5); // 3 a 7 tentativas falhadas
            // Registrar tentativas falhadas no log
            for (int f = 0; f < Math.min(tentativasFalhadas, 3); f++) {
                LogAuditoria logFalhado = LogAuditoria.builder()
                        .usuario(usuario)
                        .tipoEvento("LOGIN_FAILED")
                        .enderecoIp(ip)
                        .agenteUsuario(userAgent)
                        .localizacao(localizacao)
                        .infoDispositivo(userAgent)
                        .dataHora(LocalDateTime.now().minusMinutes(offsetMinutos * 30L + indice * 5L + f + 1))
                        .sucesso(false)
                        .build();
                repositorioLogAuditoria.save(logFalhado);
            }
        }

        // 1. Analise IA (ensemble: IF 40% + RF 30% + DL 30%)
        double aiRiskScore = 0.2;
        PerfilComportamentalIA perfilIA = null;
        try {
            // Para suspeitos: usar dados que a IA detecta como anomalos
            int falhasParaIA = suspeito ? tentativasFalhadas :
                    (usuario.getTentativasLoginFalhadas() != null ? usuario.getTentativasLoginFalhadas() : 0);

            String timezone = suspeito ? "Asia/Pyongyang" : "America/Sao_Paulo";
            String idioma = suspeito ? "ko-KP" : "pt-BR";
            String resolucao = suspeito ? "800x600" : "1920x1080";

            DadosContextoAcesso dadosContexto = new DadosContextoAcesso(
                    ip, userAgent, localizacao,
                    timezone, idioma, resolucao,
                    falhasParaIA
            );
            perfilIA = servicoAnaliseComportamentalIA.analisarComportamento(usuario, dadosContexto);
            aiRiskScore = perfilIA.getEnsembleScore() != null ? perfilIA.getEnsembleScore() : 0.2;
        } catch (Exception e) {
            log.warn("Erro na analise IA para IP {}: {}", ip, e.getMessage());
        }

        // 2. Calcular trust score
        double trustScore = 0.5;
        try {
            if (perfilIA != null) {
                ScoreConfianca scoreConfianca = servicoScoreConfianca.calcularScore(usuario, perfilIA);
                trustScore = scoreConfianca.getScoreAtual();
            }
        } catch (Exception e) {
            log.warn("Erro ao calcular trust score: {}", e.getMessage());
        }

        // 3. Determinar tipo de evento e decisao
        String decisao;
        if (suspeito) {
            if (trustScore < 0.3) {
                tipoEvento = "LOGIN_BLOCKED";
                decisao = "BLOCKED";
                sucesso = false;
            } else if (trustScore < 0.5) {
                tipoEvento = "LOGIN_MFA_REQUIRED";
                decisao = "REQUIRES_MFA";
            } else {
                decisao = "ALLOWED";
            }
        } else {
            decisao = trustScore >= 0.5 ? "ALLOWED" : "REQUIRES_MFA";
        }

        // 4. Registrar log de auditoria
        LocalDateTime dataHora = LocalDateTime.now().minusMinutes(offsetMinutos * 30L + indice * 5L);
        LogAuditoria logAuditoria = LogAuditoria.builder()
                .usuario(usuario)
                .tipoEvento(tipoEvento)
                .enderecoIp(ip)
                .agenteUsuario(userAgent)
                .localizacao(localizacao)
                .infoDispositivo(userAgent)
                .dataHora(dataHora)
                .sucesso(sucesso)
                .build();
        repositorioLogAuditoria.save(logAuditoria);

        // 5. Registrar no blockchain
        String hashTransacao = null;
        try {
            if (useNativeBlockchain && javaBlockchainService != null) {
                javaBlockchainService.recordAuthenticationEvent(
                        usuario, tipoEvento, decisao, aiRiskScore,
                        ip, localizacao, userAgent);
            } else {
                blockchainService.recordAuthenticationEvent(
                        usuario, tipoEvento, decisao, aiRiskScore,
                        ip, localizacao, userAgent);
            }
            hashTransacao = "registrado";
        } catch (Exception e) {
            log.warn("Erro ao registrar blockchain para IP {}: {}", ip, e.getMessage());
            hashTransacao = "erro: " + e.getMessage();
        }

        // 6. Atualizar dados do usuario
        usuario.setUltimoLoginIp(ip);
        usuario.setUltimoLoginLocalizacao(localizacao);
        usuario.setUltimoLoginDispositivo(userAgent);
        usuario.setUltimoLoginData(dataHora);
        if (suspeito) {
            usuario.setTentativasLoginFalhadas(
                    (usuario.getTentativasLoginFalhadas() != null ? usuario.getTentativasLoginFalhadas() : 0) + tentativasFalhadas);
        }
        repositorioUsuario.save(usuario);

        // Montar resultado
        Map<String, Object> registro = new LinkedHashMap<>();
        registro.put("pais", dadosPais.nome);
        registro.put("ip", ip);
        registro.put("localizacao", localizacao);
        registro.put("aiRiskScore", Math.round(aiRiskScore * 1000.0) / 1000.0);
        registro.put("trustScore", Math.round(trustScore * 1000.0) / 1000.0);
        registro.put("blockchain", hashTransacao);
        registro.put("classificacaoIA", perfilIA != null ? perfilIA.getClassificacaoAcesso() : "N/A");
        registro.put("tipoEvento", tipoEvento);
        registro.put("decisao", decisao);
        registro.put("suspeito", suspeito);
        registro.put("tentativasFalhadas", tentativasFalhadas);
        registro.put("dataHora", dataHora.toString());

        log.info("Registro gerado [{}]: {} | IP: {} | Risk: {} | Trust: {} | Decisao: {} | Blockchain: {}",
                suspeito ? "SUSPEITO" : "NORMAL", dadosPais.nome, ip, aiRiskScore, trustScore, decisao, hashTransacao);

        return registro;
    }

    private static class DadosPais {
        final String nome;
        final String localizacao;
        final String[] ips;
        final String[] userAgents;

        DadosPais(String nome, String localizacao, String[] ips, String[] userAgents) {
            this.nome = nome;
            this.localizacao = localizacao;
            this.ips = ips;
            this.userAgents = userAgents;
        }
    }
}
