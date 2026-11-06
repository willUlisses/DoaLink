---
name: spec
description: Como escrever especificações de funcionalidade neste projeto (Spring Boot no back-end, React + TypeScript + Tailwind no front-end). Use ao criar ou alterar uma spec em docs/specs/, ou antes de implementar uma funcionalidade que ainda não tem spec.
---

# Spec

Uma spec diz como uma funcionalidade será construída, com detalhes suficientes para implementá-la sem adivinhar. O que o produto deve fazer fica no PRD (`docs/prd/`), e o motivo das decisões estruturais, nos ADRs (`docs/adr/`).

## Quando escrever

- Escreva a spec antes do código, para toda funcionalidade nova.
- Altere a spec no mesmo trabalho em que o comportamento mudar, para que ela não contradiga o código.
- Correções pequenas, que apenas fazem o código cumprir a spec, não exigem alteração.

## Arquivo

Caminho: `docs/specs/NNNN-titulo-em-kebab-case.md`, com o próximo número livre de 4 dígitos. A numeração indica a ordem de implementação: uma spec só depende de specs com número menor.

## Modelo

Inclua apenas as seções que se aplicam à funcionalidade.

```markdown
# Spec NNNN: Título

- **Status:** Proposto
- **Requisitos:** RF-xxx, CA-xxx-x do [PRD NNNN](../prd/arquivo.md)
- **Decisões:** [ADR NNNN](../adr/arquivo.md)
- **Depende de:** [Spec NNNN](arquivo.md)

## Entidade `Nome`
Tabela com campo, tipo Java, coluna e regras (nullable, unique, enum),
além de herança, relacionamentos, constraints e índices.

## DTOs
Tabela por record: campo, tipo, validações (Bean Validation) e
regras derivadas (ex.: `@AssertTrue`). Separe Request de Response.

## `NomeService`
Tabela com método, regra de negócio e exceções de domínio lançadas.
Indique o que é transacional.

## `NomeRepository`
Métodos de consulta necessários (derived queries ou `@Query`).

## Endpoints
Tabela com método HTTP, rota, DTO de entrada, DTO de saída,
status de sucesso, erros e acesso (público, autenticado, role).

## Segurança
Roles exigidas, cookies emitidos ou consumidos, tratamento de 401/403.

## Front-end
Rotas (React Router), páginas, componentes, hooks, chamadas de API,
campos e validações do formulário, estados de carregamento e erro.

## Banco
Migrations (Flyway/Liquibase) e constraints (CHECK, UNIQUE, índices).

## Limites aceitos
Condições de corrida ou simplificações toleradas nesta versão.

## Testes
- [ ] Comportamento verificável.
```

Omita as linhas de cabeçalho que não se aplicarem, como **Requisitos** em uma funcionalidade sem RF no PRD.

## Status

- `Proposto`: a spec ainda não foi implementada por completo.
- `Implementado`: o código e os testes cumprem a spec.

Marque um item de teste com `[x]` somente quando existir um teste que o cubra e a suíte estiver passando.

## Regras

- Siga `docs/arquitetura.md`: cada regra de negócio é especificada no **service**, nunca no controller, na entidade ou no componente React.
- Controllers recebem DTOs validados e devolvem DTOs de resposta. Entidades JPA nunca aparecem em contratos de API.
- Cite os requisitos atendidos (RF e CA). Se a funcionalidade mudar o que o produto faz e o requisito não existir, atualize o PRD primeiro.
- Se a spec exigir uma decisão difícil de reverter, escreva antes um ADR. Não repita o raciocínio dele aqui.
- Descreva contratos (campos, assinaturas, rotas, status HTTP, erros). Inclua código apenas quando ele esclarecer uma regra.
- Toda validação existe em duas camadas: formato no DTO (Bean Validation) e regra de negócio no service. No front-end, a validação serve só à experiência do usuário e nunca substitui a do back-end.
- Dados sensíveis (senha, hash, CPF/CNPJ completo) não aparecem em respostas, logs nem mensagens de erro.
- Cubra nos testes os caminhos de erro e, nas rotas protegidas, os casos de visitante (401), perfil errado (403) e perfil correto (sucesso).
- Registre os limites aceitos, como condições de corrida toleradas (a garantia final de unicidade é a constraint do banco).
- Ao criar a spec, adicione o link dela no campo **Especificações** do PRD.

## Exemplo resumido de tabela de endpoints

| Método | Rota | Entrada | Saída | Sucesso | Erros | Acesso |
|---|---|---|---|---|---|---|
| POST | `/auth/doadores/cadastro` | `CadastroComumRequest` | `UsuarioResponse` + cookies | 201 | 400 validação, 409 e-mail/documento duplicado | Público |
| POST | `/auth/receptores/cadastro` | `CadastroReceptorRequest` | `UsuarioResponse` + cookies | 201 | 400, 409 | Público |
