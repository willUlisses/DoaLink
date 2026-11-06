---
name: adr
description: Como escrever decisões de arquitetura neste projeto. Use ao criar, aceitar ou substituir um ADR em docs/adr/, ou quando uma mudança contrariar ou exigir uma decisão de arquitetura (back-end Spring Boot ou front-end React).
---

# ADR

Um ADR registra uma decisão e o motivo dela, para que ninguém precise adivinhar depois. As regras do dia a dia ficam em `docs/arquitetura.md`, e o ADR explica por que elas existem.

## Quando escrever

Escreva um ADR quando a decisão for difícil de reverter, tiver alternativas reais ou afetar várias partes do projeto. Exemplos neste projeto:

- estratégia de herança JPA (`JOINED`, `SINGLE_TABLE`);
- modelo de autenticação (JWT, cookies HttpOnly, refresh token no banco);
- organização em camadas e onde ficam as regras de negócio;
- escolha de banco de dados, de ferramenta de migration ou de biblioteca estrutural (ex.: biblioteca de JWT, gerenciamento de estado no front-end);
- contrato geral da API (formato de erros, versionamento, prefixos de rota).

Não escreva para escolhas locais e fáceis de trocar, nem para detalhes de uma funcionalidade, que ficam na spec.

Se uma mudança contrariar um ADR aceito, pare e proponha um novo ADR antes de alterar o código.

## Arquivo

- Caminho: `docs/adr/NNNN-titulo-em-kebab-case.md`.
- Use o próximo número livre, com 4 dígitos. Não reaproveite números.
- Um ADR trata de uma única decisão.

## Modelo

```markdown
# ADR NNNN: Título da decisão

- **Status:** Proposto
- **Data:** AAAA-MM-DD

## Contexto

O problema, as restrições e as alternativas que importam.

## Decisão

O que foi decidido, de forma afirmativa e verificável.

## Consequências

- Os ganhos e também os custos aceitos.
```

## Status

- `Proposto`: aguardando a decisão da pessoa responsável. Um agente sempre cria o ADR com este status.
- `Aceito`: a decisão está valendo.
- `Substituído por [ADR NNNN](arquivo.md)`: a decisão deixou de valer.

## Regras

- Não reescreva a decisão de um ADR aceito. Crie outro ADR e altere apenas o status do antigo.
- Registre em Consequências ao menos um custo. Se não houver custo nem alternativa, provavelmente não é preciso um ADR.
- Liste no Contexto as alternativas consideradas e por que foram descartadas, em uma linha cada.
- Escreva no presente, sem narrar a discussão que levou à decisão.
- Use links relativos para os ADRs, specs e PRDs relacionados.
- Atualize `docs/arquitetura.md` e as specs afetadas quando a decisão mudar uma regra.
- Decisões de segurança (autenticação, armazenamento de tokens, CORS, CSRF) sempre ganham ADR, com os riscos aceitos descritos em Consequências.

## Exemplo resumido

```markdown
# ADR 0002: Herança JPA com estratégia JOINED

- **Status:** Aceito
- **Data:** 2026-11-06

## Contexto

Doador, Transportador e Receptor compartilham dados comuns, e só o Receptor
tem campos próprios (endereço). Alternativas: `SINGLE_TABLE` (colunas nulas
para quem não usa o campo), `TABLE_PER_CLASS` (consultas polimórficas com
UNION).

## Decisão

`Usuario` é uma classe abstrata com `@Inheritance(strategy = JOINED)`. O tipo
do perfil é guardado no enum `TipoUsuario`, fixado no construtor de cada
subclasse, e não há `@DiscriminatorColumn`.

## Consequências

- Colunas específicas podem ser `NOT NULL` na tabela da subclasse.
- Consultas polimórficas fazem JOIN nas tabelas das subclasses.
- Doador e Transportador têm tabelas só com o `id` até ganharem campos próprios.
```
