# Spec 0002: Oferta de Doação

- **Status:** Implementado
- **Requisitos:** RF-004, CA-004-1, CA-004-2 do `implementacao-doalink.md`
- **Decisões:** [ADR 0006](../adr/0006-oferta-agrupa-n-itens.md)
- **Depende de:** [Spec 0001](0001-autenticacao-e-cadastro.md)

---

## 1. Visão Geral

Permite ao doador registrar uma oferta de doação contendo um ou mais itens, um local de retirada opcional e uma situação que evolui ao longo do ciclo de vida da oferta. A oferta fica disponível para consulta e compatibilidade por outros participantes (CA-004-2).

---

## 2. Entidades do Domínio

### `OfertaDoacao` (tabela `ofertas_doacao`)

| Campo | Tipo Java | Coluna | Regras |
|---|---|---|---|
| `id` | `Long` | `id` PK | Gerado automaticamente |
| `doador` | `Doador` | `doador_id` FK | `NOT NULL`, `@ManyToOne LAZY` |
| `localRetirada` | `Endereco` | `local_*` (prefixo) | Opcional — `@Embedded @AttributeOverrides` |
| `situacao` | `SituacaoOferta` | `situacao` | `NOT NULL`, default `DISPONIVEL` |
| `itens` | `List<ItemDoacao>` | — | `@OneToMany cascade=ALL orphanRemoval=true`, mapeado por `oferta` |
| `criadoEm` | `LocalDateTime` | `criado_em` | `NOT NULL updatable=false`, preenchido em `@PrePersist` |
| `atualizadoEm` | `LocalDateTime` | `atualizado_em` | Atualizado em `@PreUpdate` |

**Enum `SituacaoOferta`:** `DISPONIVEL`, `RESERVADA`, `CONCLUIDA`, `CANCELADA`

**Método auxiliar:** `adicionarItem(ItemDoacao item)` — mantém os dois lados da relação bidirecional.

---

### `ItemDoacao` (tabela `itens_doacao`)

| Campo | Tipo Java | Coluna | Regras |
|---|---|---|---|
| `id` | `Long` | `id` PK | Gerado automaticamente |
| `descricao` | `String` | `descricao` | `NOT NULL`, `length=100` |
| `tipoItemDoacao` | `TipoItemDoacao` | `tipo_item_doacao` | `NOT NULL`, `@Enumerated(STRING)` |
| `quantidade` | `BigDecimal` | `quantidade` | `NOT NULL`, `precision=12 scale=3` |
| `oferta` | `OfertaDoacao` | `oferta_id` FK | `NOT NULL`, `@ManyToOne LAZY` |

**Enum `TipoItemDoacao`:** `PERECIVEL`, `NAO_PERECIVEL`, `HIGIENE`, `ROUPAS_E_TEXTIL`, `MEDICAMENTOS`, `ELETRODOMESTICOS`

> Unidade de medida está fixada em KG no back-end; não é exposta na API nesta versão.

---

### Alterações em `Usuario`

| Campo novo | Tipo Java | Coluna | Regras |
|---|---|---|---|
| `telefone` | `String` | `telefone` | Opcional |
| `endereco` | `Endereco` | `endereco_*` (prefixo) | Opcional — `@Embedded @AttributeOverrides` |

O `Receptor` mantém seu próprio endereço obrigatório (`enderecoOrganizacao`) com prefixo `receptor_*`.

---

## 3. DTOs

### Requests

| DTO | Campo | Tipo | Validação |
|---|---|---|---|
| `OfertaDoacaoRequest` | `itens` | `List<ItemDoacaoRequest>` | `@NotEmpty`, `@Size(max=20)`, `@Valid` |
| | `endereco` | `EnderecoRequest` | Opcional — sem `@NotNull`; `@Valid` quando presente |
| `ItemDoacaoRequest` | `descricao` | `String` | `@NotBlank`, `@Size(max=100)` |
| | `tipoItemDoacao` | `TipoItemDoacao` | `@NotNull` |
| | `quantidade` | `BigDecimal` | `@NotNull`, `@Positive` |

### Responses

| DTO | Campos |
|---|---|
| `OfertaDoacaoResponse` | `id`, `situacao`, `doador` (ContatoResponse), `localRetirada` (EnderecoResponse), `itens` (List\<ItemDoacaoResponse\>), `criadoEm`, `atualizadoEm` |
| `ItemDoacaoResponse` | `id`, `descricao`, `tipoItemDoacao`, `quantidade` |
| `ContatoResponse` | `nome`, `email`, `telefone` — nunca expõe `documento` (CPF/CNPJ) |

---

## 4. `OfertaDoacaoService`

| Método | Regra de negócio | Exceções |
|---|---|---|
| `criar(Long usuarioId, OfertaDoacaoRequest)` | Carrega o `Doador` pelo id do principal; mapeia endereço e itens; salva com `situacao = DISPONIVEL`; retorna `OfertaDoacaoResponse`. `@Transactional`. | `ForbiddenException` se id não corresponder a um `Doador` |
| `listarMinhas(Long usuarioId)` | Retorna todas as ofertas do doador, ordenadas por `criadoEm DESC`. `@Transactional(readOnly=true)`. | — |
| `listarDisponiveis(Pageable, TipoItemDoacao, String cidade, String estado)` | Retorna página de ofertas com `situacao = DISPONIVEL`; aplica filtros opcionais (categoria, cidade, UF) em memória sobre a página. `@Transactional(readOnly=true)`. | — |
| `buscarPorId(Long id)` | Busca oferta com `@EntityGraph` para evitar N+1. `@Transactional(readOnly=true)`. | `NotFoundException` se não encontrada |
| `cancelar(Long usuarioId, Long ofertaId)` | Verifica se o doador é dono (403) e se `situacao == DISPONIVEL` (403); muda para `CANCELADA`. `@Transactional`. | `NotFoundException`, `ForbiddenException` |

---

## 5. `OfertaDoacaoRepository`

| Método | Descrição |
|---|---|
| `findByDoadorIdOrderByCriadoEmDesc(Long doadorId)` | Minhas ofertas. `@EntityGraph(attributePaths = "itens")` |
| `findBySituacao(SituacaoOferta, Pageable)` | Consulta pública paginada. `@EntityGraph(attributePaths = "itens")` |
| `findWithItensById(Long id)` | Busca por id com itens. `@EntityGraph(attributePaths = "itens")` |

---

## 6. `DoadorRepository`

| Método | Descrição |
|---|---|
| `findById(Long id)` | Herdado de `JpaRepository` — usado pelo service para verificar se o principal é doador |
| `findByEmail(String email)` | Busca por e-mail |

---

## 7. Endpoints (`OfertaDoacaoController`)

Base: `/ofertas`

| Método | Rota | Entrada | Saída | Sucesso | Erros | Acesso |
|---|---|---|---|---|---|---|
| POST | `/ofertas` | `OfertaDoacaoRequest` | `OfertaDoacaoResponse` + `Location` | 201 | 400 validação, 403 não é doador | `ROLE_DOADOR` |
| GET | `/ofertas/minhas` | — | `List<OfertaDoacaoResponse>` | 200 | — | `ROLE_DOADOR` |
| GET | `/ofertas` | `?categoria`, `?cidade`, `?estado`, paginação | `Page<OfertaDoacaoResponse>` | 200 | — | Autenticado |
| GET | `/ofertas/{id}` | — | `OfertaDoacaoResponse` | 200 | 404 | Autenticado |
| PATCH | `/ofertas/{id}/cancelar` | — | — | 204 | 403 não é dono ou situação inválida, 404 | `ROLE_DOADOR` |

---

## 8. Segurança (`SecurityConfig`)

```java
.requestMatchers(HttpMethod.POST, "/ofertas").hasRole("DOADOR")
.requestMatchers("/ofertas/minhas").hasRole("DOADOR")
.requestMatchers(HttpMethod.PATCH, "/ofertas/*/cancelar").hasRole("DOADOR")
// GET /ofertas e GET /ofertas/{id} caem em anyRequest().authenticated()
```

O `RequestFilter` já popula a authority `ROLE_<TipoUsuario>` a partir do claim `role` do JWT, portanto `hasRole()` funciona sem mudança no filtro.

---

## 9. Mappers

| Mapper | Método | Responsabilidade |
|---|---|---|
| `OfertaDoacaoMapper` | `toEntity(OfertaDoacaoRequest, Doador)` | Mapeia endereço (via `EnderecoMapper`) e delega cada item ao `ItemDoacaoMapper`; chama `adicionarItem()` para manter os dois lados da relação |
| `ItemDoacaoMapper` | `toEntity(ItemDoacaoRequest)` | Trim na descrição, unidade fixada em `KG` |

---

## 10. Banco

Migrations a criar (Flyway):

- Alterações em `usuarios`: adicionar colunas `telefone`, `endereco_*`.
- Alterar `receptores`: renomear `endereco_*` para `receptor_*` (endereço da organização).
- Nova tabela `ofertas_doacao`: `id`, `doador_id`, `local_*`, `situacao`, `criado_em`, `atualizado_em`.
- Nova tabela `itens_doacao`: `id`, `oferta_id`, `descricao`, `tipo_item_doacao`, `quantidade`.

---

## 11. Limites Aceitos

- Filtros de categoria/cidade/UF são aplicados **em memória** sobre a página retornada. O `totalElements` pode divergir do count real quando filtros estão ativos. Queries JPA especializadas entram numa evolução futura.
- Não há verificação de quantidade mínima ou duplicidade de itens dentro de uma oferta (dois itens com mesma categoria são permitidos).
- Validade/expiração da oferta está fora do escopo desta versão (decisão pendente 9 de `implementacao-doalink.md`).
- Edição de oferta após criação está fora do escopo.

---

## 12. Testes

- [x] `POST /ofertas` com doador autenticado retorna 201 + `Location: /ofertas/{id}`
- [x] `POST /ofertas` com lista vazia retorna 400
- [x] `POST /ofertas` com quantidade ≤ 0 retorna 400
- [x] `POST /ofertas` com `tipoItemDoacao` nulo retorna 400
- [x] `POST /ofertas` com descrição em branco retorna 400
- [x] `GET /ofertas/minhas` retorna lista do doador autenticado
- [x] `GET /ofertas/{id}` retorna 200 para oferta existente
- [x] `GET /ofertas/{id}` retorna 404 para oferta inexistente
- [x] `PATCH /ofertas/{id}/cancelar` retorna 204 quando doador é o dono
- [x] `PATCH /ofertas/{id}/cancelar` retorna 403 quando doador não é o dono
- [x] `PATCH /ofertas/{id}/cancelar` retorna 404 para oferta inexistente
- [x] `criar()` no service salva oferta com N itens
- [x] `criar()` lança `ForbiddenException` quando usuário não é doador
- [x] `criar()` aceita oferta sem endereço
- [x] `listarMinhas()` retorna lista vazia quando doador não tem ofertas
- [x] `cancelar()` muda situação para `CANCELADA`
- [x] `cancelar()` lança `ForbiddenException` quando não é o dono
- [x] `cancelar()` lança `ForbiddenException` quando situação não é `DISPONIVEL`
- [x] `cancelar()` lança `NotFoundException` para oferta inexistente
