---
name: adr
description: Como escrever decisões de arquitetura neste projeto. Use ao criar, aceitar ou substituir um ADR em docs/adr/, ou quando uma mudança contrariar ou exigir uma decisão de arquitetura (back-end Spring Boot ou front-end React).
---

# ADR 0008: Recuperação de senha via token opaco hashado e envio SMTP (Mailpit)

- **Status:** Proposto
- **Data:** 2026-11-10

## Contexto

A funcionalidade de recuperação de senha (RF-003) requer que o usuário possa redefinir sua senha por meio de um link enviado por e‑mail. Considerações importantes:
- Necessidade de evitar vazamento de tokens em caso de comprometimento da base de dados.
- Prevenção contra ataques de enumeração de e‑mail (resposta uniforme, timing‑safe).
- Validade curta do link para reduzir janela de ataque.
- Revogação de refresh tokens existentes após redefinição bem‑sucedida.
- Ambiente de desenvolvimento deve usar um serviço SMTP local (Mailpit) para captura de e‑mails.

## Decisão

- **Token opaco:** Gerado como 32 bytes aleatórios (SecureRandom) e armazenado como hash SHA‑256 na tabela `token_recuperacao_senha`. O token em texto puro é enviado por e‑mail e nunca persiste.
- **Validade:** 10 minutos.
- **Uso único:** Campo `utilizado` indica se o token já foi consumido; após sucesso, o token é marcado como utilizado.
- **Revogação de sessões:** Ao redefinir a senha, todos os refresh tokens do usuário são revogados.
- **Envio de e‑mail:** Utilizar `spring-boot-starter-mail` com configuração SMTP apontando para o container `mailpit` (host `mailpit`, porta `1025`). O e‑mail será HTML amigável.
- **Resposta uniforme:** Endpoint de solicitação de recuperação devolve `200 OK` independentemente da existência do e‑mail, sem indicar se o usuário existe.

## Consequências

- **Benefícios:** Segurança reforçada (tokens não armazenados em texto claro), mitigação de enumeração, facilidade de teste local via Mailpit.
- **Custos:** Necessidade de criar nova migração de banco, serviço de e‑mail, e atualização da configuração de segurança.
- **Impacto:** Alterações em `SecurityConfig` para permitir rotas públicas de recuperação, e necessidade de limpar refresh tokens após redefinição.
