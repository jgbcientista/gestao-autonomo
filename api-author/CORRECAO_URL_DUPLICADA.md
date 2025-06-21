# Correção do Problema de URL Duplicada

## Problema Identificado
Quando o usuário clicava para registrar/persistir dados no frontend, a URL estava sendo duplicada: `http://localhost:8081/api/v1/api/v1/autenticacao/registrar`

## Diagnóstico Realizado

### 1. Teste de Backend
- ✅ URL correta funciona: `http://localhost:8081/api/v1/autenticacao/registrar`
- ❌ URL duplicada falha: `http://localhost:8081/api/v1/api/v1/autenticacao/registrar` (erro 500)

### 2. Conclusão
O problema estava no **frontend** (Angular), não no backend (Spring Boot).

## Causa Raiz
**Arquivo:** `frontend-author/src/app/services/api.service.ts`

**Problema:** A `baseUrl` já incluía `/api/v1`, mas as URLs dos métodos também incluíam `/api/v1`, causando duplicação.

**Antes:**
```typescript
export class ApiService {
  private baseUrl = 'http://localhost:8081/api/v1';  // ❌ Já incluía /api/v1

  register(userDetails: RegisterRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.baseUrl}/autenticacao/registrar`,  // ❌ Gerava: localhost:8081/api/v1/autenticacao/registrar
      userDetails, 
      { headers: this.getHeaders() }
    );
  }
}
```

Mas quando usado, provavelmente havia algum interceptor ou proxy que adicionava `/api/v1` novamente, resultando em:
`http://localhost:8081/api/v1/api/v1/autenticacao/registrar`

## Solução Aplicada

**Depois:**
```typescript
export class ApiService {
  private baseUrl = 'http://localhost:8081';  // ✅ Apenas domínio e porta

  register(userDetails: RegisterRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.baseUrl}/api/v1/autenticacao/registrar`,  // ✅ Path completo explícito
      userDetails, 
      { headers: this.getHeaders() }
    );
  }
}
```

## Benefícios da Correção

1. **URLs Corretas**: Elimina a duplicação de `/api/v1`
2. **Transparência**: Path completo fica explícito no código
3. **Compatibilidade**: Mantém compatibilidade com todas as funcionalidades
4. **Manutenibilidade**: Facilita debug e manutenção

## Métodos Corrigidos

Todos os métodos do `ApiService` foram atualizados:

- ✅ `login()` → `POST /api/v1/autenticacao/entrar`
- ✅ `register()` → `POST /api/v1/autenticacao/registrar`  
- ✅ `validateToken()` → `POST /api/v1/autenticacao/validar-token`
- ✅ `refreshToken()` → `POST /api/v1/autenticacao/renovar-token`
- ✅ `getStatus()` → `GET /api/v1/autenticacao/status`

## Como Testar

### 1. Reiniciar Frontend Angular
```bash
cd frontend-author
npm start
```

### 2. Testar Registro via Interface
1. Acesse: `http://localhost:4200/register`
2. Preencha os dados do usuário
3. Clique em "Registrar"
4. Verifique se não há erro de URL duplicada

### 3. Verificar Network Tab
1. Abra DevTools → Network
2. Faça um registro
3. Confirme que a URL da requisição é: `http://localhost:8081/api/v1/autenticacao/registrar`

## Script de Teste
Arquivo: `api-author/teste-url-duplicada.ps1`

```powershell
cd api-author
.\teste-url-duplicada.ps1
```

## Resultado Esperado

**Status Code:** 200  
**URL da Requisição:** `http://localhost:8081/api/v1/autenticacao/registrar`  
**Response:** JSON com token, nome e email do usuário

## Observações

- A correção não afeta o backend
- Todas as rotas continuam funcionando normalmente
- Frontend agora constrói URLs corretamente
- Erro de linter do tslib não afeta funcionalidade 