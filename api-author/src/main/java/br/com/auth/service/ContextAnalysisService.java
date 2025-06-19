package br.com.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dto.RequisicaoAutenticacao;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContextAnalysisService {

    private final RepositorioLogAuditoria repositorioLogAuditoria;

    public void analyzeContext(Usuario usuario, RequisicaoAutenticacao request) {
        // Verifica tentativas de login em horários incomuns
        if (isUnusualLoginTime()) {
            logSuspiciousActivity(usuario, "Tentativa de login em horário incomum", request);
        }

        // Verifica mudança de localização
        if (usuario.getUltimoLoginLocalizacao() != null && 
            !usuario.getUltimoLoginLocalizacao().equals(request.getLocation())) {
            logSuspiciousActivity(usuario, "Mudança de localização detectada", request);
        }

        // Verifica mudança de dispositivo
        if (usuario.getUltimoLoginDispositivo() != null && 
            !usuario.getUltimoLoginDispositivo().equals(request.getUserAgent())) {
            logSuspiciousActivity(usuario, "Mudança de dispositivo detectada", request);
        }

        // Verifica múltiplas tentativas de login em um curto período
        List<LogAuditoria> recentLogins = repositorioLogAuditoria.findByUsuarioAndCriadoEmBetween(
            usuario,
            LocalDateTime.now().minusMinutes(5),
            LocalDateTime.now()
        );

        if (recentLogins.size() > 3) {
            logSuspiciousActivity(usuario, "Múltiplas tentativas de login em curto período", request);
        }
    }

    private boolean isUnusualLoginTime() {
        int currentHour = LocalDateTime.now().getHour();
        // Considera horário incomum entre 23h e 5h
        return currentHour >= 23 || currentHour < 5;
    }

    private void logSuspiciousActivity(Usuario usuario, String reason, RequisicaoAutenticacao request) {
        var logAuditoria = LogAuditoria.builder()
                .usuario(usuario)
                .tipoEvento("SUSPICIOUS_ACTIVITY")
                .descricao(reason)
                .enderecoIp(request.getIpAddress())
                .agenteUsuario(request.getUserAgent())
                .localizacao(request.getLocation())
                .sucesso(false)
                .motivoFalha(reason)
                .build();
        repositorioLogAuditoria.save(logAuditoria);
    }
} 