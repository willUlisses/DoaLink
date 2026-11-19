# Spec 0005: Disponibilidade de Transporte

- **Status:** Proposto
- **Requisitos:** RF-005, CA-005-1 a CA-005-7, RNF-01, RNF-02 do [PRD 0003](../prd/0003-disponibilidade-de-transporte.md)
- **Decisões:** [ADR 0009](../adr/0011-modelo-de-dados-disponibilidade-de-transporte.md)
- **Depende de:** [Spec 0001](0001-autenticacao-e-cadastro.md)

---

## 1. Visão Geral

Permite ao usuário com perfil `TRANSPORTADOR` registrar sua disponibilidade de transporte informando tipo de veículo, capacidade e unidade, endereço de origem e data (com horário opcional). A entidade mantém `capacidadeRestante` para suportar o vínculo futuro de múltiplas ofertas em uma rota (RF-007 / Google Maps), sem fixar destino no cadastro da disponibilidade. O transportador pode consultar suas próprias disponibilidades e cancelar registros com situação `DISPONIVEL`. O cadastro impede sobreposição de disponibilidades ativas (`DISPONIVEL` ou `RESERVADA`) na mesma data para o mesmo transportador.

---

## 2. Entidade `DisponibilidadeTransporte`

Tabela: `disponibilidades_transporte`

| Campo | Tipo Java | Coluna | Regras |
|---|---|---|---|
| `id` | `Long` | `id` PK | Gerado automaticamente (`IDENTITY`) |
| `transportador` | `Transportador` | `transportador_id` FK | `NOT NULL`, `@ManyToOne(fetch = FetchType.LAZY)` |
| `tipoVeiculo` | `TipoVeiculo` | `tipo_veiculo` | `NOT NULL`, `@Enumerated(EnumType.STRING)` |
| `capacidade` | `Double` | `capacidade` | `NOT NULL`, `> 0` (`CHECK (capacidade > 0)`) |
| `capacidadeRestante` | `Double` | `capacidade_restante` | `NOT NULL`, `>= 0` e `<= capacidade`, inicializada igual a `capacidade` |
| `tipoUnidade` | `TipoUnidade` | `tipo_unidade` | `NOT NULL`, `@Enumerated(EnumType.STRING)` |
| `origem` | `Endereco` | `origem_*` (prefixo via `@AttributeOverrides`) | `NOT NULL` |
| `dataDisponivel` | `LocalDate` | `data_disponivel` | `NOT NULL` |
| `horarioDisponivel` | `LocalTime` | `horario_disponivel` | Opcional (`nullable = true`) |
| `situacao` | `SituacaoDisponibilidade` | `situacao` | `NOT NULL`, `@Enumerated(EnumType.STRING)`, default `DISPONIVEL` |
| `criadoEm` | `LocalDateTime` | `criado_em` | `NOT NULL`, `updatable = false`, preenchido em `@PrePersist` |
| `atualizadoEm` | `LocalDateTime` | `atualizado_em` | Preenchido em `@PrePersist` e `@PreUpdate` |

**Enum `TipoVeiculo`:** `MOTO`, `CARRO_PASSEIO`, `UTILITARIO_LEVE`, `CAMINHONETE`, `CAMINHAO_PEQUENO`, `CAMINHAO_MEDIO`, `CAMINHAO_GRANDE`, `OUTRO`.

**Enum `SituacaoDisponibilidade`:** `DISPONIVEL`, `RESERVADA`, `CONCLUIDA`, `CANCELADA`.

---

## 3. DTOs

### `DisponibilidadeTransporteRequest` (`record`)

| Campo | Tipo | Validação (Bean Validation) |
|---|---|---|
| `tipoVeiculo` | `TipoVeiculo` | `@NotNull` |
| `capacidade` | `Double` | `@NotNull`, `@Positive` |
| `tipoUnidade` | `TipoUnidade` | `@NotNull` |
| `origem` | `EnderecoRequest` | `@NotNull`, `@Valid` |
| `dataDisponivel` | `LocalDate` | `@NotNull`, `@FutureOrPresent` |
| `horarioDisponivel` | `LocalTime` | Opcional |

### `DisponibilidadeTransporteResponse` (`record`)

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `Long` | — |
| `situacao` | `SituacaoDisponibilidade` | — |
| `transportador` | `ContatoResponse` | `nome`, `email`, `telefone` (nunca expõe `documento`) |
| `tipoVeiculo` | `TipoVeiculo` | — |
| `capacidade` | `Double` | — |
| `capacidadeRestante` | `Double` | — |
| `tipoUnidade` | `TipoUnidade` | — |
| `origem` | `EnderecoResponse` | — |
| `dataDisponivel` | `LocalDate` | — |
| `horarioDisponivel` | `LocalTime` | Pode ser `null` |
| `criadoEm` | `LocalDateTime` | — |
| `atualizadoEm` | `LocalDateTime` | — |

---

## 4. `DisponibilidadeTransporteService`

| Método | Regra de negócio | Exceções |
|---|---|---|
| `criar(Long usuarioId, DisponibilidadeTransporteRequest req)` | Carrega `Transportador` pelo `usuarioId` (`ForbiddenException` se não for transportador); verifica se já existe disponibilidade `DISPONIVEL` ou `RESERVADA` para `usuarioId` na `dataDisponivel` (`ConflictException`); mapeia `origem` via `EnderecoMapper` (CEP só dígitos, UF maiúscula, número padrão `S/N`); inicializa `capacidadeRestante = capacidade` e `situacao = DISPONIVEL`. `@Transactional`. | `ForbiddenException`, `ConflictException` |
| `listarMinhas(Long usuarioId)` | Retorna as disponibilidades do transportador ordenadas por `dataDisponivel ASC, criadoEm DESC`. `@Transactional(readOnly = true)`. | — |
| `cancelar(Long usuarioId, Long id)` | Busca disponibilidade por `id` (`NotFoundException`); valida se `transportador.id == usuarioId` (`ForbiddenException`); valida se `situacao == DISPONIVEL` (`ForbiddenException`); altera `situacao` para `CANCELADA`. `@Transactional`. | `NotFoundException`, `ForbiddenException` |
| `encerrar(Long usuarioId, Long id)` | Busca disponibilidade por `id` (`NotFoundException`); valida se `transportador.id == usuarioId` (`ForbiddenException`); valida se `situacao == RESERVADA` (`ConflictException`); valida ausência de reservas `TRANSPORTE_VINCULADO` (`ConflictException`); altera `situacao` para `CONCLUIDA`. `@Transactional`. | `NotFoundException`, `ForbiddenException`, `ConflictException` |

---

## 5. `DisponibilidadeTransporteRepository` e `TransportadorRepository`

- **`TransportadorRepository`:** `extends JpaRepository<Transportador, Long>`.
- **`DisponibilidadeTransporteRepository`:**
  - `existsByTransportadorIdAndDataDisponivelAndSituacaoIn(Long transportadorId, LocalDate dataDisponivel, Collection<SituacaoDisponibilidade> situacoes)`
  - `@EntityGraph(attributePaths = "transportador") List<DisponibilidadeTransporte> findByTransportadorIdOrderByDataDisponivelAscCriadoEmDesc(Long transportadorId)`
  - `@EntityGraph(attributePaths = "transportador") Optional<DisponibilidadeTransporte> findWithTransportadorById(Long id)`

---

## 6. Endpoints (`DisponibilidadeTransporteController` / `IDisponibilidadeTransporteController`)

Base: `/disponibilidades`

| Método | Rota | Entrada | Saída | Sucesso | Erros | Acesso |
|---|---|---|---|---|---|---|
| `POST` | `/disponibilidades` | `DisponibilidadeTransporteRequest` | `DisponibilidadeTransporteResponse` + `Location` | `201` | `400` validação, `401`, `403`, `409` data já ocupada | `ROLE_TRANSPORTADOR` |
| `GET` | `/disponibilidades/minhas` | — | `List<DisponibilidadeTransporteResponse>` | `200` | `401`, `403` | `ROLE_TRANSPORTADOR` |
| `PATCH` | `/disponibilidades/{id}/cancelar` | — | — | `204` | `401`, `403`, `404` | `ROLE_TRANSPORTADOR` (dono) |
| `PATCH` | `/disponibilidades/{id}/encerrar` | — | — | `204` | `401`, `403`, `404`, `409` | `ROLE_TRANSPORTADOR` (dono) |

---

## 7. Segurança

- `SecurityConfig` exige `hasRole("TRANSPORTADOR")` para `POST /disponibilidades`, `GET /disponibilidades/minhas`, `PATCH /disponibilidades/*/cancelar` e `PATCH /disponibilidades/*/encerrar`.
- Visitantes sem sessão recebem `401`; outros perfis (`DOADOR`, `RECEPTOR`) recebem `403`.
- A verificação de propriedade no cancelamento é feita no `DisponibilidadeTransporteService`.

---

## 8. Banco (`V4__disponibilidade_transporte.sql`)

- Cria a tabela `disponibilidades_transporte` com FK `fk_disponibilidades_transportador` para `transportadores(id)`.
- Constraints `ck_disponibilidades_capacidade` (`CHECK (capacidade > 0)`) e `ck_disponibilidades_capacidade_restante` (`CHECK (capacidade_restante >= 0 AND capacidade_restante <= capacidade)`).
- Índice único parcial `uk_disponibilidade_transportador_data_ativa` em `(transportador_id, data_disponivel)` com `WHERE situacao IN ('DISPONIVEL', 'RESERVADA')` para garantir RNF-02 no banco.
- Índice `idx_disponibilidades_transportador_data` em `(transportador_id, data_disponivel ASC, criado_em DESC)`.
- Índice `idx_disponibilidades_situacao_data` em `(situacao, data_disponivel)`.

---

## 9. Limites aceitos

- Edição de disponibilidade após criação não é suportada; o transportador deve cancelar e criar um novo registro.
- A montagem de rota com múltiplas ofertas (integração com Google Maps) fica fora desta spec e usará `capacidadeRestante` em etapa posterior.
- A validação `@FutureOrPresent` usa o relógio padrão da JVM (configurado para o fuso da aplicação).
- A garantia final contra criação concorrente na mesma data é o índice único parcial no PostgreSQL (tratado como `409 Conflict` via `DataIntegrityViolationException` ou verificação prévia no service).

---

## 10. Testes

- [x] `POST /disponibilidades` cria disponibilidade válida para `TRANSPORTADOR` (`201` + `Location`) com `capacidadeRestante == capacidade`.
- [x] `POST /disponibilidades` rejeita data passada, capacidade `<= 0` ou campos obrigatórios ausentes (`400`).
- [x] `POST /disponibilidades` rejeita data já ocupada por disponibilidade `DISPONIVEL` ou `RESERVADA` do mesmo transportador (`409`).
- [x] `GET /disponibilidades/minhas` retorna apenas disponibilidades do transportador autenticado (`200`).
- [x] `PATCH /disponibilidades/{id}/cancelar` cancela disponibilidade `DISPONIVEL` do dono (`204`).
- [x] `PATCH /disponibilidades/{id}/cancelar` recusa outro transportador ou situação diferente de `DISPONIVEL` (`403`) e id inexistente (`404`).
- [x] `PATCH /disponibilidades/{id}/encerrar` encerra disponibilidade `RESERVADA` sem pendências (`204`) e rejeita pendências ou situação diferente de `RESERVADA` (`409`).
- [ ] Segurança: visitante recebe `401` e perfil `DOADOR`/`RECEPTOR` recebe `403` nos endpoints de `/disponibilidades` (regras declaradas em `SecurityConfig`, pendente teste de integração com filtro de segurança e PostgreSQL ativo).
