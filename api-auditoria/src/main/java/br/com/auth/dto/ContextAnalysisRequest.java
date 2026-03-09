package br.com.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContextAnalysisRequest {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("location")
    private String location;

    @JsonProperty("device_fingerprint")
    private String deviceFingerprint;

    @JsonProperty("geolocation")
    private GeolocationData geolocation;

    @JsonProperty("network_info")
    private NetworkInfo networkInfo;

    @JsonProperty("behavioral_data")
    private BehavioralData behavioralData;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeolocationData {
        private Double latitude;
        private Double longitude;
        private String country;
        private String region;
        private String city;
        private String timezone;
        private String isp;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NetworkInfo {
        @JsonProperty("ip_address")
        private String ipAddress;
        
        @JsonProperty("user_agent")
        private String userAgent;
        
        @JsonProperty("connection_type")
        private String connectionType;
        
        @JsonProperty("vpn_detected")
        private Boolean vpnDetected;
        
        @JsonProperty("proxy_detected")
        private Boolean proxyDetected;
        
        @JsonProperty("tor_detected")
        private Boolean torDetected;
        
        @JsonProperty("ip_reputation")
        private String ipReputation;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BehavioralData {
        @JsonProperty("typing_patterns")
        private Map<String, Object> typingPatterns;
        
        @JsonProperty("mouse_movements")
        private Map<String, Object> mouseMovements;
        
        @JsonProperty("screen_resolution")
        private String screenResolution;
        
        @JsonProperty("browser_plugins")
        private String[] browserPlugins;
        
        @JsonProperty("session_duration")
        private Long sessionDuration;
        
        @JsonProperty("page_interactions")
        private Integer pageInteractions;
    }
} 