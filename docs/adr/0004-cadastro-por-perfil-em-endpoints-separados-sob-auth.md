# ADR 0004: Cadastro por perfil em endpoints separados sob /auth

- **Status:** Aceito
- **Data:** 2026-11-06

## Contexto

O DoaLink possui três perfis de participantes: Doador, Transportador e Receptor. Cada perfil possui atributos, obrigatoriedades e regras cadastrais distintas. Receptores são compulsoriamente pessoas jurídicas (PJ) e necessitam de endereço completo informado no momento do cadastro para que as doações possam ser destinadas e retiradas em seus pontos físicos. Já Doadores e Transportadores podem ser pessoas físicas (PF) ou jurídicas (PJ) e não necessitam cadastrar endereço no fluxo inicial de acesso.

Permitir que o perfil seja informado dentro do corpo da requisição (`Request Body`) abre riscos de segurança (como injeção acidental ou intencional de perfil não autorizado), exige DTOs polimórficos de alta complexidade com validações condicionais frágeis e quebra a clareza do contrato REST da API.

Alternativas consideradas:
- Endpoint único `POST /auth/cadastro` com discriminador no corpo: descartado pela complexidade de validação condicional no Bean Validation e risco de vulnerabilidade de escalada/injeção de perfil.
- Endpoints de cadastro alocados sob recursos de domínio (`POST /doadores`, `POST /transportadores`, `POST /receptores`): descartado por dispersar rotas públicas e onboarding fora do módulo de autenticação (`/auth`), complicando as regras de segurança pública no Spring Security e a padronização do ciclo de login inicial.
- Endpoints dedicados sob o prefixo `/auth` (`/auth/doadores/cadastro`, `/auth/transportadores/cadastro`, `/auth/receptores/cadastro`): escolhido por explicitar o perfil no contrato da URL, permitir DTOs estritos e manter o ciclo de autenticação centralizado.

## Decisão

O cadastro de novos participantes é segregado em três endpoints públicos dedicados sob o prefixo `/auth`:
- `POST /auth/doadores/cadastro` (consome `CadastroComumRequest`)
- `POST /auth/transportadores/cadastro` (consome `CadastroComumRequest`)
- `POST /auth/receptores/cadastro` (consome `CadastroReceptorRequest`)

O perfil (`TipoUsuario`) é fixado exclusivamente pela rota acionada, sendo mapeado pelo controller para o respectivo método de negócio do `AuthService` (`cadastrarDoador`, `cadastrarTransportador` e `cadastrarReceptor`). Não é aceito nenhum campo discriminador de perfil no corpo das requisições.

O fluxo de sucesso de qualquer um desses cadastros autentica imediatamente o novo participante, retornando HTTP `201 Created`, o DTO `UsuarioResponse` no corpo e emitindo os cookies HttpOnly (`access_token` e `refresh_token`), evitando a necessidade de redirecionar para um login manual após o cadastro.

## Consequências

- **Ganhos:**
  - Impossibilidade estrutural de adulteração ou injeção indevida de perfil via manipulação de payload JSON.
  - Validações estritas em tempo de compilação e parsing: `CadastroReceptorRequest` exige compulsoriamente dados de CNPJ e endereço aninhado (`@Valid @NotNull EnderecoRequest`), enquanto `CadastroComumRequest` suporta flexibilidade entre CPF e CNPJ sem acoplar campos de endereço desnecessários.
  - Alinhamento pleno com a herança JPA `JOINED`: o service instancia diretamente a classe correspondente (`Doador`, `Transportador` ou `Receptor`) já associando o `TipoUsuario` fixado pelo construtor da subclasse.
  - Excelente experiência de onboarding (UX), logando o usuário imediatamente após o registro.
- **Custos aceitos:**
  - Manutenção de três rotas públicas de cadastro no `AuthController` e na configuração de segurança (`SecurityConfig`).
  - O front-end precisa apontar a submissão de cadastro para rotas distintas conforme a aba/opção de perfil selecionada pelo usuário.
