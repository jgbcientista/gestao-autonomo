package br.com.auth.service;

import br.com.auth.dto.AuthenticationRequest;
import br.com.auth.dto.AuthenticationResponse;
import br.com.auth.dto.RegisterRequest;
import br.com.auth.entity.AuditLog;
import br.com.auth.entity.User;
import br.com.auth.repository.AuditLogRepository;
import br.com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogRepository auditLogRepository;
    private final ContextAnalysisService contextAnalysisService;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Define roles padrão se não fornecidas
        Set<String> userRoles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            userRoles.addAll(request.getRoles());
        } else {
            userRoles.add("USER_DEFAULT");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(userRoles)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .twoFactorEnabled(false)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .name(user.getName())
                .login(user.getEmail())
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verifica se a conta está bloqueada
        if (Boolean.TRUE.equals(user.getAccountLocked()) && user.getAccountLockedUntil() != null 
            && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Conta bloqueada. Tente novamente mais tarde.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Análise de contexto
            contextAnalysisService.analyzeContext(user, request);

            // Atualiza informações de login
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(request.getIpAddress());
            user.setLastLoginLocation(request.getLocation());
            user.setLastLoginDevice(request.getUserAgent());
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            userRepository.save(user);

            // Registra o log de auditoria
            var auditLog = AuditLog.builder()
                    .user(user)
                    .eventType("LOGIN_SUCCESS")
                    .description("Login realizado com sucesso")
                    .ipAddress(request.getIpAddress())
                    .userAgent(request.getUserAgent())
                    .location(request.getLocation())
                    .success(true)
                    .build();
            auditLogRepository.save(auditLog);

            var jwtToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .name(user.getName())
                    .login(user.getEmail())
                    .build();

        } catch (Exception e) {
            // Incrementa tentativas de login
            int currentAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
            user.setFailedLoginAttempts(currentAttempts + 1);
            
            // Bloqueia a conta após 5 tentativas
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLocked(true);
                user.setAccountLockedUntil(LocalDateTime.now().plusHours(1));
            }
            
            userRepository.save(user);

            // Registra o log de auditoria
            var auditLog = AuditLog.builder()
                    .user(user)
                    .eventType("LOGIN_FAILED")
                    .description("Falha no login: " + e.getMessage())
                    .ipAddress(request.getIpAddress())
                    .userAgent(request.getUserAgent())
                    .location(request.getLocation())
                    .success(false)
                    .failureReason(e.getMessage())
                    .build();
            auditLogRepository.save(auditLog);

            throw new RuntimeException("Credenciais inválidas");
        }
    }
} 