# Frontend Auditoria - Sistema de Autenticação Inteligente

Este é o frontend Angular do Sistema de Autenticação Inteligente, que se integra com a API `api-auditoria` para fornecer uma interface moderna e responsiva para autenticação de usuários.

## 🚀 Tecnologias Utilizadas

- **Angular 18** - Framework principal
- **Bootstrap 5** - Framework CSS para UI responsiva
- **Bootstrap Icons** - Ícones para interface
- **TypeScript** - Linguagem de programação
- **SCSS** - Pré-processador CSS
- **RxJS** - Programação reativa

## 📋 Funcionalidades Implementadas

### ✅ Telas Principais
- **Login**: Tela de autenticação com validação de formulário
- **Registro**: Tela de cadastro de novos usuários
- **Dashboard**: Painel principal após autenticação

### ✅ Recursos Implementados
- Formulários reativos com validação
- Integração completa com API REST
- Gerenciamento de estado de autenticação
- Armazenamento seguro de tokens JWT
- Tratamento de erros HTTP
- Interface responsiva com Bootstrap
- Navegação por rotas
- Guard de autenticação (em desenvolvimento)

### ✅ Serviços
- **AuthService**: Gerenciamento de autenticação e sessão
- **ApiService**: Comunicação com a API backend
- **Modelos tipados**: Interfaces TypeScript para dados

## 🔧 Como Executar o Projeto

### Pré-requisitos
- Node.js (versão 18+ recomendada)
- npm ou yarn
- API `api-auditoria` rodando na porta 8081

### Passos para execução

1. **Instalar dependências**:
```bash
npm install
```

2. **Executar em modo de desenvolvimento**:
```bash
ng serve
```

3. **Acessar a aplicação**:
- URL: `http://localhost:4200`
- A aplicação redirecionará automaticamente para `/login`

## 🌐 Integração com API

### Endpoints Utilizados
- `POST /api/v1/autenticacao/entrar` - Login de usuário
- `POST /api/v1/autenticacao/registrar` - Registro de usuário
- `POST /api/v1/autenticacao/validar-token` - Validação de token
- `GET /api/v1/autenticacao/status` - Status do sistema

### Configuração da API
- **URL Base**: `http://localhost:8081/api/v1`
- **Autenticação**: JWT Token via Bearer Token
- **Armazenamento**: LocalStorage para token e dados do usuário

## 📱 Telas e Navegação

### Roteamento
- `/` → Redireciona para `/login`
- `/login` → Tela de login
- `/register` → Tela de registro
- `/dashboard` → Dashboard (requer autenticação)
- `/**` → Redireciona para `/login`

### Fluxo de Autenticação
1. Usuário acessa a aplicação
2. Redireciona para tela de login
3. Após login bem-sucedido, redireciona para dashboard
4. Token JWT é armazenado e usado em requisições subsequentes
5. Logout limpa token e redireciona para login

## 🔒 Segurança

- Tokens JWT armazenados em LocalStorage
- Validação de formulários no frontend
- Tratamento de diferentes códigos de erro HTTP
- Enriquecimento de contexto (IP, User-Agent, localização)
- Proteção contra ataques comuns (preparado para guards)

## 🎨 Interface e UX

- Design responsivo com Bootstrap 5
- Cards com hover effects
- Loading states em botões
- Mensagens de erro e sucesso
- Validação visual de formulários
- Navbar dinâmica no dashboard
- Ícones informativos

## 📁 Estrutura do Projeto

```
src/
├── app/
│   ├── components/
│   │   ├── login/
│   │   ├── register/
│   │   └── dashboard/
│   ├── services/
│   │   ├── auth.service.ts
│   │   └── api.service.ts
│   ├── models/
│   │   └── auth.model.ts
│   ├── app.routes.ts
│   ├── app.config.ts
│   └── app.component.*
├── styles.scss
└── index.html
```

## 🛠️ Scripts Disponíveis

- `ng serve` - Desenvolvimento
- `ng build` - Build de produção
- `ng test` - Testes unitários
- `ng lint` - Linting do código

## 🔄 Próximas Implementações

- [ ] Guard de autenticação para rotas
- [ ] Interceptor HTTP para token automático
- [ ] Refresh token automático
- [ ] Perfil do usuário
- [ ] Configurações de conta
- [ ] Logs de atividade
- [ ] Tema escuro/claro

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique se a API está rodando na porta 8081
2. Confirme as dependências instaladas
3. Verifique o console do navegador para erros
4. Confirme a conectividade com o backend

## 🏗️ Arquitetura

Este projeto segue os princípios SOLID e utiliza:
- **Componentes standalone** (Angular 17+)
- **Injeção de dependências**
- **Observables para programação reativa**
- **Tipagem forte com TypeScript**
- **Separação de responsabilidades**
- **Reutilização de código**
