# ADR 0009: Confirmação de e-mail por código numérico de 6 dígitos com hash SHA-256 e janela estrita de 90 segundos

- **Status:** Proposto
- **Data:** 2026-11-12

## Contexto

A implementação do RF-010 (Confirmação de e-mail) visa validar a titularidade do endereço informado durante o cadastro de novos participantes (`Doador`, `Transportador` e `Receptor`), mitigando fraudes, spam, cadastros fantasmas e sequestro de contas com e-mails pertencentes a terceiros.

Decisões de segurança críticas envolvidas:
1. **Formato do desafio de verificação:**
   - Alternativa A: Link com token de longa duração (ex: 24 horas) enviado por e-mail. Descartada por estender excessivamente a janela de exposição de tokens nos servidores de correio e dificultar a experiência imediata de validação em dispositivos móveis ou no fluxo contínuo de cadastro da SPA.
   - Alternativa B: Código numérico de 6 dígitos com validade curta (90s). Escolhida por exigir digitação manual direta na aplicação, manter o fluxo em tela contínuo e minimizar o tempo útil para ataques de força bruta ou interceptação.

2. **Segurança do armazenamento do código:**
   - Alternativa A: Armazenar o código de 6 dígitos em texto claro no banco de dados. Descartada pelo alto risco de vazamento em cenários de SQL injection, dumps de banco desprotegidos ou acesso indevido à base de dados.
   - Alternativa B: Persistência exclusiva do hash criptográfico (SHA-256) do código gerado via gerador seguro de números pseudoaleatórios (`SecureRandom`). Escolhida para garantir que mesmo um atacante com acesso de leitura à base de dados não consiga visualizar o código ativo.

3. **Ciclo de vida, revogação e idempotência temporal:**
   - Cada envio ou reenvio invalida (`revogado = true`) qualquer código prévio pendente do mesmo usuário.
   - O uso com sucesso do código marca o registro como `utilizado = true` imediatamente na mesma transação, impedindo ataques de repetição (*replay attack*).
   - Códigos expirados são marcados como revogados e rejeitados com exceção explícita de domínio.

4. **Bloqueio de acesso para contas não verificadas:**
   - No cadastro, a conta é criada com `email_verificado = false` e nenhum cookie de sessão (`access_token` ou `refresh_token`) é emitido.
   - O login (`POST /auth/login`) e a rotação de sessão (`POST /auth/refresh`) bloqueiam participantes não verificados lançando erro HTTP 403 Forbidden.
   - Somente a validação bem-sucedida do código (`POST /auth/verificar-email`) ativa a conta (`email_verificado = true`) e emite a sessão inicial autenticada.

5. **Proteção contra enumeração de e-mails no reenvio:**
   - O endpoint de reenvio (`POST /auth/reenviar-codigo`) responde com HTTP 200 de forma uniforme mesmo se o e-mail não existir ou já estiver verificado, impedindo que atacantes descubram e-mails registrados no sistema.

## Decisão

1. **Geração e Hash:** O código de 6 dígitos numéricos (gerado por `SecureRandom`) é enviado em texto simples apenas no corpo do e-mail ao usuário (via SMTP/Mailpit). No banco (`codigo_verificacao_email`), armazena-se exclusivamente o hash SHA-256 em coluna `VARCHAR(64)` (`codigo_hash`).
2. **Janela de Expiração:** Define-se a validade estrita de 90 segundos (`app.verificacao-email.expiracao-segundos:90`).
3. **Invalidação e Unicidade:**
   - Ao gerar um novo código para um usuário, todos os códigos anteriores não utilizados têm o status alterado para `revogado = true`.
   - Um código verificado é marcado com `utilizado = true` atomicamente.
4. **Portão de Autenticação (Gatekeeper):** A emissão de cookies de sessão só ocorre após verificação do e-mail. Requisições de login ou refresh para contas não verificadas são barradas com HTTP 403.
5. **Mitigação de Enumeração:** O endpoint de reenvio trata e-mails não cadastrados ou já verificados de forma silenciosa e idempotente, logando apenas em nível `DEBUG` e retornando sucesso neutro.

## Consequências

- **Ganhos:**
  - **Imunidade contra vazamento na base:** Códigos ativos não podem ser lidos diretamente no banco, pois apenas o digest SHA-256 fica salvo.
  - **Superfície de ataque temporal mínima:** A janela de 90 segundos reduz drasticamente a eficácia de tentativas de força bruta (mesmo com espaço amostral de $10^6$ valores numéricos).
  - **Proteção contra força bruta e repetição:** Códigos são de uso estritamente único e revogados ao serem superados ou expirados.
  - **Bloqueio efetivo de contas falsas:** Contas sem e-mail verificado não recebem sessão nem conseguem interagir com o ecossistema (ofertas, doações, matchings).
- **Custos aceitos:**
  - **Sensibilidade a atrasos no envio de e-mails:** Se o serviço de entrega de correio eletrônico do usuário sofrer atraso superior a 90 segundos, o usuário precisará solicitar reenvio.
  - **Sobrecarga de persistência e expurgo:** Necessidade de gerenciar registros na tabela `codigo_verificacao_email` e realizar limpeza periódica/best-effort de registros expirados.
