package br.com.auth.config;

import br.com.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        System.out.println("JWT Filter - Request Path: " + requestPath);
        System.out.println("JWT Filter - Active Profile: " + activeProfile);
        
        // Pular filtro para endpoints públicos
        if (isPublicEndpoint(requestPath)) {
            System.out.println("JWT Filter - Endpoint público, pulando filtro: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log do erro mas não bloquear o request
            System.err.println("Erro no JWT filter: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.startsWith("/api/v1/test") ||
               requestPath.startsWith("/api/v1/health") ||
               requestPath.startsWith("/api/v1/autenticacao/registrar") ||
               requestPath.startsWith("/api/v1/autenticacao/entrar") ||
               requestPath.startsWith("/api/v1/autenticacao/status") ||
               requestPath.startsWith("/api/v1/h2-console") ||
               requestPath.startsWith("/api/v1/swagger-ui") ||
               requestPath.contains("swagger-ui") ||
               requestPath.startsWith("/api/v1/v3/api-docs") ||
               requestPath.startsWith("/api/v1/api-docs") ||
               requestPath.contains("api-docs") ||
               requestPath.startsWith("/api/v1/webjars") ||
               requestPath.contains("webjars") ||
               requestPath.startsWith("/api/v1/swagger-resources") ||
               requestPath.contains("swagger-resources") ||
               requestPath.startsWith("/api/v1/configuration") ||
               requestPath.contains("configuration") ||
               requestPath.startsWith("/api/v1/actuator") ||
               requestPath.equals("/api/v1/swagger-ui.html") ||
               requestPath.equals("/swagger-ui.html") ||
               requestPath.equals("/api/v1/swagger-ui/index.html") ||
               requestPath.equals("/swagger-ui/index.html");
    }
} 