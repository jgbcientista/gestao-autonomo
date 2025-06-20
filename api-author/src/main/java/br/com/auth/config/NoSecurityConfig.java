package br.com.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança que DESABILITA COMPLETAMENTE a autenticação
 * para ambiente de desenvolvimento
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
@Primary
public class NoSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain noSecurityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🚫 NO SECURITY CONFIG - DESABILITANDO TODA SEGURANÇA!");
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .headers(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> {
                System.out.println("🔓 PERMITINDO ABSOLUTAMENTE TUDO!");
                auth.anyRequest().permitAll();
            })
            .sessionManagement(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable);

        System.out.println("✅ SEGURANÇA COMPLETAMENTE DESABILITADA!");
        return http.build();
    }
} 