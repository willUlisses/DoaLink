# DoaLink — Frontend (React + TypeScript + Vite + Tailwind)

Interface web da plataforma **DoaLink**, responsável por conectar **Doadores**, **Receptores (ONGs)** e **Transportadores** para logística solidária de doações, integrada à API REST Spring Boot em `../doalink-api`.

## Pré-requisitos

- Node.js 22+ (testado com Node 24) e npm 10+
- API `doalink-api` em execução (padrão `http://localhost:8080`)

## Instalação e Configuração

1. Instale as dependências preservando o `package-lock.json`:

   ```bash
   cd doalink-view
   npm install
   ```

2. Crie o arquivo `.env` a partir do `.env.example` e ajuste a URL da API se necessário:

   ```bash
   cp .env.example .env
   ```

   Variável disponível:

   | Variável | Padrão | Descrição |
   |---|---|---|
   | `VITE_API_BASE_URL` | `http://localhost:8080` | URL base da API Spring Boot (sem barra final) |

> **Observação sobre CORS e cookies:** O servidor de desenvolvimento do Vite é executado na porta `3000` (`http://localhost:3000`) para alinhar com `app.cors.origins` e `app.recuperacao-senha.url-base` configurados na API. A sessão utiliza cookies `HttpOnly` (`withCredentials: true`), sem armazenamento de tokens em `localStorage` ou `sessionStorage`.

## Comandos Disponíveis

| Comando | Descrição |
|---|---|
| `npm run dev` | Inicia o servidor de desenvolvimento em `http://localhost:3000` |
| `npm run build` | Executa checagem de tipos (`tsc -b`) e gera build de produção em `dist/` |
| `npm run preview` | Pré-visualiza o build de produção |
| `npm run typecheck` | Executa verificação estrita de tipos (`tsc -b --noEmit`) |
| `npm run lint` | Executa análise estática com ESLint |
| `npm test` | Executa a suíte automatizada com Vitest e Testing Library |

## Estrutura de Pastas (`src/`)

```text
src/
├── components/
│   ├── feedback/   # Skeleton, EmptyState, ErrorState, InfoBanner, ConfirmDialog, ToastContainer
│   ├── layout/     # BrandLogo, Breadcrumbs, PublicLayout, AppLayout
│   └── ui/         # Button, Input, PasswordInput, Select, Textarea, Checkbox, Card, Badge, AddressFieldset, Pagination
├── contexts/       # AuthContext (sessão via cookies HttpOnly) e ToastContext
├── features/       # Cards e diálogos de domínio (ofertas, necessidades, campanhas, transporte, reservas)
├── hooks/          # useAuth, useToast, useAsyncResource, useDocumentTitle
├── pages/          # Páginas públicas, autenticação, erros e área autenticada (/app)
├── routes/         # RouteGuards (PrivateRoute, RoleRoute, PublicOnlyRoute) e router.tsx
├── schemas/        # Schemas Zod em português e testes unitários
├── services/       # Cliente Axios centralizado (api.ts) e serviços por domínio
├── test/           # Configuração do Vitest + Testing Library
├── types/          # Tipos e enums espelhando os DTOs reais da API
└── utils/          # Validadores (CPF/CNPJ/CEP), máscaras visuais, formatadores, rótulos e tokens de perfil
```

## Documentação Relacionada

- Contrato e divergências API × documentação: [docs/frontend-implementacao.md](../docs/frontend-implementacao.md)
- Arquitetura geral do sistema: [docs/arquitetura.md](../docs/arquitetura.md)
