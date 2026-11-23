# PRD 0004: Necessidade de Doação e Critérios de Compatibilidade

- **Status:** Parcialmente substituído (RF-006 vigente; RF-007 substituído pelo [PRD 0006 — Reserva de Oferta e Vínculo de Transporte](./0006-reserva-e-vinculo-de-transporte.md))
- **Data:** 2026-11-09 (Atualizado: 2026-11-22)
- **Especificações:** [Spec 0004 — RF-006](../specs/0004-necessidade-de-doacao.md); RF-007 substituído por [Spec 0006](../specs/0006-reserva-e-vinculo-de-transporte.md)

---

## Descrição geral

O DoaLink permite que um Receptor (ONG) registre necessidades de doação declarando os itens e quantidades necessários e o local de entrega (`RF-006`).

> **Nota de evolução do produto:** A proposta original deste documento previa um mecanismo de sugestões automáticas de combinações (`RF-007` — matchmaking). Essa abordagem foi **substituída** pelo fluxo de listagens diretas, reserva explícita de ofertas por Receptores e vínculo de transporte definido no **[PRD 0006 — Reserva de Oferta e Vínculo de Transporte (RF-010)](./0006-reserva-e-vinculo-de-transporte.md)**. O `RF-007` não constitui pendência ativa para a versão atual.

O sistema **não faz**:
- Negociação de valores ou frete.
- Comunicação direta entre participantes.
- Garantia de execução da doação — a confirmação é de interesse dos participantes.
- Matchmaking automático entre ofertas, necessidades e transportes (`RF-007` substituído pelo PRD 0006).

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Receptor | ONG autenticada | Registrar necessidade e consultar suas necessidades (`RF-006`); consultar e reservar ofertas diretamente ([PRD 0006](./0006-reserva-e-vinculo-de-transporte.md)) |
| Doador | Doador autenticado | Consultar necessidades abertas |
| Transportador | Transportador autenticado | Consultar necessidades abertas |

---

## Requisitos funcionais

### RF-006 — Necessidade de doação

**Descrição:** O sistema permite que um Receptor registre uma necessidade de doação declarando de 1 a 50 itens necessários, suas quantidades e unidades, descrição opcional e o local de entrega. O sistema disponibiliza as necessidades abertas para consulta por participantes autenticados.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Itens necessários | Lista de ItemNecessidade | — | Obrigatório; mínimo 1 item, máximo 50 itens por necessidade. |
| \*Tipo do item ¹ | ENUM | — | Obrigatório por item. Deve usar as mesmas categorias do catálogo de RF-004 (`TipoItemDoacao`). |
| \*Unidade do item | ENUM (`TipoUnidade`) | — | Obrigatório por item (`KG`, `T`, `G`, `ML`, `CAIXAS`, `CESTAS_BASICAS`, `SEMUNIDADE`). |
| \*Quantidade necessária | Double | Conforme unidade | Obrigatório por item. Deve ser maior que zero (`> 0`). |
| Descrição do item | VARCHAR(500) | — | Opcional por item. Máximo de 500 caracteres. |
| \*Local de entrega ² | Endereço estruturado | Conforme campos de endereço | Obrigatório. CEP, estado (UF), cidade, bairro e logradouro obrigatórios. Número opcional (padrão `S/N`). Complemento opcional. |

¹ O catálogo de itens da necessidade utiliza o mesmo enum de categorias definido em RF-004.
² O endereço completo de entrega é obrigatório.

**Critérios de aceite**

- **CA-006-1:** Dado um Receptor autenticado com dados válidos, quando o Receptor registrar a necessidade, então o sistema cria o registro com situação `ABERTA` e o disponibiliza para consulta.
- **CA-006-2:** _(superado — RF-007 substituído pelo PRD 0006)_ Dado uma necessidade registrada, quando participantes autenticados consultarem as necessidades abertas, então a necessidade aparece na listagem.
- **CA-006-3:** Dado campos obrigatórios ausentes ou inválidos, quando o Receptor tentar registrar a necessidade, então o sistema rejeita com erro de validação.
- **CA-006-4:** Dado uma necessidade registrada pelo Receptor, quando o Receptor consultar suas necessidades, então o registro aparece na listagem pessoal.

---

### RF-007 — Critérios de compatibilidade (matchmaking) _(substituído pelo PRD 0006 — RF-010)_

> **Status do requisito:** **Substituído / Arquivado.** A arquitetura operacional vigente adota listagens diretas e reserva explícita de ofertas conforme o [PRD 0006](./0006-reserva-e-vinculo-de-transporte.md). Os critérios abaixo são mantidos apenas como histórico documental.

**Descrição:** O sistema sugere combinações entre ofertas de Doadores, necessidades de Receptores e disponibilidades de Transportadores com base em compatibilidade de tipo de item, quantidade, localização e data. As partes envolvidas podem aceitar ou recusar uma proposta gerada pelo sistema. O sistema registra e exibe a situação de cada proposta.

**Dados usados na combinação** (provenientes dos registros de RF-004, RF-005 e RF-006)

| Campo | Tipo sugerido | Validação e observações |
|---|---|---|
| Oferta de doação | Referência à oferta | Deve referenciar uma oferta com situação `DISPONIVEL`. |
| Necessidade do Receptor | Referência à necessidade | Deve referenciar uma necessidade cadastrada compatível com o tipo de item ofertado. |
| Tipo do item | ENUM (catálogo RF-004) | A oferta e a necessidade devem corresponder ao mesmo tipo de item ou a tipos considerados compatíveis (regra a definir). |
| Quantidade ofertada | Double | Deve ser maior que zero e compatível com a quantidade necessária. |
| Quantidade necessária | Double | Deve ser maior que zero. O sistema compara com a quantidade ofertada. |
| Disponibilidade de transporte | Referência à disponibilidade | Opcional. Quando a proposta exigir transporte, deve referenciar uma disponibilidade compatível com quantidade, origem, destino e data. |
| Localização de origem e entrega | Endereços de RF-004 e RF-006 | A origem da oferta e o local de entrega devem ser atendidos pelo transporte selecionado, quando houver. |
| Data | DATE ou TIMESTAMP | As datas de oferta, necessidade e disponibilidade de transporte devem ser compatíveis. |
| Resposta da proposta | ENUM | Valores: `PENDENTE`, `ACEITA`, `RECUSADA`. Registrada por participante. |
| Situação da proposta | ENUM | Deve indicar a situação atual da combinação. Estados e transições compatíveis com o fluxo da doação. A definir. |

**Critérios de aceite**

- **CA-007-1:** _(substituído pelo PRD 0006 — RF-010)_ Dado uma oferta e uma necessidade compatíveis em tipo de item, quantidade, localização e data, quando o sistema processar a compatibilidade, então apresenta uma proposta de combinação.
- **CA-007-2:** _(substituído pelo PRD 0006 — RF-010)_ Dado uma proposta que exige transporte, quando o sistema gerar a proposta, então inclui uma disponibilidade de Transportador compatível com a quantidade, a origem, o destino e a data.
- **CA-007-3:** _(substituído pelo PRD 0006 — RF-010)_ Dado nenhuma combinação compatível disponível, quando o sistema processar a compatibilidade, então informa que nenhuma proposta foi encontrada.
- **CA-007-4:** _(substituído pelo PRD 0006 — RF-010)_ Dado um participante envolvido na proposta, quando aceitar ou recusar a proposta, então o sistema registra a resposta.
- **CA-007-5:** _(substituído pelo PRD 0006 — RF-010)_ Dado uma resposta registrada, quando qualquer participante envolvido consultar a proposta, então a situação atualizada é exibida.
- **CA-007-6:** _(substituído pelo PRD 0006 — RF-010)_ Dado um participante não envolvido na proposta, quando tentar responder a ela, então o sistema impede com erro de autorização.

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** Dados de contato dos participantes (nome, e-mail, telefone) exibidos nas consultas autorizadas — nunca CPF/CNPJ.
- **RNF-02 (Consistência):** _(substituído pelo PRD 0006 — RF-010)_ Uma oferta marcada como `RESERVADA` ou `CONCLUIDA` não pode ser reservada novamente.

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Algoritmo de disparo do matchmaking (automático ou sob demanda) | RF-007 | Superado — substituído pelo fluxo de reserva direta do PRD 0006 (RF-010) |
| Tipos de item considerados compatíveis (mesmo enum ou conversão) | RF-006, RF-007 | Superado — substituído pelo PRD 0006 (RF-010) |
| Regras de conversão de unidade entre oferta e necessidade | RF-007 | Superado — substituído pelo PRD 0006 (RF-010) |
| Estados e transições da situação da proposta | RF-007 | Superado — substituído pelo PRD 0006 (RF-010) |
| Regras de visibilidade das necessidades (pública ou restrita) | RF-006 | Resolvida na Spec 0004: consulta autenticada por situação; rota pessoal restrita ao receptor |
| Cancelamento / edição de necessidade após registro | RF-006 | Resolvida na Spec 0004: fora do escopo da primeira versão |

**Decisões para RF-006:** necessidades autenticadas são consultáveis por situação; `GET /necessidades/minhas` é restrito ao receptor. RF-006 não oferece edição ou cancelamento nesta versão. A descrição do item é opcional (até 500 caracteres). A unidade é obrigatória e usa `TipoUnidade` compartilhado com ofertas. O `RF-007` (matchmaking automático) foi substituído pelo fluxo de reserva direta e vínculo de transporte descrito no `PRD 0006`.

---

## Fora do escopo desta versão

- Matchmaking automático entre ofertas, necessidades e disponibilidades (substituído pelo PRD 0006)
- Negociação de valores ou frete entre partes
- Chat ou comunicação direta entre participantes dentro do sistema
- Recomendação por inteligência artificial ou aprendizado de máquina

---

## Critérios de conclusão

Critérios de aceite do `RF-006` cobertos por testes automatizados passando (`Spec 0004`). O `RF-007` foi substituído pelo `PRD 0006` (`Spec 0006`).

