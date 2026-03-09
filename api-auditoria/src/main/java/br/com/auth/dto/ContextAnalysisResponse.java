package br.com.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContextAnalysisResponse {

    @JsonProperty("analysis_id")
    private String analysisId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("overall_risk_score")
    private Double overallRiskScore;

    @JsonProperty("risk_level")
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @JsonProperty("decision")
    private String decision; // ALLOW, DENY, REQUIRE_MFA, REQUIRE_ADDITIONAL_VERIFICATION

    @JsonProperty("confidence_score")
    private Double confidenceScore;

    @JsonProperty("analysis_details")
    private AnalysisDetails analysisDetails;

    @JsonProperty("recommendations")
    private List<String> recommendations;

    @JsonProperty("blockchain_hash")
    private String blockchainHash;

    @JsonProperty("processed_at")
    private LocalDateTime processedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnalysisDetails {
        @JsonProperty("location_analysis")
        private LocationAnalysis locationAnalysis;

        @JsonProperty("device_analysis")
        private DeviceAnalysis deviceAnalysis;

        @JsonProperty("temporal_analysis")
        private TemporalAnalysis temporalAnalysis;

        @JsonProperty("behavioral_analysis")
        private BehavioralAnalysis behavioralAnalysis;

        @JsonProperty("network_analysis")
        private NetworkAnalysis networkAnalysis;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationAnalysis {
        @JsonProperty("consistency_score")
        private Double consistencyScore;

        @JsonProperty("distance_from_usual")
        private Double distanceFromUsual;

        @JsonProperty("is_known_location")
        private Boolean isKnownLocation;

        @JsonProperty("country_risk_level")
        private String countryRiskLevel;

        @JsonProperty("flags")
        private List<String> flags;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceAnalysis {
        @JsonProperty("consistency_score")
        private Double consistencyScore;

        @JsonProperty("is_known_device")
        private Boolean isKnownDevice;

        @JsonProperty("device_reputation")
        private String deviceReputation;

        @JsonProperty("browser_integrity")
        private Double browserIntegrity;

        @JsonProperty("flags")
        private List<String> flags;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TemporalAnalysis {
        @JsonProperty("pattern_score")
        private Double patternScore;

        @JsonProperty("is_usual_time")
        private Boolean isUsualTime;

        @JsonProperty("time_deviation")
        private Double timeDeviation;

        @JsonProperty("frequency_anomaly")
        private Boolean frequencyAnomaly;

        @JsonProperty("flags")
        private List<String> flags;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BehavioralAnalysis {
        @JsonProperty("behavioral_score")
        private Double behavioralScore;

        @JsonProperty("typing_anomaly")
        private Boolean typingAnomaly;

        @JsonProperty("interaction_pattern")
        private String interactionPattern;

        @JsonProperty("ai_confidence")
        private Double aiConfidence;

        @JsonProperty("flags")
        private List<String> flags;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NetworkAnalysis {
        @JsonProperty("network_score")
        private Double networkScore;

        @JsonProperty("threat_indicators")
        private Map<String, Boolean> threatIndicators;

        @JsonProperty("ip_reputation")
        private String ipReputation;

        @JsonProperty("anomalous_traffic")
        private Boolean anomalousTraffic;

        @JsonProperty("flags")
        private List<String> flags;
    }
} 