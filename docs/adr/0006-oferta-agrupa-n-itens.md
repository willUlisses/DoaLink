# ADR 0006: Oferta agrupa N itens em vez de um item por oferta

- **Status:** Aceito
- **Data:** 2026-11-07

## Contexto

O modelo lógico de `implementacao-doalink.md` (seção 5) descreve `OfertaDoacao` com um único "item/categoria + quantidade + unidade". Seguindo à risca esse modelo, cada oferta teria exatamente um item e um doador precisaria criar uma oferta separada para cada tipo de item que quisesse doar de uma vez.

Alternativas consideradas:

- **Um item por oferta (modelo original):** fiel ao documento, mas obriga o doador a criar múltiplas ofertas para uma única entrega, dificulta a combinação com receptores que precisam de variedade e fragmenta o histórico de uma mesma doação.
- **N itens por oferta com modelo relacional (escolhida):** `OfertaDoacao` é o agregado raiz; `ItemDoacao` é uma entidade filha com `@ManyToOne oferta`. O endereço e o doador ficam na oferta, eliminando duplicação.
- **N itens desnormalizados (JSON ou colunas separadas):** evitaria a tabela de itens, mas tornaria consultas por categoria impossíveis sem parsing e violaria as regras de normalização do projeto.

## Decisão

`OfertaDoacao` contém uma lista de `ItemDoacao` (`@OneToMany cascade=ALL orphanRemoval=true`). O doador, o endereço de retirada e a situação da oferta ficam na `OfertaDoacao`. Cada `ItemDoacao` carrega descrição, categoria (`TipoItemDoacao`) e quantidade. O limite de itens por oferta é 20 (validado no DTO).

## Consequências

- Uma única transação cria a oferta e todos os seus itens; a exclusão em cascata (`orphanRemoval`) remove os itens automaticamente quando a oferta é deletada.
- Consultas por categoria de item exigem JOIN com `itens_doacao`, o que é trivial com JPA/JPQL.
- A compatibilidade com receptores (RF-007) precisa considerar todos os itens da oferta, não apenas um. Isso aumenta a complexidade do algoritmo de matching, mas reflete melhor a realidade de uma doação.
- O documento original terá que ser atualizado para refletir essa decisão antes de implementar a tela de criação de oferta no front-end.
- Há um custo aceito: itens duplicados dentro de uma mesma oferta (mesma categoria e descrição) são permitidos nesta versão, sem agrupamento automático.
