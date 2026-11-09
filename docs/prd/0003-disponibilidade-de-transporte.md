# PRD 0003: Disponibilidade de Transporte

- **Status:** Proposto
- **Data:** 2026-11-09
- **Especificações:** _(a criar — Spec 0003)_

---

## Descrição geral

O DoaLink permite que um Transportador registre uma janela de disponibilidade de transporte, declarando o tipo de veículo, a capacidade, a origem, o destino e a data disponível. Essa disponibilidade é usada pelo sistema nos critérios de compatibilidade com ofertas de doação e necessidades de Receptores (RF-007).

O sistema **não faz**:
- Rastreamento do veículo em tempo real.
- Comunicação direta entre Transportador e Doador/Receptor.
- Cálculo de frete ou valores financeiros.
- Verificação documental do veículo ou do transportador.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Transportador | Pessoa física ou jurídica cadastrada como transportador | Registrar disponibilidade, consultar suas disponibilidades |
| Doador | Doador autenticado | Consultar disponibilidades para combinar com sua oferta (via matchmaking) |
| Receptor | ONG autenticada | Consultar disponibilidades compatíveis com sua necessidade (via matchmaking) |

---

## Requisitos funcionais

### RF-005 — Disponibilidade de transporte

**Descrição:** O sistema permite que um Transportador registre uma disponibilidade de transporte informando o tipo de veículo, a capacidade, a origem, o destino e a data. O sistema considera esses dados nos critérios de compatibilidade (RF-007).

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Tipo de veículo ¹ | ENUM ou catálogo | — | Obrigatório. Categorias a definir. |
| Capacidade ² | DECIMAL(12,3) | Conforme unidade | Deve ser maior que zero quando informada. Unidade e dimensão (peso, volume, quantidade) a definir. |
| \*Origem | Endereço estruturado | Conforme campos de endereço | Obrigatório. Local de início do transporte. Campos obrigatórios: CEP, estado (UF), cidade, bairro, logradouro. Número opcional (padrão `S/N`). Complemento opcional. |
| \*Destino ³ | Endereço ou região | — | Obrigatório. Destino ou área atendida. Formato a definir. |
| \*Data ⁴ | DATE ou TIMESTAMP WITH TIME ZONE | DD/MM/AAAA ou DD/MM/AAAA HH:mm | Obrigatória. Deve ser uma data futura. Precisão (somente data ou data e horário) a definir. |

¹ O catálogo de tipos de veículo não está definido nesta versão — ver Decisões pendentes.
² A unidade de medida de capacidade está a definir — pode ser peso (kg), volume (m³) ou quantidade de itens.
³ O formato do destino está a definir: pode ser endereço fixo, cidade, UF ou lista de regiões atendidas.
⁴ A precisão necessária (DATE vs TIMESTAMP) está a definir conforme a necessidade operacional.

**Critérios de aceite**

- **CA-005-1:** Dado um Transportador autenticado com dados de disponibilidade válidos, quando o Transportador registrar a disponibilidade, então o sistema cria o registro e o disponibiliza para o mecanismo de compatibilidade.
- **CA-005-2:** Dado uma disponibilidade registrada, quando o sistema processar critérios de compatibilidade (RF-007), então a capacidade, a origem, o destino e a data são considerados na sugestão.
- **CA-005-3:** Dado uma data passada, quando o Transportador tentar registrar a disponibilidade, então o sistema rejeita com erro de validação.
- **CA-005-4:** Dado uma disponibilidade registrada pelo Transportador, quando o Transportador consultar suas disponibilidades, então o registro aparece na listagem pessoal.

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** Dados de contato do Transportador (nome, e-mail, telefone) são exibidos em contexto de compatibilidade — nunca CPF/CNPJ.
- **RNF-02 (Integridade):** A disponibilidade só pode ser usada em uma combinação ativa por vez, impedindo sobreposição de compromissos para a mesma data.

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Categorias de tipos de veículo | RF-005 | Em aberto |
| Unidade de capacidade (peso, volume, quantidade) | RF-005, RF-007 | Em aberto |
| Precisão da data: DATE vs TIMESTAMP WITH TIME ZONE | RF-005 | Em aberto |
| Formato do destino: endereço fixo, cidade, UF ou região | RF-005, RF-007 | Em aberto |
| Regras de cancelamento / edição de disponibilidade | RF-005 | Em aberto |

---

## Fora do escopo desta versão

- Rastreamento em tempo real do veículo
- Cálculo de frete ou custos de transporte
- Verificação documental do transportador ou veículo
- Comunicação direta com Doador/Receptor

---

## Critérios de conclusão

Todos os critérios de aceite cobertos por testes automatizados passando. Decisões pendentes resolvidas antes do início da implementação da Spec 0003.

