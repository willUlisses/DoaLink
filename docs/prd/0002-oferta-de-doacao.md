# PRD 0002: Oferta de Doação

- **Status:** Proposto
- **Data:** 2026-11-09
- **Especificações:** [Spec 0002 — Oferta de Doação](../specs/0002-oferta-de-doacao.md)

---

## Descrição geral

O DoaLink permite que um Doador registre uma oferta de doação contendo um ou mais itens categorizados, uma quantidade e, opcionalmente, um local de retirada diferente do endereço cadastrado. A oferta fica disponível para consulta por outros participantes autenticados e pode ser usada nos critérios de compatibilidade com necessidades de Receptores (RF-007).

O sistema **não faz**:
- Edição de oferta após a criação.
- Expiração automática de ofertas por prazo.
- Acompanhamento da entrega física da doação (ver RF-009).
- Comunicação direta entre Doador e Receptor dentro do sistema.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Doador | Pessoa física ou jurídica cadastrada como doador | Registrar oferta, consultar suas próprias ofertas, cancelar oferta |
| Transportador | Transportador autenticado | Consultar ofertas disponíveis |
| Receptor | ONG autenticada | Consultar ofertas disponíveis |

---

## Requisitos funcionais

### RF-004 — Oferta de doação

**Descrição:** O sistema permite que um Doador registre uma oferta com um ou mais itens. Cada item possui tipo, quantidade e descrição. A oferta pode incluir um local de retirada opcional. O sistema disponibiliza a oferta para consulta por participantes autenticados.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Itens da oferta | Lista de ItemDoacao | — | Obrigatório; mínimo 1 item, máximo 20 itens por oferta. |
| \*Tipo do item ¹ | ENUM | — | Obrigatório por item. Deve pertencer ao catálogo fixo de categorias. |
| \*Descrição do item | VARCHAR(100) | — | Obrigatório por item. |
| \*Quantidade | Double | Conforme unidade | Obrigatório por item. Deve ser maior que zero. Unidade fixada em quilogramas (KG) nesta versão. |
| Local de retirada ² | Endereço estruturado | Conforme campos de endereço | Opcional. Quando não informado, o local de retirada é definido em acordo entre as partes fora do sistema. |
| Situação da oferta | ENUM | — | Gerenciado pelo sistema. Valores: `DISPONIVEL`, `RESERVADA`, `CONCLUIDA`, `CANCELADA`. Valor inicial: `DISPONIVEL`. |

¹ Categorias de itens aceitas (catálogo fixo desta versão):
- `PERECIVEL` — Perecível
- `NAO_PERECIVEL` — Não Perecível
- `HIGIENE` — Higiene
- `ROUPAS_E_TEXTIL` — Roupas e Têxtil
- `MEDICAMENTOS` — Medicamentos
- `ELETRODOMESTICOS` — Eletrodomésticos

² Campos do endereço de retirada: CEP, estado (UF), cidade, bairro, logradouro; número opcional (padrão `S/N`); complemento opcional. Quando o endereço de retirada é informado, os campos obrigatórios do endereço devem estar preenchidos.

**Critérios de aceite**

- **CA-004-1:** Dado um Doador autenticado com dados de oferta válidos, quando o Doador registrar a oferta, então o sistema cria a oferta com situação `DISPONIVEL` e retorna os dados da oferta criada.
- **CA-004-2:** Dado uma oferta criada com situação `DISPONIVEL`, quando qualquer participante autenticado consultar a listagem de ofertas disponíveis, então o sistema exibe a oferta na listagem.
- **CA-004-3:** Dado uma lista de itens vazia, quando o Doador tentar registrar a oferta, então o sistema rejeita com erro de validação.
- **CA-004-4:** Dado um item com quantidade igual ou menor que zero, quando o Doador tentar registrar a oferta, então o sistema rejeita com erro de validação.
- **CA-004-5:** Dado um Doador autenticado como dono da oferta com situação `DISPONIVEL`, quando o Doador solicitar o cancelamento, então o sistema muda a situação para `CANCELADA`.
- **CA-004-6:** Dado um Doador que não é o dono da oferta, quando tentar cancelar, então o sistema recusa com erro de autorização.
- **CA-004-7:** Dado uma oferta com situação diferente de `DISPONIVEL`, quando o Doador dono tentar cancelar, então o sistema recusa com erro de autorização.
- **CA-004-8:** Dado uma oferta criada pelo Doador, quando o Doador consultar suas próprias ofertas, então a oferta aparece na listagem pessoal, independentemente da situação.

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** A API de resposta de ofertas públicas expõe apenas nome, e-mail e telefone do Doador (nunca CPF/CNPJ). O documento do Doador não é exposto em nenhum endpoint de consulta de ofertas.
- **RNF-02 (Desempenho):** A listagem pública de ofertas disponíveis deve suportar paginação. Filtros por categoria, cidade e estado são aplicáveis.
- **RNF-03 (Integridade):** A situação da oferta segue o fluxo de estados `DISPONIVEL → RESERVADA → CONCLUIDA / CANCELADA`. Transições inválidas são rejeitadas.

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Validade / expiração automática da oferta | RF-004 | Fora do escopo desta versão — decisão futura |
| Regras de visibilidade (quem vê ofertas de qual situação) | RF-004 | Em aberto para RF-007 |
| Duplicidade de itens com mesma categoria dentro de uma oferta | RF-004 | Permitida nesta versão |

---

## Fora do escopo desta versão

- Edição de oferta após a criação
- Expiração automática de ofertas por prazo definido
- Verificação de quantidade mínima por item
- Acompanhamento da entrega da doação (ver PRD 0005 — RF-009)

---

## Critérios de conclusão

Todos os critérios de aceite cobertos por testes automatizados passando (ver checklist em Spec 0002).

