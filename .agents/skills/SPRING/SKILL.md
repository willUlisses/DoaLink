---
name: spring-doalink
description: Orienta boas práticas de Spring Boot e a arquitetura do DoaLink (API em camadas, regras no service, JPA, DTOs e validação, segurança com JWT em cookies HttpOnly, transações, Flyway e testes). Use ao implementar, revisar ou refatorar o back-end, ao criar endpoints, services, entidades, migrations ou testes, e ao avaliar se uma mudança segue a arquitetura e as decisões registradas do projeto.
---

# Spring Boot no DoaLink

## Quando usar

- Implementar ou revisar qualquer código em `doalink-api`.
- Criar endpoint, service, entidade, repository, DTO, mapper, migration ou teste.
- Decidir onde uma regra de negócio deve ficar.
- Avaliar segurança, transações, persistência e contrato da API.
- Antes de abrir PR ou declarar uma funcionalidade concluída.

## Regras que não se negociam

1. **Regra de negócio no service.** Controller não valida regra de negócio, não acessa repository e não monta entidade.
2. **Controller fino.** Recebe DTO validado, chama um método de service, escreve cookies quando necessário e devolve DTO + status.
3. **Entidade JPA nunca sai na API.** Request e response são `record`s próprios; o mapper converte DTO ↔ entidade.
4. **Validação em duas camadas.** Formato e obrigatoriedade no DTO com Bean Validation; regra que depende de estado (unicidade, dono, situação, saldo) no service.
5. **Autorização no servidor.** `SecurityConfig`/`@PreAuthorize` define o acesso; esconder botão no front não autoriza nada.
6. **Segredo e configuração por ambiente.** `JWT_SECRET`, banco, CORS e `Secure` de cookie nunca ficam fixos no código.
7. **Schema versionado.** Toda mudança de entidade exige migration Flyway; `ddl-auto` não cria nem altera tabela.
8. **Dado sensível não vaza.** Senha, hash, token, CPF/CNPJ completo e endereço não aparecem em resposta, log ou mensagem de erro.

## Mapa do back-end

```
controller → service → repository → PostgreSQL
                ↘ exceções de domínio → GlobalExceptionHandler
config: SecurityConfig, RequestFilter, UsuarioDetails
service: AuthService, OfertaDoacaoService, JwtService, CookieService, RefreshTokenService
```

| Camada | Responsabilidade | Não faz |
|---|---|---|
| `controller` | HTTP, `@Valid`, cookies, status, `Location` | regra, repository, entidade |
| `service` | regra, normalização, transação, exceções | `HttpServletRequest`/`Response` |
| `repository` | consultas Spring Data JPA | regra de negócio |
| `entities` | mapeamento e relacionamentos | decisão de fluxo |
| `dtos` | contrato de entrada/saída | acesso a banco |
| `mapper` | conversão DTO ↔ entidade | regra de negócio |
| `config` | segurança, filtro, beans | regra de domínio |
| `exception` | exceções e handler HTTP | regra de domínio |

## Fluxo para implementar uma funcionalidade

1. **Leia o contrato.** Consulte `docs/arquitetura.md`, a spec da funcionalidade, os ADRs e o PRD quando existir. Se não houver spec, escreva antes de codar.
2. **Defina o contrato HTTP.** Rota, roles, request, response, status de sucesso e erros. Não invente campo sem requisito.
3. **Modele e migre.** Entidade + migration Flyway na mesma mudança. Considere `NOT NULL`, `UNIQUE`, `CHECK`, FKs e índices.
4. **Crie os DTOs.** `record` de request com Bean Validation; response mínimo; nunca exponha entidade.
5. **Mapeie.** Mapper estático ou componente dedicado, sem regra de negócio.
6. **Implemente a consulta.** Repository com derived query ou `@Query`; evite N+1 com `@EntityGraph`.
7. **Implemente o service.** Regra, normalização, autorização de domínio, transação e exceções.
8. **Feche a borda.** Controller + `SecurityConfig`; cookies só no controller.
9. **Teste.** Service, controller e caminhos de erro; 401, 403 e sucesso nas rotas protegidas.
10. **Atualize a documentação.** Spec/PRD/ADR e `arquitetura.md` quando o comportamento ou a decisão mudar.

## Spring e injeção de dependência

- Injeção por construtor com `@RequiredArgsConstructor`; evite `@Autowired` em campo.
- Prefira `final` nos campos injetados.
- Não instancie repository/service com `new`.
- Configuração em `@Configuration`/`@Bean`; não espalhe `@Value` por todo lugar.
- Use `record` para DTOs e tipos imutáveis.

```java
@Service
@RequiredArgsConstructor
public class ExemploService {

    private final ExemploRepository repository;

    @Transactional(readOnly = true)
    public ExemploResponse buscar(Long id) {
        return repository.findById(id)
                .map(ExemploResponse::from)
                .orElseThrow(() -> new NotFoundException("Exemplo não encontrado: " + id));
    }
}
```

## Transações

- A fronteira transacional fica no **service**, não no controller nem no repository.
- Consultas usam `@Transactional(readOnly = true)`.
- Chamada de rede externa (CNPJ, e-mail) não deve manter transação aberta.
- `@Transactional(noRollbackFor = ...)` só com motivo explícito e documentado.
- Service não recebe `HttpServletRequest`/`HttpServletResponse`; falha vira exceção de domínio.
- Unicidade: a verificação prévia (`existsBy...`) serve para mensagem amigável; a garantia final é a constraint.

## Contrato e validação

- Request é `record` com `@NotBlank`, `@Email`, `@CPF`, `@CNPJ`, `@Size`, `@Positive`, `@Valid`.
- Regra que cruza campos do DTO usa `@AssertTrue` em método com `@JsonIgnore`.
- Métodos auxiliares não começam com `get`/`is`, para não virarem propriedade JSON.
- O nome do componente do `record` é o nome do campo JSON. **Confira a grafia** antes de mapear; um `quantidadeu` quebra o cliente.
- Normalize no service: e-mail em minúsculas, documento sem pontuação, UF em maiúsculas.
- Máscara é apresentação: o back-end recebe com ou sem pontuação e normaliza.
- Response não inclui senha, hash, documento completo nem dados internos.
- Documente status: 201 com `Location`, 204 sem corpo, 400 validação, 401 sessão, 403 perfil/propriedade, 404 recurso, 409 conflito.

## JPA e consultas

- Herança `JOINED`; `TipoUsuario` fixado no construtor da subclasse, com setter `protected`.
- **Todo enum persistido usa `@Enumerated(EnumType.STRING)`.** Sem isso, reordenar o enum corrompe os dados.
- Relacionamentos `LAZY` por padrão; carregue o necessário com `@EntityGraph` e evite N+1.
- Não coloque regra de negócio na entidade.
- Use `@PrePersist`/`@PreUpdate` para timestamps, quando o padrão do projeto for esse.
- Prefira filtros e paginação no banco. Se o limite aceito for filtrar em memória, registre isso na spec e o efeito em `totalElements`.
- Ao adicionar campo `NOT NULL`, atualize **entidade + DTO + mapper + migration + testes**. Campo obrigatório sem preenchimento falha em runtime.
- Prefira nomes explícitos para colunas de `@Embedded` (`@AttributeOverrides`) quando houver risco de colisão.
- Não use `ddl-auto=update`; o projeto valida com `validate`.

### Padrão de enum com descrição

Enums de domínio podem carregar um rótulo legível para exibição, usando `@Getter` do Lombok:

```java
@Getter
public enum TipoUnidade {
    SEMUNIDADE("SemUnidade"),
    KG("Kilo"),
    T("Tonelada"),
    CAIXAS("Caixas"),
    CESTAS_BASICAS("CestasBasicas"),
    ML("MiliLitros"),
    G("Gramas");

    private final String Descrisao;

    TipoUnidade(String descrisao) {
        Descrisao = descrisao;
    }
}
```

- Cada constante declara seu rótulo no construtor; o getter é gerado pelo Lombok.
- O nome do campo define o getter. No código atual, `TipoUnidade` e `TipoItemDoacao` usam `Descrisao` (com maiúscula e typo) e geram `getDescrisao()`; `TipoPessoa` usa `descricao` e gera `getDescricao()`.
- Não renomeie o campo de um enum isoladamente: padronize todos os enums e ajuste consumidores, testes, JSON e documentação na mesma mudança.
- Persistência continua com `@Enumerated(EnumType.STRING)` no campo da entidade; o rótulo é para apresentação, não para o banco.

## Segurança

- API stateless: sem `HttpSession`; `SessionCreationPolicy.STATELESS`.
- `access_token` é JWT curto em cookie `HttpOnly`; `refresh_token` é opaco, com hash SHA-256 no banco.
- Refresh token rotaciona a cada uso; reutilização revoga todos os tokens do usuário.
- Cookies: `HttpOnly`, `SameSite=Strict`, `Secure` conforme ambiente. Em HTTP local, `Secure=true` faz o navegador descartar o cookie.
- CSRF desabilitado só porque `SameSite=Strict` e mesmo site; mudou o domínio do front, reveja em ADR.
- CORS com origens explícitas e `allowCredentials(true)`; nunca `*`.
- RBAC por `ROLE_<TipoUsuario>` na `SecurityConfig`; regra de propriedade (dono da oferta, situação) no service.
- Senha com BCrypt; lembre do limite de 72 bytes.
- O front nunca lê token nem usa `localStorage`; toda chamada usa `credentials: 'include'`.
- 401/403 de filtro não têm corpo; erros do handler usam `{"message","status"}`.
- Trate `DataIntegrityViolationException` como 409, para a corrida de unicidade não virar 500.
- Não registre token, senha, documento ou endereço em log.

## Banco e Flyway

- PostgreSQL; migrations em `src/main/resources/db/migration` no padrão `VNNN__descricao.sql`.
- Uma migration por mudança lógica; nunca edite migration já aplicada.
- Teste a subida em banco vazio, não só no banco de desenvolvimento.
- `baseline-on-migrate=true` é transição; não use para esconder ausência de migration.
- Constraints são a garantia final: `UNIQUE`, `NOT NULL`, `CHECK` e FKs coerentes com o domínio.
- Crie índice para coluna usada em filtro/ordenação frequente e para FK consultada.
- Tarefas `@Scheduled` só rodam com `@EnableScheduling` na aplicação.

## Configuração e ambiente

- Segredos vêm de variável de ambiente (`JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD`).
- Separe perfis de desenvolvimento e produção: cookie `Secure`, CORS, banco e log.
- Em dev com Vite, a origem costuma ser `http://localhost:5173`; o CORS precisa bater exatamente.
- Não commite `.env`, token, senha ou chave.
- Falhe cedo quando configuração obrigatória faltar; não coloque segredo padrão no repositório.

## Testes

- **Service:** JUnit 5 + Mockito, cobrindo sucesso, regra e exceções de domínio.
- **Controller:** MockMvc, cobrindo status, corpo, `Location`, validação e erros.
- **Segurança:** visitante (401), perfil errado (403) e perfil correto (sucesso).
- **Persistência:** teste de integração com PostgreSQL real/Testcontainers para constraints e queries.
- Teste caminhos de erro, não só o caminho feliz.
- Rode `./mvnw test` antes de concluir. Se a suíte não compila, a tarefa não está pronta.
- Só marque `[x]` em checklist de teste quando o teste existir e a suíte estiver verde.

## Checklist de revisão

- [ ] A regra está no service, não no controller/entidade/componente.
- [ ] O controller só recebe DTO validado e devolve DTO.
- [ ] Entidade JPA não aparece em request/response.
- [ ] O nome dos campos JSON bate com DTO, mapper, spec e cliente.
- [ ] Campos novos têm validação, normalização e migration.
- [ ] `@Transactional` está na fronteira correta; consultas usam `readOnly`.
- [ ] Enums persistidos usam `@Enumerated(STRING)`.
- [ ] Enums com descrição usam `@Getter` e o nome do campo/getter está consistente.
- [ ] Não há N+1 óbvio nem filtro em memória sobre página.
- [ ] 400/401/403/404/409 estão mapeados e testados.
- [ ] Dados sensíveis não aparecem em resposta/log.
- [ ] Roles e regra de propriedade estão no servidor.
- [ ] Migration existe e sobe em banco vazio.
- [ ] `./mvnw test` passa.
- [ ] Spec/PRD/ADR/arquitetura foram atualizados quando necessário.

## Armadilhas já vistas neste projeto

| Sintoma | Causa provável | Correção |
|---|---|---|
| Build quebra com “enum constant not expected” | construtor do enum com nome de outro tipo | construtor com o nome do próprio enum |
| Getter inesperado (`getDescrisao`/`getDescricao`) | nome do campo do enum (grafia/maiúscula) | padronizar campo, getter e consumidores em uma mudança única |
| `no suitable constructor found for Usuario(...)` | subclasse chamando `super` com assinatura antiga | alinhar construtores ou usar setter |
| `cannot find symbol: quantidade()` | campo do record grafado diferente (`quantidadeu`) | corrigir o nome no DTO, mapper, testes e JSON |
| `POST /ofertas` falha com `NOT NULL` | campo novo obrigatório não preenchido no mapper/DTO | mapear o campo ou remover a obrigatoriedade; atualizar migration |
| Unicidade concorrente retorna 500 | `DataIntegrityViolationException` sem handler | tratar como 409 |
| Job de expurgo não roda | `@Scheduled` sem `@EnableScheduling` | habilitar scheduling ou remover o job |
| Login 200 e próxima chamada 401 | cookie `Secure` em HTTP ou CORS/porta errada | `app.cookie.secure=false` em dev e origem correta |
| Código e spec divergem | mudança sem atualizar contrato | atualizar spec/PRD/ADR na mesma mudança |

## Definição de pronto (back-end)

- Contrato definido e documentado.
- Migration versionada e aplicada em banco vazio.
- DTOs, mapper, service, repository, controller e segurança implementados.
- Erros de domínio mapeados com status correto.
- Testes de sucesso e erro passando (`./mvnw test`).
- Nenhum dado sensível exposto.
- Documentação atualizada e links consistentes.
