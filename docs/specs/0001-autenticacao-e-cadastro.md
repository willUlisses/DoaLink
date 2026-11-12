# Spec 0001: Autenticação, Cadastro de Usuários e Gestão de Sessão

- **Status:** Implementado
- **Decisões:** [ADR 0004](../adr/0004-cadastro-por-perfil-em-endpoints-separados-sob-auth.md), [ADR 0005](../adr/0005-jwt-em-cookies-httponly-com-refresh-token-opaco-e-rotacao.md)

---

## 1. Visão Geral

Esta especificação define o contrato da API REST para registro de participantes por perfil (`Doador`, `Transportador` e `Receptor`), login, rotação de refresh token e encerramento de sessão (logout).

Todo o controle de sessão é realizado de forma stateless através de **cookies HttpOnly**, garantindo imunidade a ataques XSS e proteção CSRF com `SameSite=Strict`.

---

## 2. Entidades do Domínio

### `Usuario` (abstrata, `@Inheritance(strategy = JOINED)`)
- `id`: Long (PK)
- `nome`: String (VARCHAR(150), não nulo)
- `email`: String (VARCHAR(254), único, não nulo, normalizado em minúsculas)
- `senha`: String (VARCHAR(255), hash BCrypt, nunca exposto)
- `tipoPessoa`: Enum `TipoPessoa` (`FISICA` ou `JURIDICA`)
- `tipoUsuario`: Enum `TipoUsuario` (`DOADOR`, `TRANSPORTADOR`, `RECEPTOR`, fixado no construtor da subclasse)
- `documento`: String (VARCHAR(14), apenas dígitos, único no banco)
- `emailVerificado`: boolean (BOOLEAN, não nulo, default `false`)

### Subclasses
- `Doador`: Herda de `Usuario`. `tipoUsuario = DOADOR`.
- `Transportador`: Herda de `Usuario`. `tipoUsuario = TRANSPORTADOR`.
- `Receptor`: Herda de `Usuario`. `tipoUsuario = RECEPTOR`. Possui `@Embedded Endereco endereco`.

### `CodigoVerificacaoEmail`
- `id`: Long (PK, BIGSERIAL)
- `usuario`: Relacionamento `@ManyToOne Usuario` (não nulo, FK com on delete cascade)
- `codigoHash`: String (VARCHAR(64), hash SHA-256 do código numérico de 6 dígitos)
- `expiracao`: Instant (TIMESTAMP WITH TIME ZONE, expiração em 90 segundos)
- `utilizado`: boolean (BOOLEAN, default `false`)
- `revogado`: boolean (BOOLEAN, default `false`)
- `criadoEm`: Instant (TIMESTAMP WITH TIME ZONE, default `NOW()`)

### `RefreshToken`
- `id`: Long (PK)
- `usuario`: Relacionamento `@ManyToOne Usuario`
- `tokenHash`: String (VARCHAR(64), hash SHA-256 do token opaco gerado)
- `dataExpiracao`: Instant (7 dias)
- `revogado`: boolean (default `false`)
- `criadoEm`: Instant

---

## 3. DTOs

### Requests (`record`)
| DTO | Campos | Validações Bean Validation |
|---|---|---|
| `CadastroComumRequest` | `nome`, `senha`, `email`, `cpf`, `cnpj` | `@NotBlank nome`, `@Email @NotBlank email`, `@Size(min=8, max=72) senha`, `@AssertTrue` garantindo exatamente um entre CPF ou CNPJ preenchido |
| `CadastroReceptorRequest` | `nome`, `senha`, `email`, `cnpj`, `endereco` | `@NotBlank nome`, `@Email @NotBlank email`, `@Size(min=8, max=72) senha`, `@CNPJ @NotBlank cnpj`, `@NotNull @Valid endereco` |
| `EnderecoRequest` | `cep`, `logradouro`, `numero`, `bairro`, `cidade`, `estado`, `complemento` | `@NotBlank` em todos (exceto complemento) |
| `LoginRequest` | `email`, `senha` | `@Email @NotBlank email`, `@NotBlank senha` |
| `VerificarEmailRequest` | `email`, `codigo` | `@Email @NotBlank email`, `@NotBlank @Pattern(regexp="^\\d{6}$") codigo` |
| `ReenviarCodigoRequest` | `email` | `@Email @NotBlank email` |

### Responses (`record`)
| DTO | Campos | Observações |
|---|---|---|
| `UsuarioResponse` | `id`, `nome`, `email`, `tipoUsuario` | Retornado no corpo das operações bem-sucedidas. Nunca expõe dados sensíveis. |
| `MensagemResponse` | `mensagem` | Retornado em operações informativas (ex: reenvio de código). |
| `ErrorResponse` | `status`, `message`, `timestamp` | Retornado em caso de exceções de validação ou regras de negócio. |

---

## 4. Endpoints (`AuthController`)

Todos os endpoints estão agrupados sob o prefixo `/auth`:

| Método | Rota | Entrada (Body / Cookie) | Saída (Body + Headers) | Sucesso | Erros Mapeados | Acesso |
|---|---|---|---|---|---|---|
| POST | `/auth/doadores/cadastro` | Body: `CadastroComumRequest` | `UsuarioResponse` | 201 Created | 400 (validação), 409 (e-mail/documento existente) | Público |
| POST | `/auth/transportadores/cadastro` | Body: `CadastroComumRequest` | `UsuarioResponse` | 201 Created | 400 (validação), 409 (e-mail/documento existente) | Público |
| POST | `/auth/receptores/cadastro` | Body: `CadastroReceptorRequest` | `UsuarioResponse` | 201 Created | 400 (validação), 409 (e-mail/documento existente) | Público |
| POST | `/auth/verificar-email` | Body: `VerificarEmailRequest` | `UsuarioResponse` + 2 cookies HttpOnly | 200 OK | 400 (código inválido/expirado/revogado), 404 (usuário não encontrado) | Público |
| POST | `/auth/reenviar-codigo` | Body: `ReenviarCodigoRequest` | `MensagemResponse` | 200 OK | 400 (validação) | Público |
| POST | `/auth/login` | Body: `LoginRequest` | `UsuarioResponse` + 2 cookies HttpOnly | 200 OK | 400 (validação), 401 (credenciais inválidas), 403 (e-mail não verificado) | Público |
| POST | `/auth/refresh` | Cookie: `refresh_token` | `UsuarioResponse` + 2 novos cookies HttpOnly | 200 OK | 401 (token ausente, inválido, expirado ou reutilizado), 403 (e-mail não verificado) | Público |
| POST | `/auth/logout` | Cookie: `refresh_token` | Vazio + Cookies limpos (`maxAge=0`) | 204 No Content | 204 (idempotente) | Público |

---

## 5. Regras de Segurança e Cookies

- **`access_token`:**
  - Path: `/`
  - MaxAge: 15 minutos (`app.jwt.access-minutes`)
  - Atributos: `HttpOnly`, `SameSite=Strict`, `Secure` (configurável via `app.cookie.secure`)
- **`refresh_token`:**
  - Path: `/auth`
  - MaxAge: 7 dias (`app.refresh.days`)
  - Atributos: `HttpOnly`, `SameSite=Strict`, `Secure`
- **Configuração no Spring Security (`SecurityConfig`):**
  - Rotas públicas declaradas expressamente sem curinga permissivo:
    - `/auth/login`, `/auth/refresh`, `/auth/logout`
    - `/auth/doadores/cadastro`, `/auth/transportadores/cadastro`, `/auth/receptores/cadastro`
    - `/auth/verificar-email`, `/auth/reenviar-codigo`
  - Sessão: `SessionCreationPolicy.STATELESS`
  - CSRF: Desabilitado em virtude de `SameSite=Strict`

---

## 6. Checklist de Testes

- [x] `POST /auth/doadores/cadastro` com dados válidos retorna 201, `UsuarioResponse` (sem cookies) e dispara e-mail com código de 6 dígitos.
- [x] `POST /auth/doadores/cadastro` com e-mail duplicado retorna 409 Conflict.
- [x] `POST /auth/doadores/cadastro` com DTO inválido retorna 400 Bad Request.
- [x] `POST /auth/receptores/cadastro` com dados válidos retorna 201 e persiste o endereço embutido com `emailVerificado = false`.
- [x] `POST /auth/verificar-email` com código correto e dentro de 90s retorna 200 OK, emite cookies e marca `emailVerificado = true`.
- [x] `POST /auth/verificar-email` com código expirado (> 90s) retorna 400 Bad Request.
- [x] `POST /auth/verificar-email` com código inválido retorna 400 Bad Request.
- [x] `POST /auth/reenviar-codigo` revoga o código anterior e gera um novo código com validade de 90s.
- [x] `POST /auth/login` com credenciais corretas mas e-mail não verificado retorna 403 Forbidden.
- [x] `POST /auth/login` com credenciais válidas e e-mail verificado retorna 200 e emite os cookies.
- [x] `POST /auth/login` com senha incorreta retorna 401 Unauthorized.
- [x] `POST /auth/refresh` com refresh token válido rotaciona o token e emite novos cookies.
- [x] `POST /auth/refresh` sem cookie de refresh retorna 401 Unauthorized.
- [x] `POST /auth/logout` revoga o refresh token e define cookies com `Max-Age=0`.
