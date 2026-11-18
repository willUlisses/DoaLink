# PRD 0005: Campanha Emergencial e Acompanhamento da Doação

- **Status:** Proposto
- **Data:** 2026-11-09
- **Especificações:** [Spec 0005](../specs/0005-campanha-emergencial.md) para RF-008 e _(Spec 0007 para RF-009 a criar)_

---

## Descrição geral

O DoaLink permite o registro de campanhas ou eventos emergenciais (ex.: desastres naturais) com região afetada e necessidades específicas. Receptores (ONGs) podem vincular-se a uma campanha, ampliando sua visibilidade para Doadores e Transportadores. O sistema também permite que participantes autorizados acompanhem a situação de uma doação ao longo do seu fluxo.

O sistema **não faz**:
- Rastreamento de carga em tempo real.
- Comunicação direta entre participantes.
- Gestão financeira de arrecadações ou fundos.
- Verificação de autenticidade de desastres ou necessidades declaradas.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Receptor (ONG) | ONG autenticada | Registrar campanha, vincular necessidades à campanha, acompanhar situação da doação |
| Doador | Doador autenticado | Visualizar campanhas ativas, acompanhar situação das suas doações |
| Transportador | Transportador autenticado | Visualizar campanhas ativas, acompanhar situação dos transportes vinculados |

---

## Requisitos funcionais

### RF-008 — Campanha ou evento emergencial

**Descrição:** O sistema permite o registro e gerenciamento de campanhas ou eventos de emergência por Receptores (ONGs), com endereço de recebimento de doações, descrição contextual sobre a causa e necessidades emergenciais vinculadas. A campanha agrupa necessidades e amplifica a visibilidade para Doadores e Transportadores.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Nome ou identificação da campanha | VARCHAR(150) | — | Obrigatório. Máximo de 150 caracteres. |
| Descrição da causa | VARCHAR(1000) | — | Opcional. Permite à ONG contextualizar a causa e detalhar a situação emergencial. Máximo de 1000 caracteres. |
| \*Endereço de recebimento de doações | Endereço completo | Conforme campos de endereço | Obrigatório. Local onde as doações físicas devem ser entregues. |
| Necessidades emergenciais | Lista de itens e quantidades | Segue os tipos de RF-006 | Opcional. Cada necessidade segue as mesmas regras de RF-006 (categoria, unidade e quantidade). |

**Critérios de aceite**

- **CA-008-1:** Dado um usuário autenticado com perfil Receptor (ONG) com dados válidos, quando registrar a campanha, então o sistema cria a campanha com situação ATIVA, endereço de recebimento, descrição textual e necessidades informadas.
- **CA-008-2:** Dado um usuário com perfil Doador, Transportador ou não autenticado, quando tentar criar ou editar uma campanha, então o sistema rejeita a operação com status de acesso negado.
- **CA-008-3:** Dado uma campanha ativa, quando participantes consultarem necessidades ou receberem sugestões de compatibilidade (RF-007), então a campanha e suas necessidades vinculadas são consideradas conforme as regras de visibilidade.

---

### RF-009 — Acompanhamento da doação (desejável)

**Descrição:** O sistema permite que participantes autorizados acompanhem a situação da doação ao longo do fluxo, desde a oferta até a entrega. O sistema registra cada atualização de situação com data e hora.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| Situação da doação | ENUM | — | Valores: `OFERTADA`, `COMBINADA`, `EM_TRANSPORTE`, `ENTREGUE`. |
| Data e hora da atualização | TIMESTAMP WITH TIME ZONE | DD/MM/AAAA HH:mm | Registro técnico de quando a situação mudou. |
| Responsável pela atualização ¹ | Referência ao participante | — | Necessário para identificar quem alterou a situação. Confirmação pendente. |

¹ A necessidade de registrar o responsável pela atualização depende de confirmação do responsável pelo produto.

**Critérios de aceite**

- **CA-009-1:** Dado um participante autorizado, quando consultar a situação de uma doação, então o sistema exibe a situação atual.
- **CA-009-2:** Dado uma atualização de situação registrada, quando qualquer participante autorizado consultar, então a situação exibida corresponde à última atualização registrada.

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** Dados de contato dos participantes (nome, e-mail, telefone) são exibidos nas campanhas e no acompanhamento — nunca CPF/CNPJ.
- **RNF-02 (Auditoria):** Cada mudança de situação da doação deve ser registrada com data/hora para rastreabilidade histórica.

---

## Decisões resolvidas (RF-008)

| Item | Requisitos afetados | Decisão |
|---|---|---|
| Perfil autorizado a criar/editar campanhas | RF-008 | Restrito exclusivamente a Receptores (ONGs) |
| Localização da campanha | RF-008 | Endereço onde as doações são recebidas (objeto de endereço estruturado) |
| Contextualização da causa | RF-008 | Campo de texto livre de até 1000 caracteres |

---

## Decisões pendentes
| Perfis autorizados a atualizar a situação da doação | RF-009 | Em aberto |
| Necessidade de registrar o responsável pela atualização | RF-009 | Em aberto |
| Transições válidas entre estados de situação da doação | RF-009 | Em aberto |

---

## Fora do escopo desta versão

- Rastreamento de carga em tempo real (GPS ou similar)
- Gestão financeira ou arrecadação monetária vinculada a campanhas
- Verificação externa de autenticidade de desastres declarados
- Comunicação direta entre participantes dentro do sistema

---

## Critérios de conclusão

Todos os critérios de aceite cobertos por testes automatizados passando. Decisões pendentes resolvidas antes do início das implementações das Specs 0006 e 0007.

