# Spec 0004: Necessidade de Doação

- **Status:** Proposto
- **Requisitos:** RF-006, CA-006-1 a CA-006-4 do [PRD 0004](../prd/0004-necessidade-de-doacao-e-matchmaking.md)
- **Decisões:** [ADR 0006](../adr/0006-oferta-agrupa-n-itens.md)
- **Depende de:** [Spec 0002](0002-oferta-de-doacao.md)

## Entidades

`NecessidadeDoacao` (`necessidades_doacao`) tem `id`, `receptor_id` (`ManyToOne LAZY`, obrigatório), `localEntrega` (`Endereco` embutido, colunas sem prefixo), `situacao` (`SituacaoNecessidade`, STRING, obrigatório, padrão `ABERTA`), `criado_em` e `atualizado_em` preenchidos em `@PrePersist`/`@PreUpdate`, e itens `ItemDoacao` com `cascade=ALL`, `orphanRemoval=true`. `SituacaoNecessidade` contém `ABERTA`, `ATENDIDA` e `CANCELADA`.

`ItemDoacao` é a única entidade de item para oferta e necessidade. Seus campos comuns são descrição opcional (até 500), categoria e unidade obrigatórias como STRING, quantidade `Double` positiva. Cada item referencia exatamente uma oferta ou uma necessidade; a constraint `ck_itens_doacao_vinculo` garante essa exclusividade.

## DTOs

| Record | Campos e validação |
|---|---|
| `NecessidadeDoacaoRequest` | `itens`: `@NotEmpty @Size(max=50) @Valid`; `localEntrega`: `@NotNull @Valid` |
| `ItemNecessidadeRequest` | `tipoItemDoacao` e `tipoUnidade`: `@NotNull`; `quantidade`: `@NotNull @Positive Double`; `descricao`: opcional, máximo 500 |
| `NecessidadeDoacaoResponse` | `id`, `situacao`, `receptor` (`ContatoResponse` sem documento), `localEntrega`, itens, timestamps |
| `ItemNecessidadeResponse` | `id`, `descricao`, `tipoItemDoacao`, `tipoUnidade`, `quantidade`; representa os campos comuns de `ItemDoacao` |

`EnderecoRequest` exige CEP, UF, cidade, bairro e logradouro. Número é opcional e assume `S/N` quando omitido; complemento também é opcional.

## `NecessidadeDoacaoService`

| Método | Regra | Exceções/transação |
|---|---|---|
| `criar(usuarioId, request)` | Busca receptor, cria necessidade `ABERTA` e retorna response | `@Transactional`; 403 se usuário não for receptor |
| `listarMinhas(usuarioId)` | Lista próprias necessidades por data decrescente | `@Transactional(readOnly=true)` |
| `listarPorSituacao(situacao, pageable)` | Consulta necessidades pela situação, padrão `ABERTA` na API | `@Transactional(readOnly=true)` |
| `buscarPorId(id)` | Busca com itens | `@Transactional(readOnly=true)`; 404 se ausente |

## Repositórios e mapper

- `NecessidadeDoacaoRepository`: `findByReceptorIdOrderByCriadoEmDesc`, `findBySituacao` paginado e `findWithItensById` com `@EntityGraph`.
- `ReceptorRepository`: repositório JPA para `Receptor`.
- `NecessidadeDoacaoMapper.toEntity(request, receptor)` mapeia endereço com `EnderecoMapper` e itens mantendo ambos os lados.

## Endpoints

Base `/necessidades`.

| Método | Rota | Entrada | Saída | Sucesso | Acesso |
|---|---|---|---|---|---|
| POST | `/necessidades` | `NecessidadeDoacaoRequest` | response + `Location` | 201 | `ROLE_RECEPTOR` |
| GET | `/necessidades/minhas` | — | lista | 200 | `ROLE_RECEPTOR` |
| GET | `/necessidades?situacao=ABERTA` | paginação e situação opcional | página | 200 | autenticado |
| GET | `/necessidades/{id}` | — | response | 200 | autenticado |

As necessidades são consultáveis por todos os usuários autenticados para compatibilidade do RF-007; o endpoint pessoal é restrito ao receptor. Não se implementa cancelamento/edição nesta versão.

## Unidade de oferta

`ItemDoacao.tipoUnidade` será exposta em request/response e persistida como STRING, alinhando ofertas e necessidades. A migration converte os ordinais existentes para os nomes atuais de `TipoUnidade`.

## Segurança

Autenticação por cookie existente. 401 para visitante; 403 para perfil incorreto na criação; respostas não incluem CPF/CNPJ.

## Banco

Flyway cria `necessidades_doacao`, adiciona `necessidade_id` em `itens_doacao`, FKs e constraint garantindo associação com uma única raiz; cria índices por receptor/data, situação/data e necessidade. Também converte unidade ordinal para VARCHAR; quantidade permanece FLOAT8 (Double).

## Limites aceitos

Não há cancelamento, edição, deduplicação de itens ou conversão entre unidades neste RF. A compatibilidade/conversão fica para RF-007.

## Testes

- [x] Criação, lista pessoal, listagem por situação e busca por id.
- [x] Validações de itens/endereço, usuário não receptor e recurso inexistente.
- [x] Segurança: visitante, perfil incorreto e perfil autorizado.
