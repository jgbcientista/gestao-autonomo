package br.com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRelatorioDTO {

    private Long id;
    private String nome;
    private String email;
    private Double scoreAtual;
    private String nivelConfianca;
    private LocalDateTime ultimoLoginData;
    private Boolean contaBloqueada;
}
