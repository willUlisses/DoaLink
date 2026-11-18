# Spec 0005: Campanha Emergencial

- **Status:** Implementado
- **Requisitos:** RF-008, CA-008-1 a CA-008-3 do [PRD 0005](../prd/0005-campanha-emergencial-e-acompanhamento.md)
- **Decisões:** [ADR 0010](../adr/0010-campanha-emergencial-criada-por-receptores-com-endereco-de-recebimento.md)
- **Depende de:** [Spec 0001](0001-autenticacao-e-cadastro.md), [Spec 0004](0004-necessidade-de-doacao.md)

---

## Entidades

### `CampanhaEmergencial` (`campanhas_emergenciais`)
- `id`: BIGINT IDENTITY (PK)
- `receptor`: `ManyToOne(fetch = FetchType.LAZY)` para `Receptor`, obrigatório (`receptor_id NOT NULL`)
- `nome`: VARCHAR(150), obrigatório
- `descricao`: VARCHAR(1000), opcional
- `enderecoRecebimento`: `@Embedded Endereco`, obrigatório (com colunas `cep`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `estado`)
- `situacao`: `SituacaoCampanha` (`ATIVA`, `ENCERRADA`, `CANCELADA`), obrigatório, padrão `ATIVA`
- `itens`: `OneToMany(mappedBy = "campanha", cascade = CascadeType.ALL, orphanRemoval = true)` de `ItemCampanha`
- `criadoEm`, `atualizadoEm`: LocalDateTime mantidos via `@PrePersist` e `@PreUpdate`

### `ItemCampanha` (`itens_campanha`)
- `id`: BIGINT IDENTITY (PK)
- `campanha`: `ManyToOne(fetch = FetchType.LAZY)` para `CampanhaEmergencial`, obrigatório
- `tipoItemDoacao`: `TipoItemDoacao`, obrigatório
- `tipoUnidade`: `TipoUnidade`, obrigatório
- `quantidade`: Double positivo, obrigatório
- `descricao`: VARCHAR(500), opcional

---

## DTOs

| Record | Campos e validação |
|---|---|
| `CampanhaEmergencialRequest` | `nome`: `@NotBlank @Size(max = 150)`; `descricao`: `@Size(max = 1000)`; `enderecoRecebimento`: `@NotNull @Valid EnderecoRequest`; `itens`: `@Valid @Size(max = 50) List<ItemCampanhaRequest>` |
| `ItemCampanhaRequest` | `tipoItemDoacao`: `@NotNull`; `tipoUnidade`: `@NotNull`; `quantidade`: `@NotNull @Positive`; `descricao`: `@Size(max = 500)` |
| `CampanhaEmergencialResponse` | `id`, `nome`, `descricao`, `situacao`, `receptor` (`ContatoResponse`), `enderecoRecebimento` (`EnderecoResponse`), `itens` (`List<ItemCampanhaResponse>`), `criadoEm`, `atualizadoEm` |
| `ItemCampanhaResponse` | `id`, `tipoItemDoacao`, `tipoUnidade`, `quantidade`, `descricao` |

---

## `CampanhaEmergencialService`

| Método | Regra | Exceções/transação |
|---|---|---|
| `criar(usuarioId, request)` | Localiza o receptor autenticado, converte DTO para entidade, vincula os itens e salva com situação `ATIVA` | `@Transactional`; lança `ForbiddenException` se o usuário não for receptor |
| `listarMinhas(usuarioId)` | Lista campanhas da ONG autenticada ordenadas por `criadoEm DESC` | `@Transactional(readOnly = true)` |
| `listarPorSituacao(situacao, pageable)` | Lista paginada de campanhas ativas/específicas para consulta pública/geral | `@Transactional(readOnly = true)` |
| `buscarPorId(id)` | Busca campanha com seus itens | `@Transactional(readOnly = true)`; lança `NotFoundException` se não encontrada |

---

## Endpoints

Base `/campanhas`.

| Método | Rota | Entrada | Saída | Sucesso | Acesso |
|---|---|---|---|---|---|
| POST | `/campanhas` | `CampanhaEmergencialRequest` | `CampanhaEmergencialResponse` + `Location` | 201 Created | `ROLE_RECEPTOR` |
| GET | `/campanhas/minhas` | — | `List<CampanhaEmergencialResponse>` | 200 OK | `ROLE_RECEPTOR` |
| GET | `/campanhas` | `situacao` (opcional), `Pageable` | `Page<CampanhaEmergencialResponse>` | 200 OK | Autenticado |
| GET | `/campanhas/{id}` | — | `CampanhaEmergencialResponse` | 200 OK | Autenticado |

---

## Segurança

- Criação e mutação restritas a `ROLE_RECEPTOR`. Qualquer outro perfil (Doador/Transportador) recebe HTTP 403 Forbidden.
- Visitantes não autenticados recebem HTTP 401 Unauthorized.
- DTO de resposta utiliza `ContatoResponse`, ocultando dados de documento pessoal (CPF/CNPJ) conforme LGPD.

---

## Banco de Dados (Flyway Migration V6)

- Criação de `campanhas_emergenciais` e `itens_campanha`.
- Chaves estrangeiras e integridade referencial com `ON DELETE CASCADE` para itens da campanha.
- Índices:
  - `idx_campanhas_receptor_criado` em `campanhas_emergenciais(receptor_id, criado_em DESC)`
  - `idx_campanhas_situacao_criado` em `campanhas_emergenciais(situacao, criado_em DESC)`
  - `idx_itens_campanha_id` em `itens_campanha(campanha_id)`

---

## Checklist de Testes

- [ ] Criação de campanha com sucesso por receptor (201 Created com Location e dados persistidos).
- [ ] Validação do limite de 1000 caracteres no campo descrição.
- [ ] Validação de campos obrigatórios (nome, endereço) retornando 400 Bad Request.
- [ ] Tentativa de criação por usuário não-receptor retornando 403 Forbidden.
- [ ] Tentativa de criação por usuário anônimo retornando 401 Unauthorized.
- [ ] Listagem de "minhas campanhas" retornando apenas as da ONG autenticada.
- [ ] Consulta pública paginada e busca por id com carregamento de itens.
