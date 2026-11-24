# Spec 0006: Reserva de Oferta e Fluxo de Transporte

- **Status:** Atualizado pelo [PRD 0007](../prd/0007-fluxo-de-transporte-por-codigos-de-seguranca.md) e [ADR 0013](../adr/0013-fluxo-de-transporte-por-codigos-com-hash-e-suspensao-por-expiracao.md)
- **Requisitos:** RF-010 ([PRD 0006](../prd/0006-reserva-e-vinculo-de-transporte.md)) e RF-011 a RF-015 ([PRD 0007](../prd/0007-fluxo-de-transporte-por-codigos-de-seguranca.md))
- **Decisões:** [ADR 0013](../adr/0013-fluxo-de-transporte-por-codigos-com-hash-e-suspensao-por-expiracao.md) (substitui [ADR 0012](../adr/0012-vinculo-entre-oferta-receptor-e-transporte.md))
- **Depende de:** [Spec 0002](0002-oferta-de-doacao.md)

---

## 1. Visão Geral

Implementa a entidade `ReservaEntrega` para conectar uma `OfertaDoacao` reservada por um `Receptor` a uma `DisponibilidadeTransporte` de um `Transportador` (quando `precisaTransporte = true`). Uma mesma disponibilidade pode assumir múltiplas reservas enquanto houver `capacidadeRestante` compatível. O endereço completo de retirada e entrega é liberado apenas aos participantes vinculados à reserva.

---

## 2. Entidade `ReservaEntrega`

Tabela: `reservas_entrega`

| Campo | Tipo Java | Coluna | Regras |
|---|---|---|---|
| `id` | `Long` | `id` PK | Gerado automaticamente (`IDENTITY`) |
| `oferta` | `OfertaDoacao` | `oferta_id` FK | `NOT NULL`, `@ManyToOne(fetch = FetchType.LAZY)` |
| `receptor` | `Receptor` | `receptor_id` FK | `NOT NULL`, `@ManyToOne(fetch = FetchType.LAZY)` |
| `disponibilidade` | `DisponibilidadeTransporte` | `disponibilidade_id` FK | Opcional (`nullable = true`), `@ManyToOne(fetch = FetchType.LAZY)` |
| `precisaTransporte` | `Boolean` | `precisa_transporte` | `NOT NULL` |
| `situacao` | `SituacaoReservaEntrega` | `situacao` | `NOT NULL`, `@Enumerated(EnumType.STRING)` |
| `criadoEm` | `LocalDateTime` | `criado_em` | `NOT NULL`, `updatable = false`, `@PrePersist` |
| `atualizadoEm` | `LocalDateTime` | `atualizado_em` | `NOT NULL`, `@PrePersist` e `@PreUpdate` |

**Enum `SituacaoReservaEntrega`:**
- `AGUARDANDO_TRANSPORTE` (quando `precisaTransporte = true`, antes do vínculo com transportador)
- `AGUARDANDO_RETIRADA` (quando `precisaTransporte = false`, retirada direta pelo receptor)
- `TRANSPORTE_VINCULADO` (quando um transportador vinculou sua disponibilidade)
- `CONCLUIDA`
- `CANCELADA`

---

## 3. DTOs

### `ReservaEntregaRequest` (`record`)
- `ofertaId`: `Long` (`@NotNull`)
- `precisaTransporte`: `Boolean` (`@NotNull`)

### `VincularTransporteRequest` (`record`)
- `disponibilidadeId`: `Long` (`@NotNull`)

### `ReservaEntregaResponse` (`record`)
- `id`, `situacao`, `precisaTransporte`, `ofertaId`, `situacaoOferta`, `disponibilidadeId`
- `doador`, `receptor`, `transportador` (`ContatoResponse` — `doador` e `receptor` são `null` na listagem `aguardando-transporte`)
- `enderecoRetirada`, `enderecoEntrega` (`EnderecoResponse` — completos apenas para participantes vinculados; resumidos apenas com `cidade`/`estado` na listagem `aguardando-transporte`)
- `itens` (`List<ItemDoacaoResponse>`)
- `criadoEm`, `atualizadoEm`

---

## 4. `ReservaEntregaService`

| Método | Regra de negócio | Exceções |
|---|---|---|
| `reservar(Long usuarioId, ReservaEntregaRequest)` | Valida `Receptor` (`403`); busca `OfertaDoacao` (`404`); exige `oferta.situacao == DISPONIVEL` (`409`); altera oferta para `RESERVADA` e cria reserva (`AGUARDANDO_TRANSPORTE` ou `AGUARDANDO_RETIRADA`). `@Transactional`. | `ForbiddenException`, `NotFoundException`, `ConflictException` |
| `listarAguardandoTransporte()` | Lista reservas `AGUARDANDO_TRANSPORTE` com endereços resumidos (`cidade`/`estado`) e sem contatos (`doador`/`receptor` nulos). `@Transactional(readOnly = true)`. | — |
| `listarMinhasReceptor(Long usuarioId)` | Lista reservas do receptor com dados completos. `@Transactional(readOnly = true)`. | — |
| `listarMinhasTransportador(Long usuarioId)` | Lista reservas vinculadas às disponibilidades do transportador com dados completos. `@Transactional(readOnly = true)`. | — |
| `listarMinhasDoador(Long usuarioId)` | Lista reservas associadas às ofertas do doador autenticado com dados completos. `@Transactional(readOnly = true)`. | — |
| `vincularTransporte(Long usuarioId, Long reservaId, VincularTransporteRequest)` | Valida reserva `AGUARDANDO_TRANSPORTE` (`409`); valida disponibilidade do transportador dono (`403`) em situação `DISPONIVEL` ou `RESERVADA` (`409`) e `dataDisponivel >= hoje` (`409`); exige todos os itens na mesma `TipoUnidade` da disponibilidade e soma `<= capacidadeRestante` (`409`); abate `capacidadeRestante`, muda disponibilidade para `RESERVADA` e reserva para `TRANSPORTE_VINCULADO`. Lock otimista (`@Version`) gera `409` em corrida. `@Transactional`. | `NotFoundException`, `ForbiddenException`, `ConflictException` |
| `concluir(Long usuarioId, Long reservaId)` | Se `TRANSPORTE_VINCULADO`, apenas o transportador vinculado conclui; se `AGUARDANDO_RETIRADA`, apenas o receptor dono conclui (`403`); demais situações (`409`). Atualiza reserva e oferta para `CONCLUIDA`. Lock otimista (`@Version`) gera `409` em corrida. `@Transactional`. | `NotFoundException`, `ForbiddenException`, `ConflictException` |
| `cancelar(Long usuarioId, Long reservaId)` | Apenas o receptor dono (`403`) e apenas em `AGUARDANDO_TRANSPORTE` ou `AGUARDANDO_RETIRADA` (`409`). Atualiza reserva para `CANCELADA` e oferta para `DISPONIVEL`. Lock otimista (`@Version`) gera `409` em corrida. `@Transactional`. | `NotFoundException`, `ForbiddenException`, `ConflictException` |

### Atualização em `OfertaDoacaoService.buscarPorId`
- Passa a liberar o endereço completo para o `Doador` dono, para o `Receptor` da reserva ativa e para o `Transportador` vinculado à reserva ativa daquela oferta (inclusive após `CONCLUIDA`).

---

## 5. Endpoints (`ReservaEntregaController` / `IReservaEntregaController`)

Base: `/reservas`

| Método | Rota | Entrada | Saída | Sucesso | Erros | Acesso |
|---|---|---|---|---|---|---|
| `POST` | `/reservas` | `ReservaEntregaRequest` | `ReservaEntregaResponse` + `Location` | `201` | `400`, `401`, `403`, `404`, `409` | `ROLE_RECEPTOR` |
| `GET` | `/reservas/aguardando-transporte` | — | `List<ReservaEntregaResponse>` | `200` | `401`, `403` | `ROLE_TRANSPORTADOR` |
| `GET` | `/reservas/minhas/receptor` | — | `List<ReservaEntregaResponse>` | `200` | `401`, `403` | `ROLE_RECEPTOR` |
| `GET` | `/reservas/minhas/transportador` | — | `List<ReservaEntregaResponse>` | `200` | `401`, `403` | `ROLE_TRANSPORTADOR` |
| `GET` | `/reservas/minhas/doador` | — | `List<ReservaEntregaResponse>` | `200` | `401`, `403` | `ROLE_DOADOR` |
| `PATCH` | `/reservas/{id}/vincular-transporte` | `VincularTransporteRequest` | `ReservaEntregaResponse` | `200` | `400`, `401`, `403`, `404`, `409` | `ROLE_TRANSPORTADOR` |
| `PATCH` | `/reservas/{id}/concluir` | — | — | `204` | `401`, `403`, `404`, `409` | `ROLE_TRANSPORTADOR`, `ROLE_RECEPTOR` |
| `PATCH` | `/reservas/{id}/cancelar` | — | — | `204` | `401`, `403`, `404`, `409` | `ROLE_RECEPTOR` |

---

## 6. Banco (`V5__reserva_entrega.sql` e `V6__versionamento_otimista.sql`)

- Tabela `reservas_entrega` com FKs para `ofertas_doacao`, `receptores` e `disponibilidades_transporte`.
- Índice único parcial `uk_reserva_entrega_oferta_ativa` em `(oferta_id) WHERE situacao <> 'CANCELADA'`.
- Índices para consultas de receptor, disponibilidade e situação.
- `V6__versionamento_otimista.sql` adiciona coluna `versao` (`BIGINT NOT NULL DEFAULT 0`) em `ofertas_doacao`, `disponibilidades_transporte` e `reservas_entrega` para controle de concorrência otimista (`@Version`).

---

## 7. Testes

- [x] Receptor reserva oferta `DISPONIVEL` (`201`) com ou sem transporte (`AGUARDANDO_TRANSPORTE` / `AGUARDANDO_RETIRADA`) e oferta muda para `RESERVADA`.
- [x] Reserva sobre oferta não `DISPONIVEL` retorna `409`.
- [x] Transportador lista reservas `AGUARDANDO_TRANSPORTE` com endereço resumido (`cidade`/`estado`) e contatos `doador`/`receptor` ocultos.
- [x] Transportador vincula disponibilidade compatível, abate `capacidadeRestante` e permite múltiplas ofertas na mesma disponibilidade.
- [x] Vínculo com unidade incompatível, capacidade insuficiente ou disponibilidade expirada retorna `409`.
- [x] Conclusão altera reserva e oferta para `CONCLUIDA` (transportador em `TRANSPORTE_VINCULADO` e receptor em `AGUARDANDO_RETIRADA`); cancelamento pelo receptor devolve oferta para `DISPONIVEL` e rejeita em `TRANSPORTE_VINCULADO` (`409`).
- [x] Doador lista reservas de suas ofertas via `GET /reservas/minhas/doador`.
- [x] `OfertaDoacaoService.buscarPorId` libera endereço completo para transportador vinculado (inclusive em `CONCLUIDA`) e receptor da reserva.
- [ ] Integração com PostgreSQL (migrations V4–V6, concorrência real com `@Version` e filtros de segurança `401`/`403`).
