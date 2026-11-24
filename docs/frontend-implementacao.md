# Relatório da Fase 0 — Implementação do Frontend DoaLink

**Data:** 2026-11-20  
**Base de verdade:** Implementação atual da API Spring Boot em `doalink-api/` e projeto Vite/React/Tailwind em `doalink-view/`.

---

## 1. Tabela de Endpoints Encontrados na API

Todos os endpoints protegidos utilizam sessão stateless via cookies HttpOnly (`access_token` e `refresh_token`, `SameSite=Strict`), além de suporte secundário a `Authorization: Bearer` no filtro JWT (não utilizado pelo frontend).

### 1.1 Autenticação e Cadastro (`/auth`) — `AuthController` (`IAuthController`)

| Método | Rota | Acesso / Perfil | Request | Response (Sucesso) | Erros mapeados |
|---|---|---|---|---|---|
| `POST` | `/auth/doadores/cadastro` | Público | `CadastroComumRequest` (`cpf` ou `cnpj`, `email`, `nome`, `senha` [8..60]) | `201 Created` — `UsuarioResponse` (sem cookies; dispara código por e-mail) | `400` validação, `409` e-mail ou documento já cadastrado |
| `POST` | `/auth/transportadores/cadastro` | Público | `CadastroComumRequest` (`cpf` ou `cnpj`, `email`, `nome`, `senha` [8..60]) | `201 Created` — `UsuarioResponse` (sem cookies; dispara código por e-mail) | `400` validação, `409` e-mail ou documento já cadastrado |
| `POST` | `/auth/receptores/cadastro` | Público | `CadastroReceptorRequest` (`cnpj`, `nome`, `email`, `senha` [8..72], `endereco`: `EnderecoRequest`) | `201 Created` — `UsuarioResponse` (sem cookies; dispara código por e-mail) | `400` validação, `409` e-mail ou documento já cadastrado |
| `POST` | `/auth/verificar-email` | Público | `VerificarEmailRequest` (`email`, `codigo` `^\d{6}$`) | `200 OK` — `UsuarioResponse` + cookies `access_token` e `refresh_token` | `400` código inválido/expirado/revogado, `404` usuário não encontrado |
| `POST` | `/auth/reenviar-codigo` | Público | `ReenviarCodigoRequest` (`email`) | `200 OK` — `MensagemResponse` (resposta uniforme) | `400` e-mail inválido |
| `POST` | `/auth/login` | Público | `LoginRequest` (`email`, `senha`) | `200 OK` — `UsuarioResponse` + cookies `access_token` e `refresh_token` | `400` validação, `401` credenciais inválidas, `403` e-mail não verificado |
| `POST` | `/auth/refresh` | Público (cookie `refresh_token`) | Cookie `refresh_token` | `200 OK` — `UsuarioResponse` + novos cookies rotacionados | `401` token ausente/inválido/expirado/reutilizado, `403` e-mail não verificado |
| `POST` | `/auth/logout` | Público (cookie `refresh_token`) | Cookie `refresh_token` (opcional) | `204 No Content` + cookies limpos (`Max-Age=0`) | — |

### 1.2 Recuperação de Senha (`/auth`) — `RecuperacaoSenhaController` (`IRecuperacaoSenhaController`)

| Método | Rota | Acesso / Perfil | Request | Response (Sucesso) | Erros mapeados |
|---|---|---|---|---|---|
| `POST` | `/auth/recuperar-senha` | Público | `SolicitarRecuperacaoSenhaRequest` (`email`) | `200 OK` — `MensagemResponse` (resposta uniforme) | `400` e-mail inválido |
| `GET` | `/auth/recuperar-senha/validar?token=...` | Público | Query `token` (`@NotBlank`) | `200 OK` — `{ "valido": boolean }` | `400` token em branco |
| `POST` | `/auth/redefinir-senha` | Público | `RedefinirSenhaRequest` (`token`, `novaSenha` [8..72]) | `200 OK` — `MensagemResponse` | `400` validação, `404` token inválido/expirado/utilizado |

### 1.3 Ofertas de Doação (`/ofertas`) — `OfertaDoacaoController` (`IOfertaDoacaoController`)

| Método | Rota | Acesso / Perfil | Request | Response (Sucesso) | Erros mapeados |
|---|---|---|---|---|---|
| `POST` | `/ofertas` | `ROLE_DOADOR` | `OfertaDoacaoRequest` (`itens`: 1..50 `ItemDoacaoRequest`, `endereco`?: `EnderecoRequest`) | `201 Created` + `Location` — `OfertaDoacaoResponse` | `400` validação, `401`, `403` não é doador |
| `GET` | `/ofertas/minhas` | `ROLE_DOADOR` | — | `200 OK` — `List<OfertaDoacaoResponse>` | `401`, `403` |
| `GET` | `/ofertas` | Autenticado (qualquer perfil) | Query: `page`, `size`, `sort`, `categoria` (`TipoItemDoacao`), `cidade`, `estado` | `200 OK` — `Page<OfertaDoacaoResponse>` (apenas `DISPONIVEL`, `localRetirada` resumido: `cidade`/`estado`) | `400` filtro inválido, `401` |
| `GET` | `/ofertas/{id}` | Autenticado | Path `id` | `200 OK` — `OfertaDoacaoResponse` (`localRetirada` completo para doador dono, receptor da reserva ativa ou transportador vinculado; resumido para demais) | `401`, `403` transportador não vinculado em oferta não `RESERVADA`, `404` |
| `PATCH` | `/ofertas/{id}/cancelar` | `ROLE_DOADOR` (dono) | Path `id` | `204 No Content` | `401`, `403` não dono ou situação ≠ `DISPONIVEL`, `404` |

### 1.4 Necessidades de Doação (`/necessidades`) — `NecessidadeDoacaoController` (`INecessidadeDoacaoController`)

| Método | Rota | Acesso / Perfil | Request | Response (Sucesso) | Erros mapeados |
|---|---|---|---|---|---|
| `POST` | `/necessidades` | `ROLE_RECEPTOR` | `NecessidadeDoacaoRequest` (`itens`: 1..50 `ItemNecessidadeRequest`, `localEntrega`: `EnderecoRequest`) | `201 Created` + `Location` — `NecessidadeDoacaoResponse` | `400` validação, `401`, `403` |
| `GET` | `/necessidades/minhas` | `ROLE_RECEPTOR` | — | `200 OK` — `List<NecessidadeDoacaoResponse>` | `401`, `403` |
| `GET` | `/necessidades` | Autenticado | Query: `situacao` (`SituacaoNecessidade`, padrão `ABERTA`), `page`, `size`, `sort` | `200 OK` — `Page<NecessidadeDoacaoResponse>` | `400`, `401` |
| `GET` | `/necessidades/{id}` | Autenticado | Path `id` | `200 OK` — `NecessidadeDoacaoResponse` | `401`, `404` |

### 1.5 Campanhas Emergenciais (`/campanhas`) — `CampanhaEmergencialController` (`ICampanhaEmergencialController`)

| Método | Rota | Acesso / Perfil | Request | Response (Sucesso) | Erros mapeados |
|---|---|---|---|---|---|
| `POST` | `/campanhas` | `ROLE_RECEPTOR` | `CampanhaEmergencialRequest` (`nome` [≤150], `descricao`? [≤1000], `enderecoRecebimento`, `itens`? [≤50 `ItemCampanhaRequest`]) | `201 Created` + `Location` — `CampanhaEmergencialResponse` (`situacao = ATIVA`) | `400`, `401`, `403` |
| `GET` | `/campanhas/minhas` | `ROLE_RECEPTOR` | — | `200 OK` — `List<CampanhaEmergencialResponse>` | `401`, `403` |
| `GET` | `/campanhas` | Autenticado | Query: `situacao` (`SituacaoCampanha`, padrão `ATIVA`), `page`, `size`, `sort` | `200 OK` — `Page<CampanhaEmergencialResponse>` | `400`, `401` |
| `GET` | `/campanhas/{id}` | Autenticado | Path `id` | `200 OK` — `CampanhaEmergencialResponse` | `401`, `404` |

### 1.6 Disponibilidades de Transporte (`/disponibilidades`) — `DisponibilidadeTransporteController` (`IDisponibilidadeTransporteController`)

| Método | Rota | Acesso / Perfil | Request | Response (Sucesso) | Erros mapeados |
|---|---|---|---|---|---|
| `POST` | `/disponibilidades` | `ROLE_TRANSPORTADOR` | `DisponibilidadeTransporteRequest` (`tipoVeiculo`, `capacidade` > 0, `tipoUnidade`, `origem`: `EnderecoRequest`, `dataDisponivel` >= hoje, `horarioDisponivel`?) | `201 Created` + `Location` — `DisponibilidadeTransporteResponse` | `400`, `401`, `403`, `409` já existe disponibilidade ativa na data |
| `GET` | `/disponibilidades/minhas` | `ROLE_TRANSPORTADOR` | — | `200 OK` — `List<DisponibilidadeTransporteResponse>` | `401`, `403` |
| `PATCH` | `/disponibilidades/{id}/cancelar` | `ROLE_TRANSPORTADOR` (dono) | Path `id` | `204 No Content` | `401`, `403` não dono ou situação ≠ `DISPONIVEL`, `404` |
| `PATCH` | `/disponibilidades/{id}/encerrar` | `ROLE_TRANSPORTADOR` (dono) | Path `id` | `204 No Content` | `401`, `403` não dono, `404`, `409` não está `RESERVADA` ou há entregas `TRANSPORTE_VINCULADO` |

### 1.7 Reservas e Entregas (`/reservas`) — `ReservaEntregaController` (`IReservaEntregaController`)

| Método | Rota | Acesso / Perfil | Request | Response (Sucesso) | Erros mapeados |
|---|---|---|---|---|---|
| `POST` | `/reservas` | `ROLE_RECEPTOR` | `ReservaEntregaRequest` (`ofertaId`, `precisaTransporte`) | `201 Created` + `Location` — `ReservaEntregaResponse` | `400`, `401`, `403`, `404` oferta inexistente, `409` oferta não `DISPONIVEL` |
| `GET` | `/reservas/aguardando-transporte` | `ROLE_TRANSPORTADOR` | — | `200 OK` — `List<ReservaEntregaResponse>` (`doador=null`, `receptor=null`, endereços resumidos `cidade`/`estado`) | `401`, `403` |
| `GET` | `/reservas/minhas/receptor` | `ROLE_RECEPTOR` | — | `200 OK` — `List<ReservaEntregaResponse>` (dados completos) | `401`, `403` |
| `GET` | `/reservas/minhas/transportador` | `ROLE_TRANSPORTADOR` | — | `200 OK` — `List<ReservaEntregaResponse>` (dados completos) | `401`, `403` |
| `GET` | `/reservas/minhas/doador` | `ROLE_DOADOR` | — | `200 OK` — `List<ReservaEntregaResponse>` (dados completos) | `401`, `403` |
| `PATCH` | `/reservas/{id}/vincular-transporte` | `ROLE_TRANSPORTADOR` | `VincularTransporteRequest` (`disponibilidadeId`) | `200 OK` — `ReservaEntregaResponse` | `400`, `401`, `403` disponibilidade de outro usuário, `404`, `409` situação/unidade/capacidade/expirada/concorrência |
| `PATCH` | `/reservas/{id}/concluir` | `ROLE_TRANSPORTADOR` ou `ROLE_RECEPTOR` | Path `id` | `204 No Content` | `401`, `403` não autorizado para o estado atual, `404`, `409` estado inválido |
| `PATCH` | `/reservas/{id}/cancelar` | `ROLE_RECEPTOR` (dono) | Path `id` | `204 No Content` | `401`, `403`, `404`, `409` estado ≠ `AGUARDANDO_TRANSPORTE` / `AGUARDANDO_RETIRADA` |

---

## 2. Divergências Encontradas entre Documentação, Prompt e API

1. **Arquivo `desing.txt` ausente no repositório:**  
   O arquivo `desing.txt` citado no prompt não existe no repositório. Os tokens visuais, paleta, tipografia (`DM Sans`), raios e diretrizes de contraste definidos na seção 4 do prompt serão usados como base integral do design system.
2. **Endpoint de sessão ("quem sou eu"):**  
   `docs/arquitetura.md` cita a possibilidade de um endpoint "quem sou eu", porém a API não implementa `GET /auth/me` ou `GET /usuarios/me`. A restauração da sessão no carregamento da SPA será feita via `POST /auth/refresh`, que retorna `200 OK` com `UsuarioResponse` quando o cookie `refresh_token` é válido (ou `401` silencioso para visitante).
3. **Sessão após cadastro (ADR 0004 vs implementação real + ADR 0009):**  
   O ADR 0004 afirma que o cadastro autentica imediatamente com cookies, porém a API real implementa o ADR 0009: o cadastro retorna `201 Created` sem cookies e exige confirmação do código de 6 dígitos em `POST /auth/verificar-email` (validade de 90 s) para ativar a conta e emitir cookies de sessão.
4. **Limite máximo de senha no cadastro:**  
   PRD 0001 e o prompt indicam senha de 8 a 72 caracteres. Na API real:
   - `CadastroComumRequest` (Doador e Transportador) valida `@Size(min = 8, max = 60)` — limite máximo é **60** caracteres.
   - `CadastroReceptorRequest` e `RedefinirSenhaRequest` validam `@Size(min = 8, max = 72)` — limite máximo é **72** caracteres.
   O frontend seguirá exatamente o contrato de cada endpoint (60 para Doador/Transportador, 72 para Receptor/redefinição).
5. **Limites e unidade em Ofertas (`OfertaDoacaoRequest` / `ItemDoacaoRequest`):**  
   O prompt/PRD 0002/ADR 0006 citam 1 a 20 itens, descrição máx. 100 e unidade fixa em `KG`. Na API real:
   - `OfertaDoacaoRequest` permite `1` a **`50`** itens (`@Size(max = 50)`).
   - `ItemDoacaoRequest.descricao` permite até **`500`** caracteres (`@Size(max = 500)`).
   - `ItemDoacaoRequest.tipoUnidade` é obrigatório (`@NotNull`) com todo o enum `TipoUnidade` (`KG`, `T`, `G`, `ML`, `CAIXAS`, `CESTAS_BASICAS`, `SEMUNIDADE`) — essencial para compatibilidade com `DisponibilidadeTransporte`, já que `ReservaEntregaService` exige que todos os itens tenham a mesma `TipoUnidade` da disponibilidade vinculada. O frontend usará os enums e limites reais da API, com `KG` selecionado por padrão para agilizar o cadastro.
6. **Filtros em memória na listagem de ofertas (`GET /ofertas`):**  
   `OfertaDoacaoService.listarDisponiveis` aplica os filtros `categoria`, `cidade` e `estado` em memória sobre a página retornada do banco, podendo fazer `totalElements` divergir da quantidade filtrada na página atual. A UI lidará com páginas parcialmente vazias quando filtros estiverem ativos e exibirá nota informativa ao usuário.
7. **Corpo de erro padronizado (`ErrorResponse`) e `403` nos filtros:**  
   `GlobalExceptionHandler` retorna `{ "message": string, "status": number }` (sem `code` e sem mapa `fieldErrors`; erros 400 concatenam `"campo: mensagem; ..."`). Além disso, `SecurityConfig.accessDeniedHandler` retorna `403` com corpo vazio, enquanto `EmailNaoVerificadoException` retorna `403` com `{ "message": "E-mail não verificado...", "status": 403 }`. O interceptor HTTP diferenciará os dois casos inspecionando `status === 403` e a mensagem retornada.
8. **CORS e URL base de recuperação de senha:**  
   `application.properties` configura `app.cors.origins=http://localhost:3000` e `app.recuperacao-senha.url-base=http://localhost:3000`. O Vite (`vite.config.ts`) será configurado para rodar na porta `3000` em desenvolvimento (`server.port: 3000`) para respeitar o CORS atual sem alterar o backend e garantir que os links de redefinição enviados por e-mail apontem para a rota real `/redefinir-senha?token=...`.
9. **Privacidade em listagens públicas (`GET /ofertas`, `GET /necessidades`, `GET /campanhas`):**  
   Em `GET /ofertas`, o backend oculta o endereço completo (mantendo `cidade`/`estado`), mas envia `ContatoResponse` (`nome`, `email`, `telefone`). Em `GET /necessidades`, a API envia `localEntrega` e `ContatoResponse` completos. Em cumprimento à regra de privacidade da seção 6 do prompt, os cards de listagens não vinculadas exibirão apenas nome da entidade/doador e `cidade/UF`, evitando expor e-mail, telefone ou endereço completo fora de contextos de vínculo/detalhe autorizado.
10. **Nome dos campos de `ContatoResponse`:**  
    O campo `telefone` existe em `ContatoResponse`, mas os endpoints de cadastro (`/auth/*/cadastro`) não aceitam `telefone` nem `endereco` para Doador/Transportador, portanto `telefone` frequentemente virá `null` nas respostas da API.

---

## 3. Recursos Pedidos / Documentados que NÃO Existem na API (Estados Informativos)

Os recursos abaixo não possuem endpoints na API atual e serão apresentados com **estado informativo explícito** na interface (sem dados falsos nem simulação):

1. **Matchmaking automático (RF-007 / PRD 0004):** sem endpoints de geração ou aceite/recusa de propostas automáticas.
2. **Montagem automática de rotas / integração com mapas:** sem endpoints de roteirização; o transportador visualiza suas entregas vinculadas na disponibilidade com os respectivos endereços de retirada e entrega.
3. **Atualização manual de situação / histórico de doação (RF-009 / PRD 0005):** não há endpoint de timeline nem transições além de `cancelar` e `concluir`.
4. **Edição de ofertas após criação:** `OfertaDoacaoController` não possui `PUT`/`PATCH` de edição (apenas `PATCH /ofertas/{id}/cancelar` quando `DISPONIVEL`).
5. **Edição e cancelamento de necessidades:** `NecessidadeDoacaoController` não possui endpoints de edição, atendimento ou cancelamento.
6. **Edição, encerramento e cancelamento de campanhas emergenciais:** `CampanhaEmergencialController` não possui endpoints de alteração de dados nem de transição para `ENCERRADA` ou `CANCELADA`.
7. **Edição de disponibilidade de transporte e desvinculação de entrega:** `DisponibilidadeTransporteController` não permite editar (apenas cancelar quando `DISPONIVEL` e encerrar quando `RESERVADA` sem pendências); `ReservaEntregaController` não possui endpoint para o transportador desvincular uma entrega após assumi-la.
8. **Consulta pública de disponibilidades de transporte:** existe apenas `GET /disponibilidades/minhas` para o próprio transportador.
9. **Detalhe individual de reserva (`GET /reservas/{id}`):** reservas são consultadas pelas listagens por perfil (`/reservas/minhas/*` e `/reservas/aguardando-transporte`).
10. **Consulta/edição de dados da própria conta (`GET/PATCH /usuarios/me`):** a sessão atual é retornada apenas via `UsuarioResponse` (`id`, `nome`, `email`, `tipoUsuario`) nos endpoints de auth.

---

## 4. Versões de Dependências e Compatibilidade

### Já presentes em `doalink-view/package.json`:
- `react`: `^19.3.1` / `react-dom`: `^19.3.1` (suporta `<ViewTransition>` e `addTransitionType` nativamente)
- `tailwindcss`: `^4.3.5` + `@tailwindcss/vite`: `^4.3.5` (Tailwind CSS v4 — configuração CSS-first via `@theme` em `src/index.css`)
- `vite`: `^8.3.6`
- `typescript`: `~6.0.3` (modo estrito)

### Planejadas para instalação:
| Pacote | Versão estável verificada | Papel |
|---|---|---|
| `react-router-dom` | `7.18.6` | Roteamento, guards por perfil e navegação |
| `react-hook-form` | `7.92.0` | Gerenciamento de formulários e erros por campo |
| `zod` | `4.6.7` | Schemas de validação em português |
| `@hookform/resolvers` | `7.0.7` | Integração RHF + Zod (`zodResolver`) |
| `axios` | `1.21.1` | Cliente HTTP centralizado com interceptors |
| `lucide-react` | `1.77.0` | Ícones acessíveis padronizados |
| `vitest` (dev) | `5.1.1` | Runner de testes unitários e de integração |
| `@testing-library/react` (dev) | `16.3.3` | Testes de componentes/hooks React 19 |
| `@testing-library/dom` (dev) | `^10.0.0` (peer) | Suporte ao Testing Library |
| `@testing-library/jest-dom` (dev) | `8.0.0` | Matchers DOM acessíveis |
| `@testing-library/user-event` (dev) | `14.6.13` | Simulação realista de teclado/clique |
| `jsdom` (dev) | `31.0.1` | Ambiente DOM para Vitest |

**Análise de compatibilidade:**  
`@hookform/resolvers@7.0.7` declara em `peerDependencies`: `"zod": "^3.25.0 || ^4.0.0"` e `"react-hook-form": "^7.55.0"`. Portanto, **não há incompatibilidade** entre `zod@4.6.7`, `react-hook-form@7.92.0` e `@hookform/resolvers@7.0.7`. `vitest@5.1.1` aceita `vite: "^6.4.0 || ^7.0.0 || ^8.0.0"`, compatível com `vite@8.3.6`. `@testing-library/react@16.3.3` suporta `react: "^18.0.0 || ^19.0.0"`.

---

## 5. Plano de Implementação em Etapas

1. **Etapa 1 — Dependências, configuração e Design System:**  
   Instalar dependências no `doalink-view`, configurar Vitest, porta `3000` no Vite, `.env.example`, fonte `DM Sans`, tokens `@theme` / CSS variables em `src/index.css` (com variações WCAG AA verificadas), e componentes base em `components/ui/`, `components/layout/` e `components/feedback/`.
2. **Etapa 2 — Cliente HTTP, Tipos, Utils e Contexto de Sessão:**  
   Espelhar todos os DTOs/enums da API em `types/`, implementar `services/api.ts` (cookies HttpOnly, normalização de erros 400/401/403/409, refresh serializado único em 401, diferenciação de 403), `utils/` (validadores e máscaras de CPF/CNPJ/CEP, formatadores de data/unidade/labels) e `contexts/AuthContext.tsx` + guards de rota (`PrivateRoute`, `RoleRoute`, `PublicOnlyRoute`).
3. **Etapa 3 — Autenticação completa e Páginas Públicas:**  
   Implementar Landing Page (`/`) com identidade DoaLink e CTAs por perfil, Login (`/login`), Cadastro com seleção de perfil e campos condicionais (`/cadastro`), Confirmação de e-mail com contagem regressiva de 90 s e reenvio (`/verificar-email`), Recuperação de senha (`/recuperar-senha`), Redefinição de senha com validação prévia de token (`/redefinir-senha`), Acesso negado (`/acesso-negado`) e 404 (`*`).
4. **Etapa 4 — Layout Autenticado e Painel Inicial:**  
   Shell responsivo com menu lateral/superior adaptativo, identificação visual do perfil ativo, breadcrumbs, logout e painel inicial contextualizado por perfil usando exclusivamente dados reais da API.
5. **Etapa 5 — Domínios Funcionais:**  
   - **Ofertas:** listagem pública paginada com filtros (`/ofertas`), detalhe com controle de visibilidade e ação de reserva para Receptor (`/ofertas/:id`), minhas ofertas + cancelamento (`/minhas-ofertas`), criação com itens dinâmicos e endereço opcional (`/ofertas/nova`).
   - **Necessidades:** criação pelo Receptor (`/necessidades/nova`), minhas necessidades (`/minhas-necessidades`), consulta autenticada por situação (`/necessidades`) e detalhe (`/necessidades/:id`) com estado informativo para edição/cancelamento inexistentes na API.
   - **Campanhas Emergenciais:** criação pelo Receptor (`/campanhas/nova`), minhas campanhas (`/minhas-campanhas`), listagem por situação (`/campanhas`) e detalhe (`/campanhas/:id`) com estado informativo para edição/encerramento inexistentes na API.
   - **Disponibilidades de Transporte:** criação e gestão pelo Transportador (`/disponibilidades`, cancelar `DISPONIVEL`, encerrar `RESERVADA`).
   - **Reservas e Entregas:** fluxo completo para Receptor (`/minhas-reservas`, concluir retirada direta, cancelar antes do vínculo), Transportador (`/entregas-disponiveis` para vincular disponibilidade compatível e `/minhas-entregas` para concluir) e Doador (`/reservas-ofertas` para acompanhar reservas e transportadores).
6. **Etapa 6 — Testes, Acessibilidade, Auditoria e Documentação Final:**  
   Implementar suíte Vitest + Testing Library (schemas Zod, interceptors Axios 401/403, guards de rota e fluxos críticos de formulário), executar `tsc`, `eslint`, `build`, `vitest` e `npm audit`, revisar acessibilidade/responsividade e atualizar `doalink-view/README.md`.

