package br.com.auth.service;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * Serviço responsável pela autenticação multifator (MFA) baseada em TOTP.
 * Gerencia geração de segredos, QR codes e validação de códigos TOTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoMFA {

    private final RepositorioUsuario repositorioUsuario;

    private static final String EMISSOR = "AuthSystem";
    private static final int TAMANHO_SEGREDO = 32;

    /**
     * Gera um novo segredo TOTP
     */
    public String gerarSegredo() {
        SecretGenerator gerador = new DefaultSecretGenerator(TAMANHO_SEGREDO);
        return gerador.generate();
    }

    /**
     * Gera a URI otpauth:// para configuração do aplicativo autenticador
     */
    public String gerarQrCodeUri(String segredo, String email) {
        QrData dados = new QrData.Builder()
                .label(email)
                .secret(segredo)
                .issuer(EMISSOR)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        return dados.getUri();
    }

    /**
     * Gera o QR code como imagem PNG em formato Base64 (data URI)
     */
    public String gerarQrCodeBase64(String segredo, String email) {
        try {
            QrData dados = new QrData.Builder()
                    .label(email)
                    .secret(segredo)
                    .issuer(EMISSOR)
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            QrGenerator gerador = new ZxingPngQrGenerator();
            byte[] imagemBytes = gerador.generate(dados);

            return getDataUriForImage(imagemBytes, gerador.getImageMimeType());
        } catch (QrGenerationException e) {
            log.error("Erro ao gerar QR code para MFA: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar QR code para MFA", e);
        }
    }

    /**
     * Valida um código TOTP contra o segredo fornecido
     */
    public boolean validarCodigo(String segredo, String codigo) {
        TimeProvider provedor = new SystemTimeProvider();
        CodeGenerator gerador = new DefaultCodeGenerator();
        DefaultCodeVerifier verificador = new DefaultCodeVerifier(gerador, provedor);
        // Permitir ±1 período (30s antes/depois) para compensar dessincronização de relógio
        verificador.setTimePeriod(30);
        verificador.setAllowedTimePeriodDiscrepancy(2);

        return verificador.isValidCode(segredo, codigo);
    }

    /**
     * Inicia a configuração do MFA para um usuário.
     * Gera o segredo e salva no usuário (sem habilitar ainda).
     * Retorna o QR code em Base64.
     */
    @Transactional
    public ResultadoConfiguracaoMFA habilitarMFA(Usuario usuario) {
        String segredo = gerarSegredo();
        String qrCodeBase64 = gerarQrCodeBase64(segredo, usuario.getEmail());

        // Salvar segredo no usuário (MFA ainda não está habilitado até verificação)
        usuario.setSegredoDoisFatores(segredo);
        repositorioUsuario.save(usuario);

        log.info("Configuração MFA iniciada para usuário: {}", usuario.getEmail());

        return new ResultadoConfiguracaoMFA(segredo, qrCodeBase64);
    }

    /**
     * Verifica o código TOTP e habilita o MFA para o usuário.
     * Só habilita se o código for válido (confirmando que o app autenticador está configurado).
     */
    @Transactional
    public boolean verificarMFA(Usuario usuario, String codigo) {
        if (usuario.getSegredoDoisFatores() == null || usuario.getSegredoDoisFatores().isEmpty()) {
            log.warn("Tentativa de verificar MFA sem segredo configurado para: {}", usuario.getEmail());
            return false;
        }

        boolean codigoValido = validarCodigo(usuario.getSegredoDoisFatores(), codigo);

        if (codigoValido) {
            usuario.setAutenticacaoDoisFatoresHabilitada(true);
            repositorioUsuario.save(usuario);
            log.info("MFA habilitado com sucesso para usuário: {}", usuario.getEmail());
        } else {
            log.warn("Codigo MFA invalido para usuario: {}", usuario.getEmail());
        }

        return codigoValido;
    }

    /**
     * Record para encapsular o resultado da configuração MFA
     */
    public record ResultadoConfiguracaoMFA(String segredo, String qrCodeBase64) {}
}
