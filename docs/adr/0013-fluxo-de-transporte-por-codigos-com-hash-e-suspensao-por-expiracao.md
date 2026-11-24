# ADR 0013: Fluxo de transporte sem disponibilidade prévia, validação por códigos com hash e suspensão automática por expiração

- **Status:** Proposto
- **Data:** 2026-11-24

## Contexto

O DoaLink passou a atuar exclusivamente como plataforma de conexão entre doadores, receptores (ONGs) e transportadores, delegando a negociação logística e a execução física do transporte para fora da aplicação. O modelo anterior baseado em `DisponibilidadeTransporte` (cadastro prévio de veículo, capacidade e data) e conclusão direta sem comprovação criava acoplamento logístico desnecessário e não protegia doadores e receptores contra retiradas não autorizadas ou extravios após a coleta.

Alternativas consideradas:

- **Manter `DisponibilidadeTransporte` como opcional:** preserva tabelas e fluxos legados sem uso no produto e mantém complexidade de concorrência em capacidade restante.
- **Armazenar códigos de confirmação de retirada e entrega em texto puro ou criptografia simétrica reversível na tabela:** expõe os códigos ativos a leituras indevidas no banco ou em backups.
- **Enviar códigos por e-mail ao transportador ou permitir conclusão unilateral sem código:** rompe a garantia presencial de posse física na retirada e na entrega.
- **Vínculo direto `ReservaEntrega -> Transportador` + códigos de 6 dígitos derivados por `HMAC-SHA256` com `salt` aleatório e hash `SHA-256` no banco + suspensão automática em 24h (escolhida):** elimina o conceito de disponibilidade, garante que o banco armazene apenas digest e salt, permite consulta pelo dono autorizado em memória e automatiza a resposta a expirações.

## Decisão

1. **Remoção de disponibilidade e vínculo direto na reserva:**
   - A entidade `DisponibilidadeTransporte`, a tabela `disponibilidades_transporte` e os enums `TipoVeiculo` e `SituacaoDisponibilidade` são removidos via migration Flyway `V11`, migrando vínculos legados antes do descarte.
   - `ReservaEntrega` referencia diretamente `Transportador` (`transportador_id`) e registra os marcos temporais `aceitoEm`, `autorizadoEm`, `retiradaConfirmadaEm` e `entregaConfirmadaEm`, mantendo lock otimista (`@Version`) para impedir aceite simultâneo por dois motoristas (`409 Conflict`).

2. **Separação estrita de status por responsabilidade:**
   - `SituacaoOferta` (`OfertaDoacao`): `DISPONIVEL`, `RESERVADA`, `CONCLUIDA`, `CANCELADA`.
   - `SituacaoReservaEntrega` (`ReservaEntrega`): `AGUARDANDO_TRANSPORTE`, `AGUARDANDO_RETIRADA`, `AGUARDANDO_AUTORIZACAO_DOADOR`, `AGUARDANDO_CONFIRMACAO_RETIRADA`, `EM_TRANSPORTE`, `CONCLUIDA`, `CANCELADA`, `EM_INVESTIGACAO`.
   - `SituacaoTransportador` (`Transportador`): `ATIVO`, `SUSPENSO`.
   - `SituacaoOcorrenciaTransporte` (`OcorrenciaTransporte`): `PENDENTE_INVESTIGACAO`, `EM_ANALISE`, `RESOLVIDA`.

3. **Geração, armazenamento e validação de códigos de segurança (`CodigoTransporteService`):**
   - Ao gerar um código (`TipoCodigoTransporte`: `RETIRADA` ou `ENTREGA`), cria-se um `codigoSalt` de 32 bytes via `SecureRandom` e deriva-se o código numérico de 6 dígitos por `HMAC-SHA256(app.jwt.secret, salt + ":" + reservaId + ":" + tipo)`.
   - Persiste-se em `codigos_transporte` apenas `codigo_hash` (`SHA-256` do código em hex de 64 caracteres) e `codigo_salt`, nunca o código em texto puro.
   - Enquanto ativo (`!utilizado && !invalidado && !bloqueado && !estaExpirado`), apenas o `Doador` dono da oferta pode consultar o código de `RETIRADA` (em `AGUARDANDO_CONFIRMACAO_RETIRADA`) e apenas o `Receptor` dono da reserva pode consultar o código de `ENTREGA` (em `EM_TRANSPORTE`). O `Transportador` nunca recebe os códigos nos endpoints de consulta.
   - A validação ocorre exclusivamente no servidor por comparação em tempo constante (`MessageDigest.isEqual`) sobre o hash `SHA-256`. Tentativas incorretas são persistidas em transação com `noRollbackFor = {CodigoInvalidoException.class, ForbiddenException.class}` e bloqueiam o código após 5 erros (`app.transporte.codigo.max-tentativas`).

4. **Expiração de 24 horas, suspensão automática e trilha de auditoria:**
   - O código de `ENTREGA` possui validade estrita de 24 horas (`app.transporte.codigo-entrega.validade-horas`) a partir de `retiradaConfirmadaEm`.
   - Uma rotina agendada (`@Scheduled`) e verificações sob demanda nos endpoints de reserva detectam códigos vencidos, invalidam o código, alteram a reserva para `EM_INVESTIGACAO` (preservando a oferta como `RESERVADA`), alteram o `Transportador` para `SUSPENSO` (impedindo novos aceites no back-end) e criam um registro em `ocorrencias_transporte` com situação `PENDENTE_INVESTIGACAO`.
   - Toda transição relevante, tentativa inválida, bloqueio ou expiração gera registro imutável em `eventos_transporte` (`EventoTransporte`).

## Consequências

- **Ganhos:**
  - Fluxo aderente ao papel de intermediação do DoaLink, sem gestão de veículos, capacidades ou rotas.
  - Garantia criptográfica contra vazamento dos códigos ativos na base de dados e proteção contra força bruta, reutilização e requisições concorrentes.
  - Rastreabilidade completa de auditoria e resposta automática a atrasos/extravios após a retirada física dos itens.
- **Custos aceitos:**
  - A reconstrução em memória do código ativo para o doador/receptor depende da estabilidade de `app.jwt.secret` enquanto houver operações ativas; a rotação da chave secreta invalida a exibição de códigos gerados antes da rotação.
  - O bloqueio de um código após 5 tentativas incorretas ou a suspensão por expiração de 24h exige futura interface administrativa (`ADMIN`) para analisar `ocorrencias_transporte`, reativar contas ou resolver reservas em `EM_INVESTIGACAO`.
  - O uso de `@Scheduled` exige `@EnableScheduling` habilitado na aplicação (`DoalinkApiApplication`) e tolerância a latência de até 1 minuto entre o vencimento exato e a varredura periódica (compensada pela checagem síncrona na tentativa de uso ou listagem).
