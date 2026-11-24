# ADR 0010: Vínculo entre oferta, receptor e transporte via entidade de reserva

- **Status:** Substituído por [ADR 0013](0013-fluxo-de-transporte-por-codigos-com-hash-e-suspensao-por-expiracao.md)
- **Data:** 2026-11-17

## Contexto

Com a decisão de operar o DoaLink por listagens diretas (sem depender do matchmaking automático do RF-007), é necessário registrar quem reservou uma `OfertaDoacao` (`Receptor`) e qual `Transportador` (via `DisponibilidadeTransporte`) assumiu a entrega, além de permitir que um mesmo transportador assuma múltiplas ofertas em uma mesma disponibilidade e acesse o endereço completo somente das ofertas vinculadas a ele.

Alternativas consideradas:

- **Colunas diretas em `ofertas_doacao` (`receptor_id`, `disponibilidade_id`):** simples no início, mas acopla o estado da reserva/entrega à tabela de ofertas, dificulta manter histórico caso uma reserva ou transporte seja cancelado e mistura responsabilidades de entidades distintas.
- **Tabela de junção N:N pura (`disponibilidade_ofertas`):** liga disponibilidade e oferta, mas não registra o `Receptor` que reservou, se a entrega precisa de transporte, nem o ciclo de vida próprio da entrega.
- **Entidade dedicada `ReservaEntrega` (`reservas_entrega`) (escolhida):** centraliza o vínculo entre `OfertaDoacao` (1 reserva ativa por oferta), `Receptor` (quem reservou) e `DisponibilidadeTransporte` (opcional até um transportador assumir), mantendo histórico e servindo de base direta para a futura geração de rotas via Google Maps.

## Decisão

Criar a entidade `ReservaEntrega` (tabela `reservas_entrega`) com:

- `oferta` (`@ManyToOne`, `NOT NULL`) — referência à `OfertaDoacao`.
- `receptor` (`@ManyToOne`, `NOT NULL`) — referência ao `Receptor` que realizou a reserva.
- `disponibilidade` (`@ManyToOne`, `nullable = true`) — referência à `DisponibilidadeTransporte` assumida pelo transportador (nula enquanto aguarda motorista ou quando `precisaTransporte = false`).
- `precisaTransporte` (`boolean`, `NOT NULL`) — indica se a reserva fica aberta para transportadores assumirem.
- `situacao` (`SituacaoReservaEntrega`: `AGUARDANDO_TRANSPORTE`, `AGUARDANDO_RETIRADA`, `TRANSPORTE_VINCULADO`, `CONCLUIDA`, `CANCELADA`).
- Índice único parcial em `(oferta_id)` para `situacao != 'CANCELADA'`, garantindo no banco que uma oferta tenha no máximo uma reserva ativa por vez.

## Consequências

- Uma única `DisponibilidadeTransporte` pode ser referenciada por múltiplas `ReservaEntrega`, abatendo `capacidadeRestante` a cada vínculo e agrupando os endereços de origem (da oferta) e destino (do receptor) para a futura rota no Google Maps.
- A verificação de acesso ao endereço completo em `GET /ofertas/{id}` passa a checar se o usuário é o `Doador` dono, o `Receptor` da reserva ativa ou o `Transportador` da `disponibilidade` vinculada.
- **Custo aceito:** exige controle transacional (lock otimista via `@Version` em `OfertaDoacao`, `DisponibilidadeTransporte` e `ReservaEntrega`, além do índice único parcial para reserva ativa), e uma consulta adicional em `ReservaEntregaRepository` para autorizar a exibição do endereço completo.

