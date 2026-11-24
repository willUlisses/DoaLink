# PRD 0005: Campanha Emergencial e Acompanhamento da Doação

- **Status:** Parcialmente implementado (RF-008 implementado para criação/consulta; RF-009 adiado — acompanhamento atual coberto pelo PRD 0006)
- **Data:** 2026-11-09 (Atualizado: 2026-11-22)
- **Especificações:** [Spec 0005](../specs/0005-campanha-emergencial.md) para RF-008 e _(Spec 0007 para RF-009 adiada)_

---

## Descrição geral

O DoaLink permite o registro e consulta de campanhas ou eventos emergenciais (ex.: desastres naturais) por Receptores (ONGs), com endereço de recebimento, descrição da causa e lista própria de itens emergenciais (`RF-008`).

> **Alinhamento com a implementação atual:**
> - **Implementado (RF-008):** Criação (`POST /campanhas`), listagem pessoal (`GET /campanhas/minhas`), listagem autenticada por situação (`GET /campanhas`) e consulta por ID (`GET /campanhas/{id}`), com itens cadastrados diretamente na campanha (`ItemCampanha`).
> - **Adiado / depende de alterações na API:**
>   1. Vínculo entre registros independentes de Necessidades (`RF-006`) e Campanhas Emergenciais (`RF-008`).
>   2. Edição, encerramento (`ENCERRADA`) e cancelamento (`CANCELADA`) de campanhas após a criação.
>   3. Histórico/timeline auditável de acompanhamento de doações (`RF-009` — atualmente, a situação atual das ofertas e entregas é acompanhada diretamente por `SituacaoOferta` e `SituacaoReservaEntrega` no **[PRD 0006](./0006-reserva-e-vinculo-de-transporte.md)**).

O sistema **não faz**:
- Rastreamento de carga em tempo real.
- Comunicação direta entre participantes.
- Gestão financeira de arrecadações ou fundos.
- Verificação de autenticidade de desastres ou necessidades declaradas.
- Vínculo direto de necessidades preexistentes (`RF-006`) a campanhas ou linha do tempo histórica de eventos (`RF-009`) na versão atual.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Receptor (ONG) | ONG autenticada | Registrar e consultar campanhas emergenciais (`RF-008`); acompanhar reservas no fluxo do PRD 0006 |
| Doador | Doador autenticado | Visualizar campanhas ativas; acompanhar situação atual das reservas de suas ofertas ([PRD 0006](./0006-reserva-e-vinculo-de-transporte.md)) |
| Transportador | Transportador autenticado | Visualizar campanhas ativas; acompanhar situação atual das entregas vinculadas ([PRD 0006](./0006-reserva-e-vinculo-de-transporte.md)) |

---

## Requisitos funcionais

### RF-008 — Campanha ou evento emergencial

**Descrição:** O sistema permite o registro e consulta de campanhas ou eventos de emergência por Receptores (ONGs), com endereço de recebimento de doações, descrição contextual sobre a causa e lista própria de itens emergenciais (até 50 itens). A edição, encerramento e cancelamento de campanhas, bem como o vínculo com necessidades independentes (`RF-006`), ficam adiados para evolução futura da API.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Nome ou identificação da campanha | VARCHAR(150) | — | Obrigatório. Máximo de 150 caracteres. |
| Descrição da causa | VARCHAR(1000) | — | Opcional. Permite à ONG contextualizar a causa e detalhar a situação emergencial. Máximo de 1000 caracteres. |
| \*Endereço de recebimento de doações | Endereço completo | Conforme campos de endereço | Obrigatório. Local onde as doações físicas devem ser entregues. |
| Itens emergenciais | Lista de ItemCampanha (máx. 50) | — | Opcional. Cadastrados diretamente na campanha (categoria, unidade, quantidade > 0 e descrição opcional até 500 chars). Vínculo com registros independentes de RF-006 adiado. |

**Critérios de aceite**

- **CA-008-1:** Dado um usuário autenticado com perfil Receptor (ONG) com dados válidos, quando registrar a campanha, então o sistema cria a campanha com situação `ATIVA`, endereço de recebimento, descrição textual e itens informados.
- **CA-008-2:** Dado um usuário com perfil Doador, Transportador ou não autenticado, quando tentar criar uma campanha, então o sistema rejeita a operação com status de acesso negado. _(Edição de campanha adiada.)_
- **CA-008-3:** _(parcialmente atendido sem RF-007)_ Dado uma campanha ativa, quando participantes autenticados consultarem as campanhas, então a campanha e seus itens emergenciais são exibidos conforme as regras de visibilidade.

---

### RF-009 — Acompanhamento da doação / histórico de etapas _(adiado — depende de alteração na API)_

> **Status do requisito:** **Adiado.** Não há endpoint nem tabela de histórico/timeline de eventos nesta versão. O acompanhamento operacional da situação atual das entregas ocorre pelo ciclo de estados de `ReservaEntrega` (`AGUARDANDO_TRANSPORTE`, `AGUARDANDO_RETIRADA`, `TRANSPORTE_VINCULADO`, `CONCLUIDA`, `CANCELADA`) definido no [PRD 0006](./0006-reserva-e-vinculo-de-transporte.md).

**Descrição:** O sistema permite que participantes autorizados acompanhem a situação da doação ao longo do fluxo, desde a oferta até a entrega. O sistema registra cada atualização de situação com data e hora.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| Situação da doação | ENUM | — | Valores propostos: `OFERTADA`, `COMBINADA`, `EM_TRANSPORTE`, `ENTREGUE`. |
| Data e hora da atualização | TIMESTAMP WITH TIME ZONE | DD/MM/AAAA HH:mm | Registro técnico de quando a situação mudou. |
| Responsável pela atualização ¹ | Referência ao participante | — | Necessário para identificar quem alterou a situação. Confirmação pendente. |

¹ A necessidade de registrar o responsável pela atualização depende de confirmação do responsável pelo produto.

**Critérios de aceite**

- **CA-009-1:** _(atendido parcialmente pelo estado atual de ReservaEntrega no PRD 0006; histórico detalhado adiado)_ Dado um participante autorizado, quando consultar a situação de uma doação, então o sistema exibe a situação atual.
- **CA-009-2:** _(adiado — depende de endpoint de histórico/timeline)_ Dado uma atualização de situação registrada, quando qualquer participante autorizado consultar o histórico, então as transições registradas são exibidas com data e hora.

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** Dados de contato dos participantes (nome, e-mail, telefone) são exibidos nas campanhas e no acompanhamento — nunca CPF/CNPJ.
- **RNF-02 (Auditoria):** Cada mudança de situação da doação deve ser registrada com data/hora para rastreabilidade histórica _(adiado para implementação do histórico no RF-009; versão atual mantém `criadoEm` e `atualizadoEm` nas entidades)_.

---

## Decisões resolvidas (RF-008)

| Item | Requisitos afetados | Decisão |
|---|---|---|
| Perfil autorizado a criar campanhas | RF-008 | Restrito exclusivamente a Receptores (ONGs) |
| Localização da campanha | RF-008 | Endereço onde as doações são recebidas (objeto de endereço estruturado) |
| Contextualização da causa | RF-008 | Campo de texto livre de até 1000 caracteres |

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Vínculo entre registros independentes de Necessidade (RF-006) e Campanha (RF-008) | RF-006, RF-008 | Adiado — depende de evolução da API |
| Edição, encerramento (`ENCERRADA`) e cancelamento (`CANCELADA`) de campanhas | RF-008 | Adiado — depende de novos endpoints na API |
| Perfis autorizados a atualizar a situação da doação no histórico | RF-009 | Em aberto / Adiado |
| Necessidade de registrar o responsável pela atualização | RF-009 | Em aberto / Adiado |
| Transições válidas e modelo de timeline de histórico da doação | RF-009 | Em aberto / Adiado |

---

## Fora do escopo desta versão

- Edição, encerramento e cancelamento de campanhas emergenciais após a publicação
- Vínculo direto de necessidades preexistentes (`RF-006`) a campanhas
- Linha do tempo / histórico auditável de transições intermediárias (`RF-009`)
- Rastreamento de carga em tempo real (GPS ou similar)
- Gestão financeira ou arrecadação monetária vinculada a campanhas
- Verificação externa de autenticidade de desastres declarados
- Comunicação direta entre participantes dentro do sistema

---

## Critérios de conclusão

Critérios de aceite do `RF-008` cobertos na `Spec 0005`. A implementação do `RF-009` (timeline histórica) e a gestão de ciclo de vida/vínculos de campanhas ficam condicionadas à resolução das decisões pendentes e criação dos respectivos endpoints na API.
