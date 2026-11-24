# PRD 0007: Fluxo de Transporte por Códigos de Segurança

- **Status:** Proposto
- **Data:** 2026-11-24
- **Especificações:** [Spec 0006 — Reserva de Oferta e Fluxo de Transporte](../specs/0006-reserva-e-vinculo-de-transporte.md)
- **ADRs relacionados:** [ADR 0013 — Fluxo de transporte sem disponibilidade prévia, validação por códigos com hash e suspensão automática por expiração](../adr/0013-fluxo-de-transporte-por-codigos-com-hash-e-suspensao-por-expiracao.md)

---

## Descrição geral

O DoaLink conecta doadores, ONGs receptoras e transportadores, sem executar nem gerenciar a operação logística das doações. Toda a combinação de horários, veículo e trajeto acontece fora da plataforma diretamente entre os participantes.

Para proteger os envolvidos após a reserva de uma oferta que solicita transporte, o sistema substitui o cadastro prévio de disponibilidade de veículos por um fluxo direto de aceitação de transporte e confirmação em duas etapas presenciais com códigos numéricos de uso único: um código de retirada autorizado pelo doador e um código de entrega com validade de 24 horas disponibilizado ao receptor.

O sistema **não faz** nesta versão:
- Cadastro ou filtro de disponibilidade de veículos, capacidades ou agendas de transporte.
- Rastreamento GPS em tempo real ou montagem/otimização automática de rotas logísticas.
- Chat interno ou intermediação financeira de frete.
- Painel administrativo para análise manual de ocorrências e reativação de contas suspensas (previsto para etapa administrativa posterior).

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Transportador | Pessoa física ou jurídica ativa na plataforma | Visualizar ofertas que aguardam transporte, aceitar uma operação, combinar retirada e entrega externamente, confirmar retirada e entrega por código e consultar seu histórico |
| Doador | Pessoa física ou jurídica dona da oferta | Acompanhar a reserva, autorizar o início do transporte e consultar o código de retirada para informar ao motorista na coleta física |
| Receptor | ONG autenticada que reservou a oferta | Acompanhar o transporte em andamento, consultar o código de entrega com validade de 24 horas e fornecê-lo ao motorista somente após o recebimento físico dos itens |

---

## Requisitos funcionais

### RF-011 — Aceitação de transporte pelo transportador

**Descrição:** O sistema permite que um Transportador com conta ativa visualize reservas que aguardam transporte e aceite assumir uma operação, vinculando-se a ela de forma exclusiva e liberando os contatos e endereços completos para combinação externa.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Identificador da reserva | Referência | — | Obrigatório. A reserva deve solicitar transporte e aguardar transportador. |
| Transportador responsável | Referência | — | Definido exclusivamente pela conta autenticada do transportador ativo. |
| Data e hora da aceitação | TIMESTAMP | DD/MM/AAAA HH:mm | Preenchida automaticamente pelo sistema no momento do aceite. |

**Critérios de aceite**

- **CA-011-1:** Dado um Transportador ativo e uma reserva aguardando transporte, quando aceitar o transporte, então o sistema vincula o transportador à reserva, registra data e hora da aceitação, altera a situação para aguardar autorização do doador e exibe a operação em "Meus transportes" com contatos e endereços completos.
- **CA-011-2:** Dado dois Transportadores tentando aceitar simultaneamente a mesma reserva, quando ocorrer concorrência, então apenas o primeiro conclui o aceite e o segundo recebe erro de conflito.
- **CA-011-3:** Dado um Transportador com conta suspensa, quando tentar aceitar qualquer transporte, então o sistema recusa a operação com erro de permissão.
- **CA-011-4:** Dado um Transportador vinculado a uma operação, quando consultar a oferta ou reserva, então não pode editar informações da oferta nem da necessidade.

---

### RF-012 — Autorização do início pelo doador e confirmação da retirada

**Descrição:** Após um transportador aceitar a operação, o Doador dono da oferta autoriza o início do transporte, gerando um código de retirada de uso único que o transportador deve informar no sistema para comprovar o recebimento físico dos itens.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Código de confirmação de retirada | CHAR(6) | 000000 | Obrigatório para confirmar a retirada; exatamente 6 dígitos numéricos, uso único, nunca armazenado em texto puro e com limite de 5 tentativas. |

**Critérios de aceite**

- **CA-012-1:** Dado um Doador dono de oferta com transportador vinculado aguardando autorização, quando acionar "Autorizar início do transporte", então o sistema gera um código de retirada de 6 dígitos visível apenas ao doador e altera a situação para aguardar confirmação de retirada.
- **CA-012-2:** Dado um usuário que não é o Doador dono da oferta, quando tentar autorizar o início do transporte, então o sistema recusa com erro de permissão.
- **CA-012-3:** Dado o Transportador vinculado à operação e o código de retirada válido, quando informar o código, então o sistema valida no servidor, invalida imediatamente o código de retirada, registra data e hora da retirada e avança a operação para transporte em andamento.
- **CA-012-4:** Dado um código de retirada incorreto ou já utilizado, quando o Transportador tentar validar, então o sistema rejeita a confirmação, mantém a situação inalterada, registra a tentativa e bloqueia o código ao atingir o limite de tentativas.
- **CA-012-5:** Dado outro Transportador não vinculado à operação, quando tentar confirmar a retirada, então o sistema recusa com erro de permissão.

---

### RF-013 — Código de entrega de 24 horas e confirmação da entrega

**Descrição:** Ao confirmar a retirada, o sistema gera um segundo código de segurança de uso único e válido por 24 horas para o Receptor, que o fornece ao transportador no momento da entrega física para concluir a operação.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Código de confirmação de entrega | CHAR(6) | 000000 | Obrigatório para concluir a entrega; 6 dígitos numéricos, uso único, validade de 24 horas a partir da retirada, nunca exibido ao transportador antes da entrega e com limite de 5 tentativas. |

**Critérios de aceite**

- **CA-013-1:** Dado a confirmação da retirada, quando o Receptor consultar "Minhas reservas", então visualiza o status de transporte em andamento, o código de entrega, a data e hora limite de validade de 24 horas e o aviso para não compartilhar o código antes do recebimento físico.
- **CA-013-2:** Dado outro Receptor não participante da reserva, quando consultar detalhes ou listagens, então não tem acesso ao código de entrega.
- **CA-013-3:** Dado o Transportador vinculado e o código de entrega válido dentro do prazo de 24 horas, quando confirmar a entrega, então o sistema invalida o código, registra data e hora da entrega e atualiza a reserva e a oferta para concluídas.
- **CA-013-4:** Dado uma operação cuja retirada ainda não foi confirmada, quando o Transportador tentar concluir ou confirmar a entrega, então o sistema rejeita a operação.
- **CA-013-5:** Dado um código de entrega incorreto dentro do prazo, quando informado pelo Transportador, então o sistema não conclui a operação, registra a tentativa e bloqueia o código ao exceder o limite de tentativas.

---

### RF-014 — Expiração do código de entrega, suspensão do transportador e ocorrência

**Descrição:** Se o transportador confirmar a retirada e não confirmar a entrega dentro do prazo de 24 horas de validade do código de entrega, o sistema bloqueia a operação, suspende automaticamente a conta do transportador e registra uma ocorrência pendente de investigação preservando todo o histórico.

**Critérios de aceite**

- **CA-014-1:** Dado uma operação em transporte cujo prazo de 24 horas do código de entrega expirou sem confirmação de entrega, quando o sistema processar a expiração ou o motorista tentar usar o código expirado, então o código é invalidado, a operação passa para em investigação e o usuário recebe mensagem informando o bloqueio e que a conta está sob investigação.
- **CA-014-2:** Dado a expiração do prazo de entrega, quando a suspensão for aplicada, então a conta do Transportador passa para suspensa no servidor, impedindo que aceite novos transportes.
- **CA-014-3:** Dado a suspensão aplicada automaticamente, quando verificados os registros do sistema, então a reserva, a oferta (mantida como não concluída), os horários, as tentativas, os eventos e a ocorrência pendente de investigação permanecem preservados e não podem ser revertidos pelo próprio transportador.

---

### RF-015 — Perfil e navegação do transportador

**Descrição:** A área do Transportador apresenta as seções **Transportes disponíveis**, **Meus transportes** (com ações contextuais por etapa e contatos externos) e **Histórico de transportes** (operações concluídas em modo somente leitura), sem recursos de disponibilidade de veículos.

**Critérios de aceite**

- **CA-015-1:** Dado um Transportador autenticado, quando acessar a navegação do seu perfil, então visualiza "Transportes disponíveis", "Meus transportes" e "Histórico de transportes", sem qualquer opção ou filtro de disponibilidade.
- **CA-015-2:** Dado operações em "Meus transportes", quando exibidas na interface, então mostram ações exclusivas de acordo com a etapa (aguardar autorização do doador, informar código de retirada, informar código de entrega, bloqueio em investigação ou visualização somente leitura se concluído).
- **CA-015-3:** Dado operações concluídas em "Histórico de transportes", quando consultadas pelo Transportador, então exibem datas de aceite, autorização, retirada e entrega sem permitir alterações.

---

## Requisitos não funcionais

- **RNF-01 (Segurança de códigos):** Códigos de retirada e entrega devem ser gerados com entropia criptográfica, armazenados apenas por hash criptográfico com salt (nunca em texto puro), ter uso único e limite de tentativas contra força bruta.
- **RNF-02 (Privacidade e LGPD):** Antes do aceite do transporte, a listagem de transportes disponíveis exibe apenas cidade e UF de retirada e entrega e omite dados de contato de doador e receptor. Códigos de retirada são visíveis apenas ao Doador dono; códigos de entrega são visíveis apenas ao Receptor dono; o Transportador nunca visualiza os códigos gerados.
- **RNF-03 (Concorrência e integridade):** O aceite de transporte e as confirmações por código devem ser executados em operações transacionais com controle de concorrência, impedindo que dois transportadores assumam a mesma reserva ou que transições inválidas de estado ocorram.
- **RNF-04 (Auditoria):** Aceites, autorizações, geração de códigos, tentativas inválidas, bloqueios, expirações, suspensões e confirmações devem ser registrados em trilha de eventos com data e hora.

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Fluxo e perfil administrativo (`ADMIN`) para análise de ocorrências e reativação de transportadores suspensos | RF-014 | Em aberto |
| Procedimento de reemissão ou encerramento excepcional quando um código for bloqueado após 5 tentativas incorretas | RF-012, RF-013 | Em aberto |

---

## Fora do escopo desta versão

- Cadastro, edição ou filtro de disponibilidade de veículos e capacidade de carga (RF-005 retirado)
- Rastreamento em tempo real da carga ou do veículo
- Montagem, sugestão ou otimização de rotas logísticas por mapas
- Chat interno ou sistema de pagamento/frete
- Reversão de suspensão pelo próprio transportador ou painel administrativo completo de ocorrências

---

## Critérios de conclusão

Todos os critérios de aceite cobertos por testes automatizados no back-end (serviços, controladores e integração com banco relacional) e no front-end (fluxos de aceitação, autorização, códigos, expiração/suspensão e layout de cadastro sem rodapé).
