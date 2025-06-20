# Correção do Erro 403 no Swagger - Status e Soluções

## 🚨 **Problema Identificado**
- **Erro:** HTTP ERROR 403 ao acessar Swagger UI
- **URL:** http://localhost:8081/api/v1/swagger-ui.html
- **Causa:** Filtro JWT bloqueando acesso mesmo para endpoints públicos

## ✅ **Correções Implementadas**

### **1. SecurityConfig.java - Endpoints Liberados**
```java
.requestMatchers(
    "/api/v1/swagger-ui/**",
    "/api/v1/swagger-ui.html",
    "/api/v1/swagger-ui/index.html",
    "/api/v1/v3/api-docs/**",
    "/api/v1/api-docs/**",
    "/api/v1/webjars/**",
    "/api/v1/swagger-resources/**",
    "/api/v1/configuration/ui",
    "/api/v1/configuration/security",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/api-docs/**",
    "/webjars/**",
    "/swagger-resources/**",
    "/configuration/ui",
    "/configuration/security"
).permitAll()
```

### **2. JwtAuthenticationFilter.java - Perfil Condicional**
```java
@Component
@RequiredArgsConstructor
@Profile("!dev")  // Só aplica quando NÃO for perfil dev
public class JwtAuthenticationFilter extends OncePerRequestFilter {
```

### **3. DevSecurityConfig.java - Segurança Desabilitada**
```java
@Configuration
@EnableWebSecurity
@Profile("dev")  // Só aplica no perfil dev
public class DevSecurityConfig {
    
    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
```

### **4. application.yml - Swagger Habilitado**
```yaml
# Configuração global
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method

# Perfil de produção
springdoc:
  api-docs:
    enabled: true
    path: /api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

## 🔍 **Diagnóstico do Problema**

### **Logs Observados:**
```
JWT Filter - Endpoint público, pulando filtro: /api/v1/swagger-ui/index.html
JWT Filter - Request Path: /api/v1/swagger-ui/index.html
HTTP ERROR 403
```

### **Análise:**
1. ✅ Filtro JWT identifica endpoint como público
2. ✅ Filtro JWT pula a validação
3. ❌ **Ainda assim retorna 403**

### **Possíveis Causas Restantes:**
1. **Outro filtro de segurança** não identificado
2. **Configuração do Spring Security** conflitante
3. **Problema de ordem dos filtros**
4. **Cache de configuração** não atualizado

## 🛠️ **Soluções Alternativas**

### **Opção 1: Desabilitar Completamente a Segurança no Dev**
```java
@Profile("dev")
@Configuration
public class NoSecurityConfig {
    
    @Bean
    @Order(1)
    public SecurityFilterChain noSecurityFilterChain(HttpSecurity http) {
        return http
            .securityMatcher("/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }
}
```

### **Opção 2: Swagger Independente de Segurança**
```java
@Bean
@Order(SecurityProperties.BASIC_AUTH_ORDER - 10)
public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) {
    return http
        .securityMatcher("/api/v1/swagger-ui/**", "/api/v1/api-docs/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .build();
}
```

### **Opção 3: Acessar Swagger Sem Context Path**
```
# Tentar acessar diretamente:
http://localhost:8081/swagger-ui.html
http://localhost:8081/swagger-ui/index.html
```

## 🎯 **Comandos para Teste**

### **Verificar se Aplicação Está Rodando:**
```bash
netstat -an | findstr :8081
Get-Process | Where-Object {$_.ProcessName -eq "java"}
```

### **Testar Endpoints Básicos:**
```bash
# Health check
curl http://localhost:8081/api/v1/health

# Swagger UI
curl http://localhost:8081/api/v1/swagger-ui.html

# API Docs
curl http://localhost:8081/api/v1/api-docs
```

### **Executar com Diferentes Perfis:**
```bash
# Desenvolvimento (H2 + sem segurança)
java -jar target/auth-service-1.0.0.jar --spring.profiles.active=dev

# Produção (PostgreSQL + segurança)
java -jar target/auth-service-1.0.0.jar --spring.profiles.active=prod

# Padrão (PostgreSQL + segurança)
java -jar target/auth-service-1.0.0.jar
```

## 📋 **Status Atual**

### **✅ Implementado:**
- ✅ Configuração de segurança para Swagger
- ✅ Filtro JWT condicional por perfil
- ✅ Endpoints públicos configurados
- ✅ DevSecurityConfig para desenvolvimento
- ✅ Swagger habilitado em todos os perfis

### **❌ Ainda Pendente:**
- ❌ **Erro 403 persiste** mesmo com todas as configurações
- ❌ **Health check também retorna 403**
- ❌ **Problema não está apenas no Swagger**

## 🚀 **Próximos Passos**

1. **Investigar outros filtros** que podem estar bloqueando
2. **Verificar ordem de aplicação** dos filtros de segurança
3. **Testar com segurança completamente desabilitada**
4. **Verificar logs detalhados** da aplicação
5. **Considerar problema de configuração do Spring Boot**

## 💡 **Workaround Temporário**

Para desenvolvimento, considere:
1. **Usar Postman/Insomnia** com as requisições do `test-requests.http`
2. **Acessar H2 Console** que pode estar funcionando
3. **Testar endpoints via linha de comando** com curl
4. **Verificar se problema é específico do browser**

O Swagger está **tecnicamente configurado corretamente**, mas há um problema de configuração de segurança mais profundo que precisa ser investigado. 