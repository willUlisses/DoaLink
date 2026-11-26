# Guia Geral de Agentes e Skills (DoaLink)

Este arquivo define as instruções gerais de operação para todas as personas localizadas em `.agents/personas/`. Todo agente atuando neste repositório deve ler este guia antes de iniciar qualquer tarefa.

---

## 1. Leitura Obrigatória da Arquitetura

Antes de propor alterações, escrever especificações ou implementar código, **leia obrigatoriamente** o documento de arquitetura do sistema:

- [`docs/arquitetura.md`](../docs/arquitetura.md)

### Visão Geral do Sistema (MVC)

O DoaLink adota uma arquitetura **MVC (Model-View-Controller)** distribuída em duas aplicações independentes:

- **Model e Controller (`doalink-api/`):** API REST em **Spring Boot (Java 21)**.
  - **Controller (`controller/`):** Recebe requisições HTTP, aciona validações de DTOs e delega para a camada de serviço, retornando respostas padronizadas.
  - **Model (`entities/`, `repository/`, `service/`, `dtos/`, `mapper/`):** Representa o domínio, persistência (JPA/Hibernate + PostgreSQL/Flyway) e concentra todas as regras de negócio na camada de `service/`.
- **View (`doalink-view/`):** SPA em **React + TypeScript + Tailwind CSS v4**.
  - Responsável pela interface do usuário, roteamento, validação de UX (`schemas/`), comunicação HTTP via `fetch` com cookies `HttpOnly` (`services/`) e estado global mínimo (`contexts/`).

---

## 2. Uso Obrigatório de Skills por Persona

As instruções específicas de formato, padrões e boas práticas residem em `.agents/skills/`. Cada persona deve consultar e seguir rigorosamente suas respectivas skills:

| Persona (`.agents/personas/`) | Skills Obrigatórias (`.agents/skills/`) | Finalidade |
|---|---|---|
| **Documentador** (`documentador.md`) | `ADR/SKILL.md`<br>`PRD/SKILL.md`<br>`SPEC/SKILL.md`<br>`writing-guidelines/SKILL.md` | Padronização de registros de decisão arquitetural (`docs/adr/`), requisitos de produto (`docs/prd/`), especificações técnicas (`docs/specs/`) e clareza textual. |
| **Especialista Backend** (`backend-specialist.md`) | `SPRING/SKILL.md` | Boas práticas de Spring Boot, regras de camadas, segurança JWT em cookies HttpOnly, transações, migrations Flyway e testes em `doalink-api/`. |
| **Especialista Frontend** (`frontend-specialist.md`) | `vercel-react-best-practices/SKILL.md`<br>`vercel-composition-patterns/SKILL.md`<br>`web-design-guidelines/SKILL.md`<br>`webapp-testing/SKILL.md` | Padrões de React, composição de componentes, diretrizes de UI/acessibilidade e testes em `doalink-view/`. |
| **Versionador** (`versionador.md`) | *(Diretrizes da própria persona)* | Inspeção atômica (`git status`/`git diff`), Conventional Commits e higiene do repositório. |

> **Regra para o Documentador:** Sempre que for redigir ou atualizar um ADR, PRD ou Spec, leia primeiro a skill correspondente (`ADR/SKILL.md`, `PRD/SKILL.md` ou `SPEC/SKILL.md`) e verifique a consistência com `docs/arquitetura.md`.

---

## 3. Fluxo de Trabalho entre Agentes (Spec-Driven)

1. **Requisitos e Decisões:** O **Documentador** registra o comportamento esperado em `docs/prd/` e decisões estruturais em `docs/adr/`, seguindo as skills `PRD` e `ADR`.
2. **Especificação Técnica:** Antes de qualquer implementação, o **Documentador** escreve ou atualiza a spec em `docs/specs/` seguindo a skill `SPEC`.
3. **Implementação:** Os especialistas (**Backend** e **Frontend**) implementam exclusivamente o que foi especificado, respeitando `docs/arquitetura.md` e suas respectivas skills.
4. **Atualização Documental e Versionamento:** Alterações estruturais refletem em `docs/arquitetura.md` (via **Documentador**) e são consolidadas em commits semânticos pelo **Versionador**.
