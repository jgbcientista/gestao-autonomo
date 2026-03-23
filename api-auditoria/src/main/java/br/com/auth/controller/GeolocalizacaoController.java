package br.com.auth.controller;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import br.com.auth.service.ServicoGeolocalizacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller para testes e demonstração do serviço de geolocalização
 */
@RestController
@RequestMapping("/api/v1/geolocalizacao")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Geolocalização", description = "APIs para testes de geolocalização e cálculo de distâncias")
public class GeolocalizacaoController {

    private final ServicoGeolocalizacao servicoGeolocalizacao;
    private final RepositorioLogAuditoria repositorioLogAuditoria;
    private final RepositorioUsuario repositorioUsuario;

    @GetMapping("/ip/{ipAddress}")
    @Operation(summary = "Obter geolocalização por IP", 
               description = "Retorna dados de geolocalização para um endereço IP específico")
    public ResponseEntity<ServicoGeolocalizacao.DadosGeolocalizacao> obterGeoPorIp(
            @Parameter(description = "Endereço IP para consulta", example = "8.8.8.8")
            @PathVariable String ipAddress) {
        
        log.info("Consultando geolocalização para IP: {}", ipAddress);
        
        ServicoGeolocalizacao.DadosGeolocalizacao dados = 
            servicoGeolocalizacao.obterLocalizacaoPorIp(ipAddress);
        
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/endereco")
    @Operation(summary = "Obter coordenadas por endereço", 
               description = "Converte endereço em coordenadas geográficas (geocoding)")
    public ResponseEntity<ServicoGeolocalizacao.DadosGeolocalizacao> obterCoordenadasPorEndereco(
            @Parameter(description = "Endereço para converter", example = "São Paulo, Brasil")
            @RequestParam String endereco) {
        
        log.info("Obtendo coordenadas para endereço: {}", endereco);
        
        ServicoGeolocalizacao.DadosGeolocalizacao dados = 
            servicoGeolocalizacao.obterCoordenadasPorEndereco(endereco);
        
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/distancia")
    @Operation(summary = "Calcular distância entre coordenadas", 
               description = "Calcula a distância em quilômetros entre duas coordenadas geográficas")
    public ResponseEntity<Map<String, Object>> calcularDistancia(
            @Parameter(description = "Latitude do primeiro ponto", example = "-23.5505")
            @RequestParam double lat1,
            @Parameter(description = "Longitude do primeiro ponto", example = "-46.6333")
            @RequestParam double lon1,
            @Parameter(description = "Latitude do segundo ponto", example = "-22.9068")
            @RequestParam double lat2,
            @Parameter(description = "Longitude do segundo ponto", example = "-43.1729")
            @RequestParam double lon2) {
        
        log.info("Calculando distância entre ({}, {}) e ({}, {})", lat1, lon1, lat2, lon2);
        
        double distancia = servicoGeolocalizacao.calcularDistancia(lat1, lon1, lat2, lon2);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("ponto1", Map.of("latitude", lat1, "longitude", lon1));
        resultado.put("ponto2", Map.of("latitude", lat2, "longitude", lon2));
        resultado.put("distanciaKm", Math.round(distancia * 100.0) / 100.0);
        resultado.put("distanciaMilhas", Math.round(distancia * 0.621371 * 100.0) / 100.0);
        
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/distancia-enderecos")
    @Operation(summary = "Calcular distância entre endereços", 
               description = "Calcula a distância entre dois endereços convertendo-os em coordenadas")
    public ResponseEntity<Map<String, Object>> calcularDistanciaEnderecos(
            @Parameter(description = "Primeiro endereço", example = "São Paulo, Brasil")
            @RequestParam String endereco1,
            @Parameter(description = "Segundo endereço", example = "Rio de Janeiro, Brasil")
            @RequestParam String endereco2) {
        
        log.info("Calculando distância entre '{}' e '{}'", endereco1, endereco2);
        
        try {
            ServicoGeolocalizacao.DadosGeolocalizacao loc1 = 
                servicoGeolocalizacao.obterCoordenadasPorEndereco(endereco1);
            ServicoGeolocalizacao.DadosGeolocalizacao loc2 = 
                servicoGeolocalizacao.obterCoordenadasPorEndereco(endereco2);
            
            if (!loc1.isValido() || !loc2.isValido()) {
                Map<String, Object> erro = new HashMap<>();
                erro.put("erro", "Não foi possível obter coordenadas para um ou ambos os endereços");
                erro.put("endereco1Valido", loc1.isValido());
                erro.put("endereco2Valido", loc2.isValido());
                return ResponseEntity.badRequest().body(erro);
            }
            
            double distancia = servicoGeolocalizacao.calcularDistancia(loc1, loc2);
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("endereco1", Map.of(
                "endereco", endereco1,
                "coordenadas", Map.of("latitude", loc1.getLatitude(), "longitude", loc1.getLongitude()),
                "localizacao", loc1.getLocalizacaoCompleta(),
                "fonte", loc1.getFonte()
            ));
            resultado.put("endereco2", Map.of(
                "endereco", endereco2,
                "coordenadas", Map.of("latitude", loc2.getLatitude(), "longitude", loc2.getLongitude()),
                "localizacao", loc2.getLocalizacaoCompleta(),
                "fonte", loc2.getFonte()
            ));
            resultado.put("distanciaKm", Math.round(distancia * 100.0) / 100.0);
            resultado.put("distanciaMilhas", Math.round(distancia * 0.621371 * 100.0) / 100.0);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Erro ao calcular distância entre endereços: {}", e.getMessage());
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro interno ao calcular distância");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }

    @GetMapping("/pais/{codigoPais}/risco")
    @Operation(summary = "Verificar se país é de alto risco",
               description = "Verifica se um país é considerado de alto risco para fraudes")
    public ResponseEntity<Map<String, Object>> verificarPaisRisco(
            @Parameter(description = "Código do país (ISO 2 letras)", example = "BR")
            @PathVariable String codigoPais) {
        
        log.info("Verificando risco para país: {}", codigoPais);
        
        boolean altoRisco = servicoGeolocalizacao.isPaisAltoRisco(codigoPais);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("codigoPais", codigoPais.toUpperCase());
        resultado.put("altoRisco", altoRisco);
        resultado.put("nivelRisco", altoRisco ? "ALTO" : "NORMAL");
        
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/analisar")
    @Operation(summary = "Analisar localização",
               description = "Analisa dados de localização para detecção de anomalias")
    public ResponseEntity<Map<String, Object>> analisarLocalizacao(@RequestBody Map<String, Object> request) {

        log.info("Analisando localização: {}", request);

        try {
            String ip = (String) request.getOrDefault("ip", "0.0.0.0");
            ServicoGeolocalizacao.DadosGeolocalizacao dados = servicoGeolocalizacao.obterLocalizacaoPorIp(ip);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("ip", ip);
            resultado.put("geolocalizacao", dados);
            resultado.put("valido", dados.isValido());

            if (request.containsKey("codigoPais")) {
                String codigoPais = (String) request.get("codigoPais");
                resultado.put("paisAltoRisco", servicoGeolocalizacao.isPaisAltoRisco(codigoPais));
            }

            resultado.put("analisadoEm", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro ao analisar localização: {}", e.getMessage());
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao analisar localização");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }

    @GetMapping("/usuario/{userId}/historico")
    @Operation(summary = "Obter histórico de localização do usuário",
               description = "Retorna o histórico de localizações acessadas por um usuário")
    public ResponseEntity<?> obterHistoricoLocalizacaoUsuario(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        log.info("Obtendo histórico de localização para usuário: {}", userId);

        try {
            Usuario usuario = repositorioUsuario.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<LogAuditoria> logs = repositorioLogAuditoria.findByUsuario(usuario);

            List<Map<String, Object>> historico = logs.stream()
                    .sorted((a, b) -> {
                        if (a.getDataHora() == null) return 1;
                        if (b.getDataHora() == null) return -1;
                        return b.getDataHora().compareTo(a.getDataHora());
                    })
                    .map(logEntry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("dataHora", logEntry.getDataHora());
                        item.put("enderecoIp", logEntry.getEnderecoIp());
                        item.put("localizacao", logEntry.getLocalizacao());
                        item.put("dispositivo", logEntry.getAgenteUsuario() != null
                                ? logEntry.getAgenteUsuario() : logEntry.getInfoDispositivo());
                        item.put("tipoEvento", logEntry.getTipoEvento());
                        item.put("sucesso", logEntry.isSucesso());
                        item.put("nivelRisco", calcularNivelRisco(logEntry));
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(historico);

        } catch (Exception e) {
            log.error("Erro ao obter histórico de localização para usuário: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao obter histórico: " + e.getMessage()));
        }
    }

    private String calcularNivelRisco(LogAuditoria log) {
        if (!log.isSucesso()) return "ALTO";
        String loc = log.getLocalizacao();
        if (loc != null && !loc.contains("BR") && !loc.contains("Brasil") && !loc.contains("São Paulo")) {
            return "ALTO";
        }
        if (loc != null && !loc.contains("São Paulo") && !loc.contains("Salvador")) {
            return "MEDIO";
        }
        return "BAIXO";
    }

    @GetMapping("/usuario/{userId}/mapa-acessos")
    @Operation(summary = "Obter dados de mapa de acessos",
               description = "Retorna dados agregados de localização para visualização em mapa")
    public ResponseEntity<?> obterMapaAcessos(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        log.info("Obtendo dados de mapa de acessos para usuário: {}", userId);

        try {
            Usuario usuario = repositorioUsuario.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<LogAuditoria> logs = repositorioLogAuditoria.findByUsuario(usuario);

            // Coordenadas conhecidas para cidades de demonstração
            Map<String, double[]> coordenadasCidades = Map.ofEntries(
                Map.entry("São Paulo, BR", new double[]{-23.5505, -46.6333}),
                Map.entry("Rio de Janeiro, BR", new double[]{-22.9068, -43.1729}),
                Map.entry("Salvador, BR", new double[]{-12.9714, -38.5124}),
                Map.entry("Brasília, BR", new double[]{-15.7975, -47.8919}),
                Map.entry("Belo Horizonte, BR", new double[]{-19.9167, -43.9345}),
                Map.entry("Recife, BR", new double[]{-8.0476, -34.8770}),
                Map.entry("Curitiba, BR", new double[]{-25.4284, -49.2733}),
                Map.entry("Porto Alegre, BR", new double[]{-30.0346, -51.2177}),
                Map.entry("New York, US", new double[]{40.7128, -74.0060}),
                Map.entry("Columbus, US", new double[]{39.9612, -82.9988}),
                Map.entry("Paris, FR", new double[]{48.8566, 2.3522}),
                Map.entry("Frankfurt, DE", new double[]{50.1109, 8.6821}),
                Map.entry("Mumbai, IN", new double[]{19.0760, 72.8777}),
                Map.entry("Tokyo, JP", new double[]{35.6762, 139.6503}),
                Map.entry("London, GB", new double[]{51.5074, -0.1278}),
                Map.entry("Sydney, AU", new double[]{-33.8688, 151.2093}),
                Map.entry("Amsterdam, NL", new double[]{52.3676, 4.9041}),
                Map.entry("Seoul, KR", new double[]{37.5665, 126.9780}),
                Map.entry("Menlo Park, US", new double[]{37.4529, -122.1817}),
                Map.entry("San Francisco, US", new double[]{37.7749, -122.4194}),
                Map.entry("Berlin, DE", new double[]{52.5200, 13.4050}),
                Map.entry("Moscow, RU", new double[]{55.7558, 37.6173}),
                Map.entry("Nairobi, KE", new double[]{-1.2921, 36.8219}),
                Map.entry("Lagos, NG", new double[]{6.5244, 3.3792})
            );

            // Agregar acessos por localização
            Map<String, List<LogAuditoria>> acessosPorLocal = logs.stream()
                    .filter(l -> l.getLocalizacao() != null)
                    .collect(Collectors.groupingBy(LogAuditoria::getLocalizacao));

            List<Map<String, Object>> acessosNormais = new java.util.ArrayList<>();
            List<Map<String, Object>> acessosSuspeitos = new java.util.ArrayList<>();

            acessosPorLocal.forEach((local, logsLocal) -> {
                double[] coords = coordenadasCidades.getOrDefault(local, null);
                if (coords == null) return;

                String nivelRisco = calcularNivelRisco(logsLocal.get(0));
                long falhas = logsLocal.stream().filter(l -> !l.isSucesso()).count();

                Map<String, Object> ponto = new java.util.HashMap<>();
                ponto.put("latitude", coords[0]);
                ponto.put("longitude", coords[1]);
                ponto.put("cidade", local);
                ponto.put("contagem", logsLocal.size());
                ponto.put("falhas", falhas);
                ponto.put("nivelRisco", nivelRisco);
                ponto.put("ultimoAcesso", logsLocal.stream()
                        .map(LogAuditoria::getDataHora)
                        .filter(java.util.Objects::nonNull)
                        .max(java.time.LocalDateTime::compareTo)
                        .orElse(null));

                if ("ALTO".equals(nivelRisco) || falhas > 0) {
                    acessosSuspeitos.add(ponto);
                } else {
                    acessosNormais.add(ponto);
                }
            });

            // Detectar viagens impossíveis (conexões entre acessos sequenciais)
            List<Map<String, Object>> conexoes = new java.util.ArrayList<>();
            List<LogAuditoria> logsCronologicos = logs.stream()
                    .filter(l -> l.getDataHora() != null && l.getLocalizacao() != null)
                    .sorted((a, b) -> a.getDataHora().compareTo(b.getDataHora()))
                    .collect(Collectors.toList());

            for (int i = 1; i < logsCronologicos.size(); i++) {
                LogAuditoria anterior = logsCronologicos.get(i - 1);
                LogAuditoria atual = logsCronologicos.get(i);

                double[] coordsAnterior = coordenadasCidades.getOrDefault(anterior.getLocalizacao(), null);
                double[] coordsAtual = coordenadasCidades.getOrDefault(atual.getLocalizacao(), null);

                if (coordsAnterior == null || coordsAtual == null) continue;
                if (anterior.getLocalizacao().equals(atual.getLocalizacao())) continue;

                double distanciaKm = servicoGeolocalizacao.calcularDistancia(
                        coordsAnterior[0], coordsAnterior[1],
                        coordsAtual[0], coordsAtual[1]);

                long intervaloMinutos = java.time.Duration.between(anterior.getDataHora(), atual.getDataHora()).toMinutes();
                if (intervaloMinutos <= 0) intervaloMinutos = 1;

                double velocidadeKmH = (distanciaKm / intervaloMinutos) * 60;
                boolean viagemImpossivel = velocidadeKmH > 900; // Mais rápido que avião comercial

                Map<String, Object> conexao = new java.util.HashMap<>();
                conexao.put("origem", Map.of("latitude", coordsAnterior[0], "longitude", coordsAnterior[1], "cidade", anterior.getLocalizacao()));
                conexao.put("destino", Map.of("latitude", coordsAtual[0], "longitude", coordsAtual[1], "cidade", atual.getLocalizacao()));
                conexao.put("distanciaKm", Math.round(distanciaKm));
                conexao.put("intervaloMinutos", intervaloMinutos);
                conexao.put("velocidadeKmH", Math.round(velocidadeKmH));
                conexao.put("viagemImpossivel", viagemImpossivel);

                conexoes.add(conexao);
            }

            Map<String, Object> resultado = new java.util.HashMap<>();
            resultado.put("acessosNormais", acessosNormais);
            resultado.put("acessosSuspeitos", acessosSuspeitos);
            resultado.put("conexoes", conexoes);
            resultado.put("totalAcessos", logs.size());

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("Erro ao obter dados de mapa para usuário: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao obter dados do mapa: " + e.getMessage()));
        }
    }

    @GetMapping("/teste-completo")
    @Operation(summary = "Teste completo do serviço de geolocalização",
               description = "Executa uma bateria de testes para demonstrar todas as funcionalidades")
    public ResponseEntity<Map<String, Object>> testeCompleto() {
        
        log.info("Executando teste completo do serviço de geolocalização");
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Teste 1: Geolocalização por IP público
            ServicoGeolocalizacao.DadosGeolocalizacao geoGoogle = 
                servicoGeolocalizacao.obterLocalizacaoPorIp("8.8.8.8");
            resultado.put("teste1_ip_publico", Map.of(
                "ip", "8.8.8.8",
                "resultado", geoGoogle,
                "valido", geoGoogle.isValido()
            ));
            
            // Teste 2: Geolocalização por IP privado (deve retornar padrão)
            ServicoGeolocalizacao.DadosGeolocalizacao geoPrivado = 
                servicoGeolocalizacao.obterLocalizacaoPorIp("192.168.1.1");
            resultado.put("teste2_ip_privado", Map.of(
                "ip", "192.168.1.1",
                "resultado", geoPrivado,
                "ehPadrao", "PADRAO".equals(geoPrivado.getFonte())
            ));
            
            // Teste 3: Geocoding de endereço
            ServicoGeolocalizacao.DadosGeolocalizacao geoEndereco = 
                servicoGeolocalizacao.obterCoordenadasPorEndereco("São Paulo, Brasil");
            resultado.put("teste3_geocoding", Map.of(
                "endereco", "São Paulo, Brasil",
                "resultado", geoEndereco,
                "valido", geoEndereco.isValido()
            ));
            
            // Teste 4: Cálculo de distância
            double distancia = servicoGeolocalizacao.calcularDistancia(
                -23.5505, -46.6333, // São Paulo
                -22.9068, -43.1729  // Rio de Janeiro
            );
            resultado.put("teste4_distancia", Map.of(
                "origem", "São Paulo (-23.5505, -46.6333)",
                "destino", "Rio de Janeiro (-22.9068, -43.1729)",
                "distanciaKm", Math.round(distancia * 100.0) / 100.0
            ));
            
            // Teste 5: Verificação de país de risco
            resultado.put("teste5_pais_risco", Map.of(
                "brasil_BR", servicoGeolocalizacao.isPaisAltoRisco("BR"),
                "china_CN", servicoGeolocalizacao.isPaisAltoRisco("CN"),
                "russia_RU", servicoGeolocalizacao.isPaisAltoRisco("RU")
            ));
            
            resultado.put("status", "SUCESSO");
            resultado.put("mensagem", "Todos os testes executados com sucesso");
            
        } catch (Exception e) {
            log.error("Erro durante teste completo: {}", e.getMessage());
            resultado.put("status", "ERRO");
            resultado.put("erro", e.getMessage());
        }
        
        return ResponseEntity.ok(resultado);
    }
} 