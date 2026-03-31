package br.com.auth.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        String origin = request.getHeader("Origin");
        
        // Permitir apenas origens específicas
        if (origin != null && (
                origin.equals("http://localhost:4200") ||
                origin.equals("http://localhost:3000") ||
                origin.equals("http://localhost:4202") ||
                origin.equals("http://127.0.0.1:4200") ||
                origin.equals("http://127.0.0.1:3000") ||
                origin.equals("http://127.0.0.1:4202") ||
                origin.equals("https://mestrado.vps7950.panel.icontainer.net") ||
                origin.equals("https://www.jgbtecnologia.com.br") ||
                origin.equals("https://joaoguedes.com.br") ||
                origin.equals("https://www.joaoguedes.com.br")
        )) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else if (origin == null) {
            // Para requisições não-CORS (como do Postman)
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:4202");
        }
        
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", 
                "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        response.setHeader("Access-Control-Allow-Headers", 
                "Authorization, Content-Type, X-Requested-With, Accept, Origin, " +
                "Access-Control-Request-Method, Access-Control-Request-Headers");
        response.setHeader("Access-Control-Expose-Headers", 
                "Authorization, Content-Type");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // Se for uma requisição OPTIONS (preflight), apenas responde com 200
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
} 