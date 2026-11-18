# ADR 0010: Criação e Gestão de Campanhas Emergenciais por Receptores (ONGs)

- **Status:** Aceito
- **Data:** 2026-11-16

## Contexto

O requisito funcional RF-008 do PRD 0005 prevê o registro de campanhas ou eventos emergenciais que agrupam necessidades e ampliam a visibilidade para Doadores e Transportadores.

Três aspectos técnicos e de negócio precisavam de padronização:
1. **Perfil autorizado:** O PRD deixava em aberto se administradores, doadores ou ONGs poderiam criar campanhas. No ecossistema do DoaLink, as ONGs (Receptores) são os agentes na linha de frente que centralizam a triagem e o recebimento de doações em situações críticas.
2. **Localização vs. Região:** O requisito original mencionava "região afetada", porém doadores e transportadores necessitam de um local exato e viável para entrega ou transbordo dos mantimentos. Portanto, definiu-se que a localização deve ser o endereço estruturado onde as doações são recebidas.
3. **Contextualização da Causa:** Campanhas emergenciais necessitam de narrativa para engajar doadores e descrever a gravidade da emergência (ex.: enchentes, deslizamentos), demandando um campo textual expressivo (`descricao` de até 1000 caracteres).

Alternativas consideradas:
- **Criação de campanha aberta para qualquer perfil:** Descartada, pois geraria spam e campanhas sem capacidade de armazenamento ou triagem confiável.
- **Região afetada como polígono ou texto livre:** Descartada, pois dificultaria rotas logísticas e entrega direta. O modelo padronizado `@Embedded Endereco` já existente no projeto é ideal para direcionar as entregas.
- **Vínculo indireto com necessidades existentes:** Descartado para o cadastro inicial; a campanha pode definir diretamente seus itens necessários (`ItemCampanha`), garantindo uma criação atômica e rápida na emergência.

## Decisão

1. Apenas usuários autenticados com o perfil Receptor (`ROLE_RECEPTOR`) têm permissão para criar e gerenciar campanhas emergenciais (`CampanhaEmergencial`).
2. A campanha possui um campo `descricao` com validação de até 1000 caracteres para detalhamento da causa e circunstâncias da emergência.
3. O local da campanha é mapeado pelo objeto embutido `@Embedded Endereco enderecoRecebimento`, aproveitando a estrutura e validações já estabelecidas no sistema (`EnderecoRequest`).
4. Cada campanha nasce no estado `SituacaoCampanha.ATIVA` e pode agrupar itens de necessidade emergencial (`ItemCampanha`) contendo tipo de item (`TipoItemDoacao`), unidade de medida (`TipoUnidade`), quantidade positiva e descrição complementar.

## Consequências

- **Ganhos:**
  - Garantia de que campanhas partem de entidades receptoras cadastradas e verificáveis.
  - Clareza logística para os doadores e voluntários quanto ao ponto físico de entrega.
  - Reutilização direta dos componentes de validação de endereço e itens de doação já consolidados.
- **Custos/Desvantagens aceitas:**
  - Usuários que não sejam ONGs não podem abrir campanhas de arrecadação direta na plataforma sem antes cadastrar uma entidade receptora.
