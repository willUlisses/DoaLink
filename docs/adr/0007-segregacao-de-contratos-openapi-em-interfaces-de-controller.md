# ADR 0007: Segregação de contratos OpenAPI em interfaces de controller

- **Status:** Aceito
- **Data:** 2026-11-09

## Contexto

Com a adoção do SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui`), as classes de controller (`AuthController` e `OfertaDoacaoController`) passaram a acumular um volume massivo de anotações declarativas de documentação (`@Tag`, `@Operation`, `@ApiResponses`, `@ApiResponse`, `@Parameter`, `@Schema`).

Esse acúmulo gerou forte poluição visual (*code clutter*), obscurecendo a responsabilidade primária das classes de controller (receber a requisição, validar DTOs, delegar ao service e construir o `ResponseEntity`).

Alternativas consideradas:

- **Manter as anotações diretamente nos métodos dos controllers:** abordagem padrão de tutoriais, mas torna as classes muito extensas e dificulta a manutenção e leitura do fluxo de código.
- **Documentação externa em arquivos YAML/JSON avulsos:** desacopla totalmente o código, mas perde a validação estática de tipos Java e sincronização automática com DTOs e endpoints.
- **Segregação em interfaces de contrato (`IAuthController`, `IOfertaDoacaoController`) (escolhida):** declaração de todas as anotações OpenAPI/Swagger e mapeamentos de roteamento HTTP nas interfaces, mantendo as classes concretas estritamente focadas na implementação com anotações de negócio mínimas e `@Override`.

## Decisão

Adota-se o padrão de interfaces de contrato para os controllers da API:
1. Para cada controller, existe uma interface correspondente (ex: `IAuthController` para `AuthController`, `IOfertaDoacaoController` para `OfertaDoacaoController`).
2. A interface define os contratos de rota (`@RequestMapping`, `@PostMapping`, etc.), anotações de documentação OpenAPI (`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`) e anotações de validação de entrada (`@Valid`, `@RequestBody`, `@CookieValue`, etc.).
3. A classe concreta implementa a interface, anota-se apenas com `@RestController` e `@RequiredArgsConstructor`, e implementa os métodos com `@Override`, contendo unicamente a lógica de orquestração do endpoint.

## Consequências

- **Legibilidade e Elegância:** Controllers concretos ficam enxutos, claros e focados apenas na execução da regra de transporte HTTP e delegação a services.
- **Contratos Explícitos:** A interface serve como um catálogo limpo da API pública e facilita consultas de documentação e contratos.
- **Compatibilidade:** O Spring MVC e o SpringDoc OpenAPI herdam integralmente os mapeamentos e anotações das interfaces implementadas.
- **Custo aceito:** Introdução de um arquivo adicional (interface) por controller e necessidade de manter as assinaturas sincronizadas entre interface e classe concreta em caso de alteração de parâmetros.
