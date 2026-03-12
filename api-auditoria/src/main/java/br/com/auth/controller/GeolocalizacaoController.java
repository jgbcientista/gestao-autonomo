package br.com.auth.controller;

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
    public ResponseEntity<Map<String, Object>> obterHistoricoLocalizacaoUsuario(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        log.info("Obtendo histórico de localização para usuário: {}", userId);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("userId", userId);
        resultado.put("historico", List.of());
        resultado.put("totalRegistros", 0);
        resultado.put("consultadoEm", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(resultado);
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