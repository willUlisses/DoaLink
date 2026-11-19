# DoaLink

> Plataforma para conectar doadores, transportadores voluntários e instituições receptoras, facilitando e agilizando a logística de doações solidárias.

---

## O que é o projeto

O **DoaLink** é uma aplicação Full-Stack voltada para o impacto social e apoio humanitário. O sistema resolve o gargalo de distribuição e logística em cadeias de doação, aproximando quem deseja doar de entidades que necessitam de auxílio, com suporte ativo de voluntários para transporte.

---

## Objetivo

Eliminar o desperdício, centralizar demandas de entidades assistenciais e simplificar a destinação de donativos por meio de uma plataforma segura, transparente e eficiente, garantindo agilidade na entrega dos itens arrecadados.

---

## Features principais

1. **Gestão de Perfis de Acesso (RBAC):** Fluxos especializados para Doadores (PF/PJ), Receptores (entidades com validação de CNPJ) e Transportadores voluntários.
2. **Publicação e Rastreio de Doações:** Criação e agrupamento de ofertas e necessidades com múltiplos itens, unidades padronizadas e controle de status do donativo.
3. **Autenticação Segura e Resiliente:** Sessão *stateless* baseada em JWT com rotação de refresh tokens em banco e armazenamento exclusivo em cookies `HttpOnly` com `SameSite=Strict`.
4. **Matchmaking e Compatibilidade Logística:** Visualização de necessidades por região e categoria de item, viabilizando conexões diretas entre demanda, oferta e rotas de entrega.

---

## Stack tecnológica

<p align="left">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,spring,postgres,maven,docker,react,ts,tailwind,vite" alt="Tech Stack" />
  </a>
</p>

- **Back-end:** Java 17+, Spring Boot (Web, Security, Data JPA, Bean Validation), Flyway, JWT.
- **Banco de Dados:** PostgreSQL.
- **Front-end:** React, TypeScript, Tailwind CSS, Vite.
- **Infraestrutura e Ferramental:** Docker, Maven, Git.

---

## Arquitetura usada (MVC em camadas)

O projeto adota o padrão **MVC (Model-View-Controller)** adaptado a uma arquitetura desacoplada em camadas com API REST e Single Page Application (SPA):

```
DoaLink/
├── doalink-api/               # Back-end (Model & Controller)
│   └── src/main/java/com/doalink_api/
│       ├── controller/        # [Controller] Endpoints REST, validação e contratos OpenAPI
│       ├── service/           # [Model/Service] Regras de negócio, transações e validações
│       ├── repository/        # [Model/Repository] Consultas Spring Data JPA
│       ├── entities/          # [Model/Entity] Mapeamento relacional e herança de perfis
│       ├── dtos/              # Contratos de Request e Response (Records imutáveis)
│       └── security/          # Filtros JWT, autenticação stateless e cookies HttpOnly
│
├── doalink-view/              # Front-end (View)
│   └── src/
│       ├── pages/             # [View] Páginas e composição de telas
│       ├── components/        # Componentes visuais reutilizáveis
│       ├── services/          # Clientes HTTP e integração com a API
│       └── hooks/             # Lógica reativa de estado e contexto do cliente
│
└── docs/                      # Documentação técnica e governança do projeto
```

- **Controller:** Ponto de entrada HTTP que valida os dados de entrada (`@Valid`), chama o respectivo serviço de domínio e retorna DTOs com o status HTTP correspondente.
- **Model (Service, Repository, Entity):**
  - **Service:** Concentra todas as regras de negócio, limites transacionais e integridade dos dados.
  - **Entity:** Mapeia as tabelas do PostgreSQL utilizando estratégias relacionais como herança `JOINED`.
  - **Repository:** Provê a camada de persistência com Spring Data JPA.
- **View:** Interface reativa em React + TypeScript + Tailwind CSS, desacoplada e consumidora dos recursos REST com segurança via cookies `HttpOnly`.

---

## Estrutura das documentações

A documentação técnica é mantida no diretório [`docs/`](file:///home/will/Documentos/ProjetosDev/Doalink-App/DoaLink/docs) e dividida por objetivos específicos:

| Diretório / Arquivo | Objetivo |
|---|---|
| [`docs/arquitetura.md`](file:///home/will/Documentos/ProjetosDev/Doalink-App/DoaLink/docs/arquitetura.md) | Visão sistêmica, convenções de código, diretrizes de segurança e contratos entre front-end e back-end. |
| `docs/prd/` | **Product Requirements Documents:** Requisitos funcionais, perfis de usuários e critérios de aceite das regras do produto. |
| `docs/specs/` | **Especificações Técnicas:** Detalhes de implementação de cada funcionalidade (endpoints, DTOs, entidades e métodos de serviço). |
| `docs/adr/` | **Architecture Decision Records:** Justificativa técnica e histórico de decisões arquiteturais estruturantes do projeto. |
