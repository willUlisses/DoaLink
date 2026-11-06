# ADR 0005: JWT em cookies HttpOnly com refresh token opaco e rotação

- **Status:** Aceito
- **Data:** 2026-11-06

## Contexto

A aplicação é composta por uma Single Page Application (SPA) em React e uma API REST em Spring Boot. A comunicação precisa ser segura contra ataques clássicos da web, principalmente Cross-Site Scripting (XSS) e Cross-Site Request Forgery (CSRF), mantendo a arquitetura de serviços majoritariamente stateless e escalável, sem abrir mão do controle de revogação de acessos.

Alternativas consideradas:
- Armazenamento de JWT em `localStorage` ou `sessionStorage` e envio via cabeçalho `Authorization: Bearer`: descartado pelo elevado risco de exfiltração completa dos tokens em qualquer brecha de injeção XSS em scripts de terceiros ou na SPA.
- Sessão tradicional com estado no servidor (`HttpSession` / Redis): descartado por quebrar a arquitetura stateless da API REST e exigir infraestrutura de persistência de sessão distribuída em escala horizontal.
- JWT de longa duração em cookie HttpOnly sem refresh token: descartado pela impossibilidade de revogar sessões ativas antes da expiração natural da assinatura criptográfica.
- JWT de curta duração associado a Refresh Token opaco com hash no banco, ambos em cookies HttpOnly com rotação: escolhido por unir a proteção contra XSS via HttpOnly, alta performance stateless nas rotas comuns e controle granular de revogação com detecção de roubo de sessão.

## Decisão

Adota-se a autenticação baseada em dois tokens trafegados estritamente via cookies `HttpOnly`:
1. **Access Token (JWT):**
   - Assinado com HMAC-SHA256 (`com.auth0:java-jwt`).
   - Tempo de vida curto de 15 minutos (900 segundos).
   - Contém claims essenciais: `sub` (ID do usuário), `email` e role (`ROLE_DOADOR`, `ROLE_TRANSPORTADOR` ou `ROLE_RECEPTOR`).
   - Validado no `JwtAuthFilter` exclusivamente pela verificação de assinatura e validade temporal, de forma puramente stateless sem consultas ao banco de dados.
   - Trafegado no cookie `access_token` com `Path=/`, `HttpOnly`, `SameSite=Strict` e flag `Secure` (ativa em produção/HTTPS).

2. **Refresh Token (Opaco):**
   - String aleatória de alta entropia gerada criptograficamente (`SecureRandom`).
   - Tempo de vida de 7 dias (604.800 segundos).
   - Trafegado no cookie `refresh_token` com `Path=/auth`, `HttpOnly`, `SameSite=Strict` e flag `Secure`.
   - **Persistência segura:** o valor em texto puro nunca é salvo no banco de dados. Apenas o **hash SHA-256** do token é persistido na tabela `refresh_tokens`, juntamente com `usuario_id`, `data_expiracao`, flag `revogado` e `criado_em`.

3. **Política de Rotação Estrita:**
   - A cada chamada ao endpoint `POST /auth/refresh`, o refresh token apresentado é invalidado (`revogado = true`) e um novo par de tokens (`access_token` e `refresh_token`) é emitido.
   - **Detecção de reuso/roubo:** se um refresh token que já foi revogado for reapresentado, o sistema assume que a credencial foi clonada/interceptada, revogando imediatamente **todos** os refresh tokens ativos daquele usuário e retornando HTTP 401.

4. **Isolamento no Front-end:**
   - O front-end React nunca lê, acessa nem manipula os tokens (não há leitura de `document.cookie` nem gravação em `localStorage`).
   - Todas as requisições HTTP utilizam `credentials: 'include'`.
   - Um interceptador no cliente HTTP detecta respostas 401, dispara `POST /auth/refresh` de forma serializada (evitando múltiplos refreshes concorrentes) e reenvia a requisição original.

## Consequências

- **Ganhos:**
  - **Proteção contra XSS:** script malicioso injetado no navegador não tem permissão de leitura de cookies com atributo `HttpOnly`.
  - **Mitigação de CSRF:** o atributo `SameSite=Strict` impede o envio dos cookies em requisições disparadas por outros sites/origens.
  - **Baixa sobrecarga no banco:** mais de 99% das requisições autenticadas usam o JWT e são validadas de forma puramente matemática e stateless na CPU pelo filtro de segurança.
  - **Capacidade de revogação imediata:** encerramento de sessão explícito (`POST /auth/logout`) e revogação em cascata automática ao detectar tokens duplicados ou expirados.
- **Custos aceitos:**
  - Manutenção de tabela de persistência `refresh_tokens` e rotina periódica de expurgo de tokens antigos expirados.
  - Obrigatoriedade de configuração restrita de CORS no Spring Boot (`allowedOrigins` explícito e `allowCredentials(true)`).
  - O front-end necessita de tratamento robusto para gerenciar a serialização de requisições durante a renovação assíncrona do token.
