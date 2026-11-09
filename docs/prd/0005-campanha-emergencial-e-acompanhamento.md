# PRD 0005: Campanha Emergencial e Acompanhamento da Doação

- **Status:** Proposto
- **Data:** 2026-11-09
- **Especificações:** _(a criar — Spec 0006 para RF-008 e Spec 0007 para RF-009)_

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

**Descrição:** O sistema permite o registro de campanhas ou eventos de desastre com região afetada e necessidades emergenciais. A campanha agrupa necessidades e amplifica a visibilidade para Doadores e Transportadores. Usuários autorizados podem registrar e gerenciar campanhas.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Nome ou identificação da campanha | VARCHAR(150) | — | Obrigatório. ¹ |
| \*Região afetada ² | Endereço, município, estado ou área geográfica | Conforme campos de endereço | Obrigatório. Formato a definir. |
| Necessidades emergenciais | Lista de itens e quantidades | Segue os tipos de RF-006 | Opcional. Cada necessidade segue as mesmas regras de RF-006. |

¹ O campo de nome não estava detalhado no requisito original do PDF — confirmado como necessário.
² O formato da região afetada está a definir: texto livre, lista de municípios/estados, delimitação geográfica.

**Critérios de aceite**

- **CA-008-1:** Dado um usuário autorizado com dados de campanha válidos, quando registrar a campanha, então o sistema cria a campanha com região afetada e necessidades informadas.
- **CA-008-2:** Dado uma campanha ativa, quando participantes consultarem necessidades ou receberem sugestões de compatibilidade (RF-007), então a campanha e suas necessidades vinculadas são consideradas conforme as regras de visibilidade.

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

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Perfil autorizado a criar campanhas (Receptor, Administrador ou ambos) | RF-008 | Em aberto |
| Formato da região afetada (texto, municípios, UF, coordenadas) | RF-008 | Em aberto |
| Regras de visibilidade das campanhas (pública ou restrita por perfil) | RF-008 | Em aberto |
| Encerramento de campanhas: manual ou por prazo | RF-008 | Em aberto |
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

