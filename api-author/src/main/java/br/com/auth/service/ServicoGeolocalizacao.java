package br.com.auth.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;

/**
 * Serviço de Geolocalização com integração a múltiplas APIs
 * Suporta: IP-API, OpenCage, Nominatim
 * Calcula distâncias usando fórmula de Haversine
 */
@Slf4j
@Service
public class ServicoGeolocalizacao {

    private final RestTemplate restTemplate;
    
    @Value("${geolocalizacao.opencage.api-key:}")
    private String openCageApiKey;
    
    @Value("${geolocalizacao.timeout:5000}")
    private int timeoutMs;
    
    @Value("${geolocalizacao.fallback.enabled:true}")
    private boolean fallbackEnabled;

    public ServicoGeolocalizacao() {
        this.restTemplate = new RestTemplate();
        // Configurar timeout
        this.restTemplate.getMessageConverters().forEach(converter -> {
            // Configurações de timeout serão aplicadas por requisição
        });
    }

    /**
     * Obter dados de geolocalização por IP
     */
    public DadosGeolocalizacao obterLocalizacaoPorIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty() || isPrivateIp(ipAddress)) {
            log.debug("IP privado ou inválido: {}, retornando localização padrão", ipAddress);
            return criarLocalizacaoPadrao();
        }

        try {
            // Tentar IP-API primeiro (gratuito, sem chave necessária)
            DadosGeolocalizacao dados = obterDadosIpApi(ipAddress);
            
            if (dados != null && dados.isValido()) {
                log.debug("Geolocalização obtida via IP-API para IP: {}", ipAddress);
                return dados;
            }

            // Fallback para outras APIs se habilitado
            if (fallbackEnabled) {
                dados = obterDadosOpenCage(ipAddress);
                if (dados != null && dados.isValido()) {
                    log.debug("Geolocalização obtida via OpenCage para IP: {}", ipAddress);
                    return dados;
                }
            }

        } catch (Exception e) {
            log.warn("Erro ao obter geolocalização para IP {}: {}", ipAddress, e.getMessage());
        }

        log.debug("Usando localização padrão para IP: {}", ipAddress);
        return criarLocalizacaoPadrao();
    }

    /**
     * Obter coordenadas por endereço (geocoding)
     */
    public DadosGeolocalizacao obterCoordenadasPorEndereco(String endereco) {
        if (endereco == null || endereco.trim().isEmpty()) {
            return criarLocalizacaoPadrao();
        }

        try {
            // Usar Nominatim (OpenStreetMap) - gratuito
            String url = String.format(
                "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1&addressdetails=1",
                endereco.replace(" ", "%20")
            );

            CompletableFuture<NominatimResponse[]> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return restTemplate.getForObject(url, NominatimResponse[].class);
                } catch (Exception e) {
                    log.warn("Erro ao consultar Nominatim: {}", e.getMessage());
                    return null;
                }
            });

            NominatimResponse[] responses = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            
            if (responses != null && responses.length > 0) {
                NominatimResponse response = responses[0];
                return DadosGeolocalizacao.builder()
                    .latitude(Double.parseDouble(response.lat))
                    .longitude(Double.parseDouble(response.lon))
                    .cidade(response.address != null ? response.address.city : null)
                    .estado(response.address != null ? response.address.state : null)
                    .pais(response.address != null ? response.address.country : null)
                    .codigoPais(response.address != null ? response.address.countryCode : null)
                    .endereco(response.displayName)
                    .precisao("ALTA")
                    .fonte("NOMINATIM")
                    .build();
            }

        } catch (Exception e) {
            log.warn("Erro ao obter coordenadas para endereço {}: {}", endereco, e.getMessage());
        }

        return criarLocalizacaoPadrao();
    }

    /**
     * Calcular distância entre duas coordenadas usando fórmula de Haversine
     */
    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }

        final double R = 6371; // Raio da Terra em km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distância em km
    }

    /**
     * Calcular distância entre duas localizações
     */
    public double calcularDistancia(DadosGeolocalizacao loc1, DadosGeolocalizacao loc2) {
        if (loc1 == null || loc2 == null || 
            loc1.getLatitude() == null || loc1.getLongitude() == null ||
            loc2.getLatitude() == null || loc2.getLongitude() == null) {
            return Double.MAX_VALUE; // Distância máxima se dados inválidos
        }

        return calcularDistancia(
            loc1.getLatitude(), loc1.getLongitude(),
            loc2.getLatitude(), loc2.getLongitude()
        );
    }

    /**
     * Verificar se é um país de alto risco
     */
    public boolean isPaisAltoRisco(String codigoPais) {
        if (codigoPais == null) return false;
        
        String[] paisesAltoRisco = {
            "CN", "RU", "NG", "PK", "BD", "KP", "IR", "SY", "AF", "IQ"
        };
        
        for (String pais : paisesAltoRisco) {
            if (pais.equalsIgnoreCase(codigoPais)) {
                return true;
            }
        }
        
        return false;
    }

    public Map<String, String> obterLocalizacao(String ip) {
        // TODO: Implementar integração com serviço de geolocalização
        // Por enquanto retorna dados mock
        Map<String, String> localizacao = new HashMap<>();
        localizacao.put("pais", "Brasil");
        localizacao.put("cidade", "São Paulo");
        localizacao.put("latitude", "-23.5505");
        localizacao.put("longitude", "-46.6333");
        return localizacao;
    }

    // Métodos privados

    private DadosGeolocalizacao obterDadosIpApi(String ipAddress) {
        try {
            String url = "http://ip-api.com/json/" + ipAddress + "?fields=status,country,countryCode,region,regionName,city,lat,lon,timezone,isp,proxy";
            
            CompletableFuture<IpApiResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return restTemplate.getForObject(url, IpApiResponse.class);
                } catch (Exception e) {
                    log.debug("Erro ao consultar IP-API: {}", e.getMessage());
                    return null;
                }
            });

            IpApiResponse response = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            
            if (response != null && "success".equals(response.status)) {
                return DadosGeolocalizacao.builder()
                    .latitude(response.lat)
                    .longitude(response.lon)
                    .cidade(response.city)
                    .estado(response.regionName)
                    .pais(response.country)
                    .codigoPais(response.countryCode)
                    .fusoHorario(response.timezone)
                    .provedor(response.isp)
                    .isProxy(response.proxy)
                    .precisao("MEDIA")
                    .fonte("IP_API")
                    .build();
            }

        } catch (Exception e) {
            log.debug("Timeout ou erro na consulta IP-API: {}", e.getMessage());
        }

        return null;
    }

    private DadosGeolocalizacao obterDadosOpenCage(String ipAddress) {
        if (openCageApiKey == null || openCageApiKey.trim().isEmpty()) {
            return null;
        }

        try {
            String url = String.format(
                "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s&limit=1",
                ipAddress, openCageApiKey
            );

            CompletableFuture<OpenCageResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return restTemplate.getForObject(url, OpenCageResponse.class);
                } catch (Exception e) {
                    log.debug("Erro ao consultar OpenCage: {}", e.getMessage());
                    return null;
                }
            });

            OpenCageResponse response = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            
            if (response != null && response.results != null && response.results.length > 0) {
                OpenCageResult result = response.results[0];
                return DadosGeolocalizacao.builder()
                    .latitude(result.geometry.lat)
                    .longitude(result.geometry.lng)
                    .cidade(result.components.city)
                    .estado(result.components.state)
                    .pais(result.components.country)
                    .codigoPais(result.components.countryCode)
                    .endereco(result.formatted)
                    .precisao("ALTA")
                    .fonte("OPENCAGE")
                    .build();
            }

        } catch (Exception e) {
            log.debug("Timeout ou erro na consulta OpenCage: {}", e.getMessage());
        }

        return null;
    }

    private boolean isPrivateIp(String ip) {
        if (ip == null) return true;
        
        return ip.startsWith("192.168.") || 
               ip.startsWith("10.") || 
               ip.startsWith("172.") ||
               ip.equals("127.0.0.1") ||
               ip.equals("localhost");
    }

    private DadosGeolocalizacao criarLocalizacaoPadrao() {
        return DadosGeolocalizacao.builder()
            .latitude(-23.5505)
            .longitude(-46.6333)
            .cidade("São Paulo")
            .estado("São Paulo")
            .pais("Brasil")
            .codigoPais("BR")
            .fusoHorario("America/Sao_Paulo")
            .precisao("BAIXA")
            .fonte("PADRAO")
            .build();
    }

    // Classes de resposta das APIs

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpApiResponse {
        private String status;
        private String country;
        private String countryCode;
        private String region;
        private String regionName;
        private String city;
        private Double lat;
        private Double lon;
        private String timezone;
        private String isp;
        private Boolean proxy;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenCageResponse {
        private OpenCageResult[] results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenCageResult {
        private String formatted;
        private OpenCageGeometry geometry;
        private OpenCageComponents components;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenCageGeometry {
        private Double lat;
        private Double lng;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenCageComponents {
        private String city;
        private String state;
        private String country;
        @JsonProperty("country_code")
        private String countryCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NominatimResponse {
        private String lat;
        private String lon;
        @JsonProperty("display_name")
        private String displayName;
        private NominatimAddress address;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NominatimAddress {
        private String city;
        private String state;
        private String country;
        @JsonProperty("country_code")
        private String countryCode;
    }

    // Classe de dados de geolocalização

    @Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DadosGeolocalizacao {
        private Double latitude;
        private Double longitude;
        private String cidade;
        private String estado;
        private String pais;
        private String codigoPais;
        private String fusoHorario;
        private String provedor;
        private String endereco;
        private Boolean isProxy;
        private String precisao; // ALTA, MEDIA, BAIXA
        private String fonte; // IP_API, OPENCAGE, NOMINATIM, PADRAO

        public boolean isValido() {
            return latitude != null && longitude != null && 
                   latitude >= -90 && latitude <= 90 &&
                   longitude >= -180 && longitude <= 180;
        }

        public String getLocalizacaoCompleta() {
            StringBuilder sb = new StringBuilder();
            if (cidade != null) sb.append(cidade);
            if (estado != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(estado);
            }
            if (pais != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(pais);
            }
            return sb.toString();
        }
    }
} 