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
public class RespostaAutenticacao {
    
    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;
    
    @Schema(description = "Login/email do usuário", example = "joao.silva@email.com")
    private String login;

    // Métodos auxiliares para compatibilidade com código existente
    public String getName() {
        return nome;
    }

    public void setName(String nome) {
        this.nome = nome;
    }
} 