package br.com.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Autenticação Inteligente")
                        .description("""
                            API do Sistema de Autenticação com Análise Comportamental e Score de Confiança
                            
                            ## Funcionalidades Principais:
                            - 🔐 Autenticação JWT tradicional
                            - 🤖 Análise comportamental com IA (3 algoritmos)
                            - 📊 Score de confiança dinâmico (0.0 a 1.0)
                            - 🛡️ Decisões automáticas (PERMITIR/MFA/BLOQUEAR)
                            - 🌍 Análise de geolocalização
                            - 📈 Métricas e estatísticas em tempo real
                            - 🔗 Integração com blockchain para auditoria
                            
                            ## Algoritmos de IA:
                            - **Isolation Forest**: Detecção de anomalias
                            - **Random Forest**: Classificação de padrões
                            - **Deep Learning**: Análise comportamental avançada
                            
                            ## Níveis de Confiança:
                            - **MUITO_ALTO** (0.8-1.0): Acesso direto
                            - **ALTO** (0.6-0.8): Acesso direto
                            - **MÉDIO** (0.4-0.6): Contextual
                            - **BAIXO** (0.2-0.4): Requer MFA
                            - **MUITO_BAIXO** (0.0-0.2): Bloqueado
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@authsystem.com")
                                .url("https://github.com/authsystem"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.authsystem.com")
                                .description("Servidor de Produção")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtido através do endpoint de autenticação")));
    }
} 