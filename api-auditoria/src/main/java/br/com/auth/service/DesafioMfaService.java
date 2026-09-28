package br.com.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Desafios MFA de uso único emitidos após a validação da senha.
 * Garante que /api/v1/mfa/validar só emita JWT para quem passou pelo primeiro fator
 * no mesmo fluxo de login, com validade curta e número limitado de tentativas.
 */
@Slf4j
@Service
public class DesafioMfaService {

    static final long VALIDADE_SEGUNDOS = 300;
    static final int MAX_TENTATIVAS = 5;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Desafio> desafios = new ConcurrentHashMap<>();

    private static final class Desafio {
        final String email;
        final Instant expiraEm;
        int tentativas;

        Desafio(String email, Instant expiraEm) {
            this.email = email;
            this.expiraEm = expiraEm;
        }
    }

    public enum Resultado { VALIDO, INVALIDO }

    /** Emite um desafio para o usuário que acabou de validar a senha. */
    public String emitir(String email) {
        removerExpirados();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String id = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        desafios.put(id, new Desafio(email.toLowerCase(), Instant.now().plusSeconds(VALIDADE_SEGUNDOS)));
        return id;
    }

    /** Confere se o desafio existe, não expirou, pertence ao e-mail e ainda tem tentativas. */
    public Resultado verificar(String id, String email) {
        if (id == null || email == null) return Resultado.INVALIDO;
        Desafio d = desafios.get(id);
        if (d == null) return Resultado.INVALIDO;
        if (Instant.now().isAfter(d.expiraEm) || !d.email.equals(email.toLowerCase())) {
            desafios.remove(id);
            return Resultado.INVALIDO;
        }
        return Resultado.VALIDO;
    }

    /** Registra um código TOTP errado; após o limite, o desafio é descartado. */
    public void registrarFalha(String id) {
        desafios.computeIfPresent(id, (k, d) -> ++d.tentativas >= MAX_TENTATIVAS ? null : d);
    }

    /** Consome o desafio após o sucesso (uso único). */
    public void consumir(String id) {
        desafios.remove(id);
    }

    private void removerExpirados() {
        Instant agora = Instant.now();
        desafios.entrySet().removeIf(e -> agora.isAfter(e.getValue().expiraEm));
    }
}
