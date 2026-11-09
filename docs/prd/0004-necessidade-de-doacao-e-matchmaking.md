# PRD 0004: Necessidade de Doação e Critérios de Compatibilidade

- **Status:** Proposto
- **Data:** 2026-11-09
- **Especificações:** _(a criar — Spec 0004 para RF-006 e Spec 0005 para RF-007)_

---

## Descrição geral

O DoaLink permite que um Receptor (ONG) registre necessidades de doação declarando os itens e quantidades necessários e o local de entrega. Com base nas necessidades registradas, nas ofertas de Doadores (RF-004) e nas disponibilidades de Transportadores (RF-005), o sistema sugere combinações compatíveis (matchmaking). As partes envolvidas podem aceitar ou recusar uma proposta de combinação.

O sistema **não faz**:
- Negociação de valores ou frete.
- Comunicação direta entre participantes.
- Garantia de execução da doação — a confirmação é de interesse dos participantes.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Receptor | ONG autenticada | Registrar necessidade, consultar suas necessidades, aceitar ou recusar propostas |
| Doador | Doador autenticado | Consultar propostas que envolvem suas ofertas, aceitar ou recusar |
| Transportador | Transportador autenticado | Consultar propostas que envolvem sua disponibilidade, aceitar ou recusar |

---

## Requisitos funcionais

### RF-006 — Necessidade de doação

**Descrição:** O sistema permite que um Receptor registre uma necessidade de doação declarando os itens necessários, as quantidades e o local de entrega. O sistema disponibiliza a necessidade para análise de compatibilidade.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Item necessário ¹ | ENUM ou catálogo | — | Obrigatório. Deve usar as mesmas categorias do catálogo de RF-004. |
| \*Quantidade necessária | DECIMAL(12,3) | Conforme unidade | Obrigatório. Deve ser maior que zero. Unidade compatível com as ofertas. |
| \*Local de entrega ² | Endereço estruturado | Conforme campos de endereço | Obrigatório. CEP, estado (UF), cidade, bairro e logradouro obrigatórios. Número opcional (padrão `S/N`). Complemento opcional. |

¹ O catálogo de itens da necessidade deve ser compatível com o catálogo de itens da oferta (mesmo enum de categorias definido em RF-004).
² O endereço completo de entrega é obrigatório para permitir o cálculo de compatibilidade de rotas.

**Critérios de aceite**

- **CA-006-1:** Dado um Receptor autenticado com dados válidos, quando o Receptor registrar a necessidade, então o sistema cria o registro e o disponibiliza para o mecanismo de compatibilidade.
- **CA-006-2:** Dado uma necessidade registrada, quando o sistema processar critérios de compatibilidade (RF-007), então a necessidade é considerada na sugestão de combinação.
- **CA-006-3:** Dado campos obrigatórios ausentes ou inválidos, quando o Receptor tentar registrar a necessidade, então o sistema rejeita com erro de validação.
- **CA-006-4:** Dado uma necessidade registrada pelo Receptor, quando o Receptor consultar suas necessidades, então o registro aparece na listagem pessoal.

---

### RF-007 — Critérios de compatibilidade (matchmaking)

**Descrição:** O sistema sugere combinações entre ofertas de Doadores, necessidades de Receptores e disponibilidades de Transportadores com base em compatibilidade de tipo de item, quantidade, localização e data. As partes envolvidas podem aceitar ou recusar uma proposta gerada pelo sistema. O sistema registra e exibe a situação de cada proposta.

**Dados usados na combinação** (provenientes dos registros de RF-004, RF-005 e RF-006)

| Campo | Tipo sugerido | Validação e observações |
|---|---|---|
| Oferta de doação | Referência à oferta | Deve referenciar uma oferta com situação `DISPONIVEL`. |
| Necessidade do Receptor | Referência à necessidade | Deve referenciar uma necessidade cadastrada compatível com o tipo de item ofertado. |
| Tipo do item | ENUM (catálogo RF-004) | A oferta e a necessidade devem corresponder ao mesmo tipo de item ou a tipos considerados compatíveis (regra a definir). |
| Quantidade ofertada | DECIMAL(12,3) | Deve ser maior que zero e compatível com a quantidade necessária. |
| Quantidade necessária | DECIMAL(12,3) | Deve ser maior que zero. O sistema compara com a quantidade ofertada. |
| Disponibilidade de transporte | Referência à disponibilidade | Opcional. Quando a proposta exigir transporte, deve referenciar uma disponibilidade compatível com quantidade, origem, destino e data. |
| Localização de origem e entrega | Endereços de RF-004 e RF-006 | A origem da oferta e o local de entrega devem ser atendidos pelo transporte selecionado, quando houver. |
| Data | DATE ou TIMESTAMP | As datas de oferta, necessidade e disponibilidade de transporte devem ser compatíveis. |
| Resposta da proposta | ENUM | Valores: `PENDENTE`, `ACEITA`, `RECUSADA`. Registrada por participante. |
| Situação da proposta | ENUM | Deve indicar a situação atual da combinação. Estados e transições compatíveis com o fluxo da doação. A definir. |

**Critérios de aceite**

- **CA-007-1:** Dado uma oferta e uma necessidade compatíveis em tipo de item, quantidade, localização e data, quando o sistema processar a compatibilidade, então apresenta uma proposta de combinação.
- **CA-007-2:** Dado uma proposta que exige transporte, quando o sistema gerar a proposta, então inclui uma disponibilidade de Transportador compatível com a quantidade, a origem, o destino e a data.
- **CA-007-3:** Dado nenhuma combinação compatível disponível, quando o sistema processar a compatibilidade, então informa que nenhuma proposta foi encontrada.
- **CA-007-4:** Dado um participante envolvido na proposta, quando aceitar ou recusar a proposta, então o sistema registra a resposta.
- **CA-007-5:** Dado uma resposta registrada, quando qualquer participante envolvido consultar a proposta, então a situação atualizada é exibida.
- **CA-007-6:** Dado um participante não envolvido na proposta, quando tentar responder a ela, então o sistema impede com erro de autorização.

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** Dados de contato dos participantes (nome, e-mail, telefone) exibidos nas propostas — nunca CPF/CNPJ.
- **RNF-02 (Consistência):** Uma oferta marcada como `RESERVADA` ou `CONCLUIDA` não entra em novas combinações.

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Algoritmo de disparo do matchmaking (automático ou sob demanda) | RF-007 | Em aberto |
| Tipos de item considerados compatíveis (mesmo enum ou conversão) | RF-006, RF-007 | Em aberto |
| Regras de conversão de unidade entre oferta e necessidade | RF-007 | Em aberto |
| Estados e transições da situação da proposta | RF-007 | Em aberto |
| Regras de visibilidade das necessidades (pública ou restrita) | RF-006 | Em aberto |
| Cancelamento / edição de necessidade após registro | RF-006 | Em aberto |

---

## Fora do escopo desta versão

- Negociação de valores ou frete entre partes
- Chat ou comunicação direta entre participantes dentro do sistema
- Recomendação por inteligência artificial ou aprendizado de máquina

---

## Critérios de conclusão

Todos os critérios de aceite cobertos por testes automatizados passando. Decisões pendentes resolvidas antes do início da implementação das Specs 0004 e 0005.

