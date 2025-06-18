package br.com.auth.service;

import br.com.auth.dto.AuthenticationRequest;
import br.com.auth.entity.AuditLog;
import br.com.auth.entity.User;
import br.com.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContextAnalysisService {

    private final AuditLogRepository auditLogRepository;

    public void analyzeContext(User user, AuthenticationRequest request) {
        // Verifica tentativas de login em horários incomuns
        if (isUnusualLoginTime()) {
            logSuspiciousActivity(user, "Tentativa de login em horário incomum", request);
        }

        // Verifica mudança de localização
        if (user.getLastLoginLocation() != null && 
            !user.getLastLoginLocation().equals(request.getLocation())) {
            logSuspiciousActivity(user, "Mudança de localização detectada", request);
        }

        // Verifica mudança de dispositivo
        if (user.getLastLoginDevice() != null && 
            !user.getLastLoginDevice().equals(request.getUserAgent())) {
            logSuspiciousActivity(user, "Mudança de dispositivo detectada", request);
        }

        // Verifica múltiplas tentativas de login em um curto período
        var recentLogins = auditLogRepository.findByUserAndCreatedAtBetween(
            user,
            LocalDateTime.now().minusMinutes(5),
            LocalDateTime.now(),
            PageRequest.of(0, 10)
        );

        if (recentLogins.getTotalElements() > 3) {
            logSuspiciousActivity(user, "Múltiplas tentativas de login em curto período", request);
        }
    }

    private boolean isUnusualLoginTime() {
        int currentHour = LocalDateTime.now().getHour();
        // Considera horário incomum entre 23h e 5h
        return currentHour >= 23 || currentHour < 5;
    }

    private void logSuspiciousActivity(User user, String reason, AuthenticationRequest request) {
        var auditLog = AuditLog.builder()
                .user(user)
                .eventType("SUSPICIOUS_ACTIVITY")
                .description(reason)
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .location(request.getLocation())
                .success(false)
                .failureReason(reason)
                .build();
        auditLogRepository.save(auditLog);
    }
} 