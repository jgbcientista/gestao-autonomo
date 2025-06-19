package br.com.auth.dominio.interfaces;

import br.com.auth.dto.AuthenticationRequest;
import br.com.auth.dto.AuthenticationResponse;
import br.com.auth.dto.RegisterRequest;

/**
 * Interface que define o contrato para serviços de autenticação
 * Segue o princípio da Responsabilidade Única (SRP) do SOLID
 */
public interface IServicoAutenticacao {
    
    /**
     * Registra um novo usuário no sistema
     * @param requisicao Dados para registro do usuário
     * @return Resposta com token de autenticação
     */
    AuthenticationResponse registrar(RegisterRequest requisicao);
    
    /**
     * Autentica um usuário existente
     * @param requisicao Dados para autenticação
     * @return Resposta com token de autenticação
     */
    AuthenticationResponse autenticar(AuthenticationRequest requisicao);
    
    /**
     * Valida token de autenticação
     * @param token Token JWT para validação
     * @return True se token válido, false caso contrário
     */
    boolean validarToken(String token);
    
    /**
     * Renova token de autenticação
     * @param tokenExpirado Token expirado para renovação
     * @return Novo token de autenticação
     */
    String renovarToken(String tokenExpirado);
} 