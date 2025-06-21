# 🔧 Solução do Problema do Endpoint de Login

## 📋 Problema Relatado
- **URL com duplicação**: `http://localhost:8081/api/v1/api/v1/autenticacao/entrar`
- **Dados de teste**: 
  ```json
  {
    "email": "joaoguedes@gmail.com",
    "password": "1234567890"
  }
  ```
- **Status**: Endpoint não está funcionando

## 🔍 Análise do Problema

### 1. **Problema Identificado na URL**
- **URL INCORRETA**: `http://localhost:8081/api/v1/api/v1/autenticacao/entrar` (duplicação `/api/v1`)
- **URL CORRETA**: `http://localhost:8081/api/v1/autenticacao/entrar`

### 2. **Configuração do Controlador** ✅
O controlador está correto:
```java
@RestController
@RequestMapping("/api/v1/autenticacao")
public class AutenticacaoController {
    
    @PostMapping("/entrar")
    public ResponseEntity<AuthenticationResponse> entrar(
            @RequestBody AuthenticationRequest requisicao,
            HttpServletRequest request) {
        // Implementação correta...
    }
}
```

### 3. **Configuração da Aplicação** ✅
O `application.yml` está configurado corretamente:
```yaml
server:
  port: 8081
```

## 🚀 Solução Implementada

### 1. **URLs Corretas para Usar**

#### **Login/Autenticação:**
```bash
POST http://localhost:8081/api/v1/autenticacao/entrar
Content-Type: application/json

{
    "email": "joaoguedes@gmail.com",
    "password": "1234567890",
    "location": "São Paulo, BR"
}
```

#### **Registro (se necessário):**
```bash
POST http://localhost:8081/api/v1/autenticacao/registrar
Content-Type: application/json

{
    "name": "João Guedes",
    "email": "joaoguedes@gmail.com",
    "password": "1234567890"
}
```

#### **Status da API:**
```bash
GET http://localhost:8081/api/v1/autenticacao/status
```

### 2. **Script de Teste PowerShell**

Criado script `teste-endpoint-login.ps1` para validar o funcionamento:

```powershell
# 1. Testar status
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET

# 2. Registrar usuário (se necessário)
$userData = @{
    name = "João Guedes"
    email = "joaoguedes@gmail.com"
    password = "1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -Body $userData -ContentType "application/json"

# 3. Fazer login
$loginData = @{
    email = "joaoguedes@gmail.com"
    password = "1234567890"
    location = "São Paulo, BR"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/entrar" -Method POST -Body $loginData -ContentType "application/json"
```

## 🎯 Como Executar a Solução

### 1. **Iniciar o Servidor**
```bash
cd api-author
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 2. **Aguardar Inicialização**
- Aguarde o servidor inicializar completamente
- Verifique se a porta 8081 está sendo usada: `netstat -an | findstr :8081`

### 3. **Testar os Endpoints**

#### **Via PowerShell:**

```powershell
# Status
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET

# Login
$loginData = '{"email":"joaoguedes@gmail.com","password":"1234567890","location":"São Paulo, BR"}'
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/entrar" -Method POST -Body $loginData -ContentType "application/json"
```

#### **Via cURL (se disponível):**
```bash
# Status
curl http://localhost:8081/api/v1/autenticacao/status

# Login
curl -X POST http://localhost:8081/api/v1/autenticacao/entrar \
  -H "Content-Type: application/json" \
  -d '{"email":"joaoguedes@gmail.com","password":"1234567890","location":"São Paulo, BR"}'
```

## 🔧 Correções no Frontend (Se Aplicável)

Se o problema estiver no frontend Angular, corrigir o `api.service.ts`:

```typescript
export class ApiService {
  private baseUrl = 'http://localhost:8081'; // ✅ SEM /api/v1

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.baseUrl}/api/v1/autenticacao/entrar`, // ✅ URL completa
      credentials
    );
  }
}
```

## 📝 Resposta Esperada

### **Sucesso (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "name": "João Guedes",
  "email": "joaoguedes@gmail.com"
}
```

### **Erro de Credenciais (401):**
```json
{
  "message": "Credenciais inválidas"
}
```

### **Usuário não encontrado (404):**
```json
{
  "message": "Usuário não encontrado"
}
```

## ⚠️ Problemas Comuns

### 1. **Servidor não iniciando**
- Verificar se há outros processos usando a porta 8081
- Verificar configuração do banco de dados (H2 no perfil dev)
- Verificar dependências do Maven

### 2. **URL duplicada no frontend**
- Verificar configuração do `baseUrl` no serviço Angular
- Verificar se não há interceptors duplicando o path

### 3. **Erro 500**
- Verificar logs do servidor
- Verificar se o usuário existe no banco de dados
- Verificar configuração de segurança

## 📚 Documentação de Referência

- [Guia de Endpoints Corretos](GUIA_ENDPOINTS_CORRETOS.md)
- [Correção URL Duplicada](CORRECAO_URL_DUPLICADA.md)
- [Guia de Testes](GUIA_DE_TESTES.md) 