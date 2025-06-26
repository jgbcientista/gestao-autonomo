package br.com.auth.service;

import br.com.auth.dominio.entidades.SessaoAtiva;
import br.com.auth.dominio.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void notificarNovaSessao(SessaoAtiva sessao) {
        try {
            Usuario usuario = sessao.getUsuario();
            String to = usuario.getEmail();

            Context context = new Context(new Locale("pt", "BR"));
            Map<String, Object> variables = new HashMap<>();
            variables.put("nome", usuario.getName());
            variables.put("data", sessao.getLoginTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            variables.put("dispositivo", sessao.getDeviceInfo());
            variables.put("navegador", sessao.getBrowserInfo());
            variables.put("localizacao", sessao.getLocation());
            variables.put("ip", sessao.getIpAddress());
            variables.put("baseUrl", baseUrl);
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/nova-sessao", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Nova sessão detectada em sua conta");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de notificação enviado para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de notificação: {}", e.getMessage());
        }
    }

    @Async
    public void notificarSessaoSuspeita(SessaoAtiva sessao) {
        try {
            Usuario usuario = sessao.getUsuario();
            String to = usuario.getEmail();

            Context context = new Context(new Locale("pt", "BR"));
            Map<String, Object> variables = new HashMap<>();
            variables.put("nome", usuario.getName());
            variables.put("data", sessao.getLoginTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            variables.put("dispositivo", sessao.getDeviceInfo());
            variables.put("navegador", sessao.getBrowserInfo());
            variables.put("localizacao", sessao.getLocation());
            variables.put("ip", sessao.getIpAddress());
            variables.put("riskLevel", sessao.getRiskLevel());
            variables.put("baseUrl", baseUrl);
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/sessao-suspeita", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("⚠️ Atividade suspeita detectada em sua conta");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de alerta enviado para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de alerta: {}", e.getMessage());
        }
    }
} 