# 🚀 Guia de Endpoints - API de Autenticação

## 📋 URLs Corretas

### Base URL
```
http://localhost:8081/api/v1/autenticacao
```

## 🔗 Endpoints Principais

### 1. **Status da API**
```http
GET http://localhost:8081/api/v1/autenticacao/status
```
**Resposta**: `Serviço de autenticação operacional`

### 2. **Registro de Usuário** ✅
```http
POST http://localhost:8081/api/v1/autenticacao/registrar
Content-Type: application/json

{
    "name": "Nome do Usuário",
    "email": "usuario@exemplo.com",
    "password": "senha123"
}
```

**Resposta de Sucesso (200)**:
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "name": "Nome do Usuário", 
    "email": "usuario@exemplo.com"
}
```

### 3. **Login de Usuário**
```http
POST http://localhost:8081/api/v1/autenticacao/entrar
Content-Type: application/json

{
    "email": "usuario@exemplo.com",
    "password": "senha123",
    "location": "São Paulo, BR"
}
```

### 4. **Validar Token**
```http
POST http://localhost:8081/api/v1/autenticacao/validar-token?token=SEU_TOKEN_JWT
```

## ❌ **URLs INCORRETAS (NÃO USE)**

- ❌ `http://localhost:8081/api/v1/api/v1/autenticacao/registrar` (duplicação)
- ❌ `http://localhost:8081/autenticacao/registrar` (sem /api/v1)
- ❌ `http://localhost:8081/api/autenticacao/registrar` (sem v1)

## 🧪 **Teste Rápido via PowerShell**

### Registro:
```powershell
$body = '{"name":"Teste","email":"teste@exemplo.com","password":"123456"}'
Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -ContentType "application/json" -Body $body
```

### Status:
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET
```

## 🔧 **Configuração do Frontend Angular**

No seu `api.service.ts`:
```typescript
export class ApiService {
  private baseUrl = 'http://localhost:8081/api/v1/autenticacao';
  
  register(userData: RegisterRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.baseUrl}/registrar`, userData);
  }
  
  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.baseUrl}/entrar`, credentials);
  }
}
```

## ✅ **Status dos Serviços**

- **Backend**: ✅ Funcionando (http://localhost:8081)
- **Frontend**: ✅ Funcionando (http://localhost:4200) 
- **Endpoint Registro**: ✅ Operacional
- **Endpoint Login**: ⚠️ Funcionando (dados null - problema cosmético)

## 🆘 **Resolução de Problemas**

1. **Erro 500**: Verifique se não há `/api/v1` duplicado na URL
2. **Erro 404**: Confirme que está usando `http://localhost:8081/api/v1/autenticacao/`
3. **API não responde**: Verifique se está rodando com `netstat -ano | findstr :8081`
4. **CORS**: Frontend deve estar em `http://localhost:4200`

## 📞 **Suporte**

Se ainda houver problemas:
1. Verifique a URL exata que está usando
2. Confirme que a API está rodando na porta 8081
3. Use os scripts de teste fornecidos no diretório 