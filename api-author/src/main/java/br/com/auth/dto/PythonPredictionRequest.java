package br.com.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonPredictionRequest {

    @JsonProperty("id_usuario")
    private Long userId;

    @JsonProperty("endereco_ip")
    private String ipAddress;

    @JsonProperty("agente_navegador")
    private String userAgent;

    @JsonProperty("hora_do_dia")
    private int hourOfDay;

    @JsonProperty("dia_da_semana")
    private int dayOfWeek;

    @JsonProperty("tentativas_login_ultima_hora")
    private int loginAttemptsLastHour;

    @JsonProperty("novo_dispositivo")
    private boolean isNewDevice;

    @JsonProperty("nova_localizacao")
    private boolean isNewLocation;

    @JsonProperty("usa_vpn")
    private boolean isVpn;

    @JsonProperty("usa_tor")
    private boolean isTor;

    @JsonProperty("duracao_media_sessao")
    private double sessionDurationAvg;

    @JsonProperty("paginas_por_sessao_media")
    private double pagesPerSessionAvg;

    @JsonProperty("horas_desde_ultimo_login")
    private double timeSinceLastLoginHours;
}
