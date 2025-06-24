package br.com.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable()) // Desabilitar CORS do Spring Security (usando filtro personalizado)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()) // Permite H2 console
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/health",
                    "/auth/register",
                    "/auth/login",
                    "/api/v1/test/**",
                    "/api/v1/health/**", 
                    "/api/v1/autenticacao/**",
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/h2-console/**",
                    "/api/v1/swagger-ui/**",
                    "/api/v1/swagger-ui.html",
                    "/api/v1/swagger-ui/index.html",
                    "/api/v1/v3/api-docs/**",
                    "/api/v1/api-docs/**",
                    "/api/v1/webjars/**",
                    "/api/v1/swagger-resources/**",
                    "/api/v1/configuration/ui",
                    "/api/v1/configuration/security",
                    "/api/v1/actuator/**",
                    "/api/blockchain/test/**",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/configuration/ui",
                    "/configuration/security"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // IMPORTANTE: Não usar "*" com setAllowCredentials(true)
        // Permitir origens específicas apenas
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",    // Angular dev server
            "http://localhost:3000",    // React/Node dev server  
            "http://127.0.0.1:4200",
            "http://127.0.0.1:3000"
        ));
        
        // Permitir métodos HTTP específicos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // Permitir headers específicos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Permitir credenciais (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Expor headers importantes para o frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type"
        ));
        
        // Tempo de cache para requisições OPTIONS (preflight)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 