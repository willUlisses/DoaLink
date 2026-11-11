# Spec 0003: Validade do link de recuperação de senha e contratos de API

- **Status:** Implementado
- **Data:** 2026-11-10 (Atualizado: 2026-11-11)
- **Requisitos do PRD:** [RF-003 — Recuperação de senha](../prd/0001-autenticacao-cadastro-e-sessao.md#rf-003--recuperação-de-senha)
- **Decisões arquiteturais:** [ADR 0007](../adr/0007-segregacao-de-contratos-openapi-em-interfaces-de-controller.md), [ADR 0008](../adr/0008-recuperacao-de-senha-via-token-hash-e-smtp.md)

---

## Visão geral

Esta especificação define o comportamento do fluxo de recuperação de senha e do ciclo de vida dos tokens gerados. O token é opaco (32 bytes aleatórios via `SecureRandom`), enviado exclusivamente por e-mail, e seu hash SHA-256 é armazenado de forma segura na tabela `token_recuperacao_senha` com expiração de 10 minutos.

---

## Requisitos funcionais

- **RF-003-A:** O token de recuperação deve ter validade estrita de **10 minutos** a partir da sua criação.
- **RF-003-B:** O token é de uso único. Após ser consumido para definir uma nova senha ou ao expirar, o token torna-se inválido e não pode ser reutilizado.
- **RF-003-C:** O token nunca é salvo em texto puro; apenas seu hash SHA-256 é armazenado e associado ao usuário.
- **RF-003-D:** A solicitação de recuperação de senha possui resposta uniforme (`200 OK`) para prevenir enumeração de contas (timing-safe e resposta invariante).
- **RF-003-E:** Ao redefinir a senha com sucesso, todos os refresh tokens ativos do usuário são sumariamente revogados, forçando um novo login em todos os dispositivos.

---

## Contratos de API (SpringDoc OpenAPI / ADR 0007)

Declarados na interface `com.doalink_api.controller.IRecuperacaoSenhaController` e implementados por `com.doalink_api.controller.RecuperacaoSenhaController`:

### 1. Solicitar recuperação de senha
- **Rota:** `POST /auth/recuperar-senha`
- **Autenticação:** Pública (`permitAll`)
- **Corpo da requisição (`SolicitarRecuperacaoSenhaRequest`):**
  ```json
  {
    "email": "usuario@exemplo.com"
  }
  ```
- **Respostas:**
  - `200 OK`: `{"mensagem": "Se o e-mail informado estiver cadastrado, você receberá um link para redefinir sua senha em breve."}`
  - `400 Bad Request`: E-mail ausente ou inválido.

### 2. Validar token de recuperação
- **Rota:** `GET /auth/recuperar-senha/validar?token={token}`
- **Autenticação:** Pública (`permitAll`)
- **Parâmetros:** `token` (String, obrigatório, `@NotBlank`)
- **Respostas:**
  - `200 OK`: `{"valido": true}` ou `{"valido": false}`
  - `400 Bad Request`: Parâmetro ausente ou vazio.

### 3. Redefinir senha
- **Rota:** `POST /auth/redefinir-senha`
- **Autenticação:** Pública (`permitAll`)
- **Corpo da requisição (`RedefinirSenhaRequest`):**
  ```json
  {
    "token": "token-opaco-recebido-por-email",
    "novaSenha": "NovaSenhaForte@123"
  }
  ```
- **Validações:** Senha entre 8 e 72 caracteres, token obrigatório.
- **Respostas:**
  - `200 OK`: `{"mensagem": "Senha redefinida com sucesso."}`
  - `400 Bad Request`: Dados inválidos (senha fora do tamanho ou campos em branco).
  - `404 Not Found`: Token inválido, expirado ou já utilizado.

---

## Critérios de Aceite Verificados

| Critério | Descrição | Status | Teste Automatizado |
|---|---|---|---|
| **CA-003-1** | Envio de link de uso único ao e-mail cadastrado | Atendido | `RecuperacaoSenhaIntegrationTest#deveExecutarFluxoCompletoDeRecuperacaoComSucesso` |
| **CA-003-2** | Resposta uniforme para e-mail não cadastrado | Atendido | `RecuperacaoSenhaIntegrationTest#deveResponderOkUniformeQuandoEmailNaoCadastrado` |
| **CA-003-3** | Atualização da senha para critérios de RF-001 | Atendido | `RecuperacaoSenhaIntegrationTest#deveExecutarFluxoCompletoDeRecuperacaoComSucesso` |
| **CA-003-4** | Rejeição de link inválido, expirado ou reutilizado | Atendido | `RecuperacaoSenhaIntegrationTest#deveRejeitarReutilizacaoDeToken`, `deveRejeitarTokenExpirado` |
| **CA-003-5** | Login bem-sucedido com a nova senha após redefinição | Atendido | `RecuperacaoSenhaIntegrationTest#deveExecutarFluxoCompletoDeRecuperacaoComSucesso` |
| **CA-003-6** | Rejeição de autenticação com a senha antiga | Atendido | `RecuperacaoSenhaIntegrationTest#deveExecutarFluxoCompletoDeRecuperacaoComSucesso` |
