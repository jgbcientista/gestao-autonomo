package br.com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resposta da autenticação contendo token JWT e informações do usuário")
public class AuthenticationResponse {
    
    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String name;
    
    @Schema(description = "Email do usuário", example = "joao.silva@email.com")
    private String email;

    @Schema(description = "Papel/perfil do usuário", example = "ADMIN")
    private String role;

    @Schema(description = "Indica se é necessário autenticação de dois fatores", example = "false")
    private Boolean requiresMfa;

    @Schema(description = "Mensagem relacionada ao MFA", example = "Autenticação de dois fatores necessária")
    private String mfaMessage;

    @Schema(description = "QR Code em Base64 para configuração do MFA", example = "data:image/png;base64,...")
    private String mfaQrCode;

    @Schema(description = "Segredo TOTP para configuração manual do MFA")
    private String mfaSecret;

    @Schema(description = "Trust Score do usuário (0.0 a 1.0)", example = "0.85")
    private Double trustScore;

    @Schema(description = "Nível de confiança baseado no trust score", example = "HIGH")
    private String trustLevel;

    @Schema(description = "Score de risco calculado pela IA ensemble", example = "0.15")
    private Double aiRiskScore;
} 