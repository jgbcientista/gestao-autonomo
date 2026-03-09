package br.com.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonPredictionResponse {

    @JsonProperty("id_usuario")
    private Long userId;

    @JsonProperty("score_risco")
    private double riskScore;

    @JsonProperty("nivel_risco")
    private String riskLevel;

    @JsonProperty("decisao")
    private String decision;

    @JsonProperty("confianca")
    private double confidence;

    @JsonProperty("fatores")
    private List<String> factors;

    private String timestamp;
}
