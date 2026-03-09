package br.com.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequisicaoAutenticacao {
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
    
    private String enderecoIp;
    private String agenteUsuario;
    private String localizacao;

    // Métodos auxiliares para compatibilidade com código existente
    public String getPassword() {
        return senha;
    }

    public void setPassword(String senha) {
        this.senha = senha;
    }

    public String getIpAddress() {
        return enderecoIp;
    }

    public void setIpAddress(String ip) {
        this.enderecoIp = ip;
    }

    public String getUserAgent() {
        return agenteUsuario;
    }

    public void setUserAgent(String agente) {
        this.agenteUsuario = agente;
    }

    public String getLocation() {
        return localizacao;
    }

    public void setLocation(String localizacao) {
        this.localizacao = localizacao;
    }
} 