# ADR 0009: Modelo de dados para disponibilidade de transporte (RF-005)

- **Status:** Substituído por [ADR 0013](0013-fluxo-de-transporte-por-codigos-com-hash-e-suspensao-por-expiracao.md)
- **Data:** 2026-11-16

## Contexto

O requisito [RF-005](../prd/0003-disponibilidade-de-transporte.md) permite ao transportador cadastrar disponibilidade de transporte com tipo de veículo, capacidade/unidade, origem e data (com horário opcional), deixando definições estruturais em aberto que impactam persistência, contrato da API e o algoritmo de sugestões/rotas ([RF-007](../prd/0004-necessidade-de-doacao-e-matchmaking.md)):

1. **Categorias de veículo:**
   - **Texto livre:** inviabiliza filtros e padronização nas sugestões.
   - **Tabela de catálogo dinâmico no banco:** exige CRUD administrativo fora do escopo atual.
   - **Enum fixo `TipoVeiculo` persistido como `STRING` (escolhida):** segue o padrão do projeto (`TipoItemDoacao`, `TipoUnidade`, `TipoUsuario`).

2. **Significado, unidade e consumo da capacidade:**
   - **Valor numérico sem unidade:** gera ambiguidade entre peso, volume e quantidade de volumes.
   - **Uso exclusivo 1:1 (uma disponibilidade = uma única oferta):** impede o transportador de atender múltiplas ofertas em uma mesma rota.
   - **Valor numérico positivo (`Double`) + `capacidadeRestante` associados ao enum `TipoUnidade` (escolhida):** reutiliza o enum `TipoUnidade` já adotado em `ItemDoacao` (`OfertaDoacao` e `NecessidadeDoacao`) e permite que uma disponibilidade atenda múltiplas ofertas enquanto houver saldo de capacidade.

3. **Origem e destino:**
   - **Destino fixo obrigatório na disponibilidade:** obriga o transportador a declarar um destino único quando, na operação, o trajeto/destino é composto pelas ofertas e necessidades vinculadas na rota.
   - **Sem destino fixo na `DisponibilidadeTransporte` (escolhida):** a entidade registra apenas a `origem` (`@Embeddable` `Endereco`); os destinos e paradas passam a ser derivados das ofertas (`OfertaDoacao`) e necessidades (`NecessidadeDoacao`) na montagem da rota (etapa futura com API do Google Maps / RF-007).

4. **Precisão temporal:**
   - **Apenas `LocalDate`:** não atende à necessidade de informar horário quando aplicável.
   - **`OffsetDateTime` obrigatório:** força o usuário a informar hora exata mesmo quando tem disponibilidade no dia inteiro.
   - **`LocalDate` obrigatório + `LocalTime` opcional (escolhida):** representa a data da disponibilidade com precisão diária por padrão e horário opcional quando o transportador tiver janela específica.

## Decisão

A entidade `DisponibilidadeTransporte` adota os seguintes tipos e granularidades:

- **Tipo de veículo:** enum `TipoVeiculo` (`@Enumerated(EnumType.STRING)`), com os valores `MOTO`, `CARRO_PASSEIO`, `UTILITARIO_LEVE`, `CAMINHONETE`, `CAMINHAO_PEQUENO`, `CAMINHAO_MEDIO`, `CAMINHAO_GRANDE` e `OUTRO`.
- **Capacidade:** `capacidade` (`Double`, `> 0`) e `capacidadeRestante` (`Double`, `>= 0` e `<= capacidade`, inicializada igual a `capacidade`), combinadas com `tipoUnidade` (`TipoUnidade`, `@Enumerated(EnumType.STRING)`).
- **Origem sem destino fixo:** `origem` utiliza o `@Embeddable` `Endereco`; não há campos de destino na `DisponibilidadeTransporte`, sendo o destino derivado das ofertas/necessidades na formação de rota.
- **Precisão temporal:** `dataDisponivel` (`LocalDate`, obrigatória e não passada) e `horarioDisponivel` (`LocalTime`, opcional).

## Consequências

- O matchmaking e a futura geração de rotas (via API do Google Maps) podem agrupar múltiplas ofertas em uma mesma `DisponibilidadeTransporte`, abatendo `capacidadeRestante` sem exigir destino pré-fixado no cadastro.
- **Custo aceito:** novas categorias de veículo exigem alteração de código e validação de enum; além disso, como `capacidade` representa uma única grandeza por registro (ex.: `KG` ou `CAIXAS`), um veículo não declara simultaneamente limite de peso e de volume nesta versão.
- **Custo aceito:** `LocalDate` + `LocalTime` assume o fuso horário padrão da operação (`America/Sao_Paulo`) sem armazenar offset por registro.
