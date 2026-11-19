# PRD 0006: Reserva de Oferta e Vínculo de Transporte

- **Status:** Proposto
- **Data:** 2026-11-17
- **Especificações:** [Spec 0006 — Reserva de Oferta e Vínculo de Transporte](../specs/0006-reserva-e-vinculo-de-transporte.md)
- **ADRs relacionados:** [ADR 0010 — Vínculo entre oferta, receptor e transporte via entidade de reserva](../adr/0012-vinculo-entre-oferta-receptor-e-transporte.md)

---

## Descrição geral

O DoaLink opera por meio de listagens diretas, sem exigir matchmaking automático (RF-007). Este PRD define o fluxo de reserva de uma oferta de doação por um Receptor e o vínculo dessa entrega a um Transportador (e sua disponibilidade), permitindo que um único transportador assuma múltiplas ofertas dentro da capacidade disponível e tenha acesso ao endereço exato de retirada e entrega apenas após o vínculo.

O sistema **não faz** nesta versão:
- Sugestão automática de combinações (RF-007).
- Montagem automática de rota via API do Google Maps (será adicionada em etapa posterior sobre este vínculo).
- Rastreamento do veículo em tempo real ou chat interno.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Receptor | ONG autenticada | Reservar uma oferta `DISPONIVEL`, informar se precisa de transporte e cancelar reserva antes da coleta |
| Transportador | Transportador autenticado | Listar reservas que aguardam transporte, vincular uma ou mais ofertas à sua disponibilidade e concluir a entrega |
| Doador | Doador dono da oferta | Acompanhar a situação da oferta reservada e quem assumiu a entrega |

---

## Requisitos funcionais

### RF-010 — Reserva de oferta e vínculo de transporte

**Descrição:** O sistema permite que um Receptor reserve uma oferta de doação `DISPONIVEL` e que um Transportador com disponibilidade compatível assuma uma ou mais ofertas reservadas que aguardam transporte, abatendo a capacidade restante da sua disponibilidade e liberando os endereços completos apenas às partes vinculadas.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Oferta de doação | Referência (`Long`) | — | Obrigatória. Deve estar com situação `DISPONIVEL` no momento da reserva pelo Receptor. |
| \*Precisa de transporte | Boolean | — | Obrigatório na reserva. Define se a entrega ficará visível para Transportadores assumirem. |
| Disponibilidade de transporte | Referência (`Long`) | — | Obrigatória quando o Transportador assumir a entrega. Deve pertencer ao Transportador autenticado e ter `capacidadeRestante` suficiente. |
| Situação da reserva | ENUM | — | Gerenciada pelo sistema: `AGUARDANDO_TRANSPORTE`, `AGUARDANDO_RETIRADA`, `TRANSPORTE_VINCULADO`, `CONCLUIDA`, `CANCELADA`. |

**Critérios de aceite**

- **CA-010-1:** Dado um Receptor autenticado e uma oferta `DISPONIVEL`, quando o Receptor reservar a oferta, então a oferta passa para `RESERVADA` e uma reserva é criada vinculando oferta e receptor.
- **CA-010-2:** Dado uma oferta que já não está `DISPONIVEL`, quando outro Receptor tentar reservá-la, então o sistema rejeita com erro de conflito.
- **CA-010-3:** Dado um Transportador autenticado com disponibilidade ativa e `capacidadeRestante` suficiente, quando assumir uma reserva `AGUARDANDO_TRANSPORTE`, então o sistema vincula a disponibilidade à reserva, abate `capacidadeRestante` e atualiza a situação da reserva para `TRANSPORTE_VINCULADO`.
- **CA-010-4:** Dado um Transportador tentando assumir uma reserva cujo peso/quantidade exceda `capacidadeRestante`, com unidade incompatível ou disponibilidade expirada, quando solicitar o vínculo, então o sistema rejeita com erro de conflito (`409`).
- **CA-010-5:** Dado um Transportador que assumiu múltiplas reservas na mesma disponibilidade, quando consultar suas entregas, então todas as ofertas vinculadas aparecem disponíveis para compor sua rota.
- **CA-010-6:** Dado uma oferta `RESERVADA` ou `CONCLUIDA` vinculada a um Transportador, quando esse Transportador ou o Receptor da reserva consultar os detalhes da oferta (`GET /ofertas/{id}`), então o endereço completo de retirada é exibido (oculto para terceiros não vinculados).
- **CA-010-7:** Dado uma entrega finalizada pelo Transportador vinculado (ou pelo Receptor quando a reserva estiver `AGUARDANDO_RETIRADA`), quando concluir a entrega, então a reserva e a oferta passam para `CONCLUIDA`.
- **CA-010-8:** Dado um Doador com ofertas reservadas, quando consultar suas reservas (`GET /reservas/minhas/doador`), então visualiza situação, receptor e transportador vinculado (quando houver).
- **CA-010-9:** Dado um Receptor dono de uma reserva `AGUARDANDO_TRANSPORTE` ou `AGUARDANDO_RETIRADA`, quando cancelar, então a reserva fica `CANCELADA` e a oferta volta a `DISPONIVEL`; em outras situações o sistema rejeita com erro de conflito (`409`).

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** O endereço exato e os dados de contato (e-mail e telefone) de doador e receptor só são exibidos para o Doador dono, o Receptor que reservou e o Transportador vinculado àquela reserva. Na listagem de reservas aguardando transporte, `doador` e `receptor` são omitidos e os endereços são resumidos (`cidade`/`estado`).
- **RNF-02 (Concorrência):** Uma oferta não pode ter mais de uma reserva ativa simultaneamente; reservas concorrentes sobre a mesma oferta devem resultar em conflito (`409`).
- **RNF-03 (Integridade de capacidade):** A soma das quantidades vinculadas a uma `DisponibilidadeTransporte` nunca pode ultrapassar sua `capacidade` (`capacidadeRestante >= 0`).

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Conversão automática entre unidades compatíveis (ex.: `G`, `KG`, `T`) ao abater `capacidadeRestante` | RF-010 | Em aberto |
| Regras de desvinculação/cancelamento pelo Transportador após assumir entrega | RF-010 | Em aberto |

---

## Fora do escopo desta versão

- Matchmaking automático entre ofertas, necessidades e transportes (RF-007)
- Otimização/ordenação automática de rota via API do Google Maps
- Chat interno ou rastreamento GPS em tempo real

---

## Critérios de conclusão

Todos os critérios de aceite cobertos por testes automatizados passando após a criação da Spec 0006 correspondente.

