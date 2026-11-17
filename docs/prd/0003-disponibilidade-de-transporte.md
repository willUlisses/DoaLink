# PRD 0003: Disponibilidade de Transporte

- **Status:** Proposto
- **Data:** 2026-11-16
- **Especificações:** [Spec 0005 — Disponibilidade de Transporte](../specs/0005-disponibilidade-de-transporte.md)
- **ADRs relacionados:** [ADR 0009 — Modelo de dados para disponibilidade de transporte](../adr/0009-modelo-de-dados-disponibilidade-de-transporte.md)

---

## Descrição geral

O DoaLink permite que um Transportador registre uma janela de disponibilidade de transporte, declarando o tipo de veículo, a capacidade e sua unidade, a origem e a data (com horário opcional). O destino não é fixado no cadastro da disponibilidade, sendo derivado das ofertas de doação e necessidades vinculadas posteriormente na formação de rotas (RF-007). Uma mesma disponibilidade pode atender múltiplas ofertas até o limite da capacidade disponível.

O sistema **não faz** nesta versão:
- Montagem automática de rotas via Google Maps (prevista para etapa posterior).
- Edição de disponibilidade após a criação.
- Listagem pública geral de disponibilidades fora do mecanismo de sugestões (RF-007).
- Rastreamento do veículo em tempo real.
- Comunicação direta entre Transportador e Doador/Receptor.
- Cálculo de frete ou valores financeiros.
- Verificação documental do veículo ou do transportador.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Transportador | Pessoa física ou jurídica cadastrada como transportador | Registrar disponibilidade, consultar suas próprias disponibilidades e cancelar disponibilidade |

---

## Requisitos funcionais

### RF-005 — Disponibilidade de transporte

**Descrição:** O sistema permite que um Transportador registre uma disponibilidade de transporte informando o tipo de veículo, a capacidade e unidade, a origem e a data (com horário se necessário). O sistema disponibiliza o registro na listagem pessoal do Transportador, permite o cancelamento enquanto disponível e considera esses dados nos critérios de compatibilidade e rotas (RF-007).

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Tipo de veículo ¹ | ENUM | — | Obrigatório. Deve pertencer ao catálogo fixo de categorias de veículo. |
| \*Capacidade | Double | — | Obrigatório. Deve ser maior que zero (`> 0`). |
| Capacidade restante | Double | — | Gerenciada pelo sistema. Inicializada igual à `Capacidade`; abatida conforme ofertas forem vinculadas na rota. |
| \*Unidade da capacidade ² | ENUM (`TipoUnidade`) | — | Obrigatória. Compartilhada com o catálogo de unidades de itens. |
| \*Origem ³ | Endereço estruturado | CEP 00000-000 | Obrigatória. Local de partida do transportador. Armazenar CEP só dígitos. |
| \*Data disponível | DATE | DD/MM/AAAA | Obrigatória. Deve ser igual ou posterior à data atual. |
| Horário disponível | TIME | HH:mm | Opcional. Informado quando o transportador tiver janela de horário específica. |
| Situação | ENUM | — | Gerenciada pelo sistema: `DISPONIVEL`, `RESERVADA`, `CONCLUIDA`, `CANCELADA`. Valor inicial: `DISPONIVEL`. |

¹ Categorias de veículo aceitas (conforme [ADR 0009](../adr/0009-modelo-de-dados-disponibilidade-de-transporte.md)):
- `MOTO` — Moto
- `CARRO_PASSEIO` — Carro de passeio
- `UTILITARIO_LEVE` — Utilitário leve (furgão/van)
- `CAMINHONETE` — Caminhonete / Pickup
- `CAMINHAO_PEQUENO` — Caminhão pequeno (VUC / 3/4)
- `CAMINHAO_MEDIO` — Caminhão médio (toco / truck)
- `CAMINHAO_GRANDE` — Caminhão grande (carreta / bitrem)
- `OUTRO` — Outro

² A unidade utiliza o mesmo catálogo `TipoUnidade` de ofertas e necessidades (`KG`, `T`, `G`, `ML`, `CAIXAS`, `CESTAS_BASICAS`, `SEMUNIDADE`), permitindo comparação direta no mecanismo de sugestões (RF-007).

³ Campos obrigatórios da origem: CEP, estado (UF), cidade, bairro e logradouro. Número opcional (padrão `S/N`) e complemento opcional.

**Critérios de aceite**

- **CA-005-1:** Dado um Transportador autenticado com dados de disponibilidade válidos (tipo de veículo, capacidade, unidade, origem e data), quando o Transportador registrar a disponibilidade, então o sistema cria o registro com situação `DISPONIVEL` e `capacidadeRestante` igual à `capacidade`.
- **CA-005-2:** Dado uma disponibilidade registrada, quando o sistema processar critérios de compatibilidade e rotas (RF-007), então a capacidade restante (e sua unidade), a origem, os destinos das ofertas/necessidades e a data (e horário, quando informado) são considerados na sugestão.
- **CA-005-3:** Dado uma data passada ou capacidade menor ou igual a zero, quando o Transportador tentar registrar a disponibilidade, então o sistema rejeita com erro de validação.
- **CA-005-4:** Dado uma disponibilidade registrada pelo Transportador, quando o Transportador consultar suas disponibilidades, então o registro aparece na listagem pessoal.
- **CA-005-5:** Dado um Transportador dono de uma disponibilidade com situação `DISPONIVEL`, quando solicitar o cancelamento, então o sistema altera a situação para `CANCELADA`.
- **CA-005-6:** Dado um Transportador que não é dono da disponibilidade ou uma disponibilidade com situação diferente de `DISPONIVEL`, quando tentar cancelar, então o sistema recusa com erro de autorização.
- **CA-005-7:** Dado um Transportador que já possui uma disponibilidade ativa (`DISPONIVEL` ou `RESERVADA`) para uma data, quando tentar cadastrar outra para a mesma data, então o sistema rejeita com erro de conflito.
- **CA-005-8:** Dado um Transportador dono de uma disponibilidade `RESERVADA` sem entregas pendentes (`TRANSPORTE_VINCULADO`), quando solicitar o encerramento, então a situação passa para `CONCLUIDA`.

---

## Requisitos não funcionais

- **RNF-01 (LGPD):** Dados de contato do Transportador (nome, e-mail, telefone) são exibidos apenas em contexto de compatibilidade — nunca CPF/CNPJ.
- **RNF-02 (Integridade):** O Transportador não pode manter mais de uma disponibilidade ativa (`DISPONIVEL` ou `RESERVADA`) na mesma data, mas uma mesma disponibilidade pode agrupar múltiplas ofertas em rota enquanto houver `capacidadeRestante > 0`.

---

## Decisões pendentes

_(Nenhuma decisão pendente para o RF-005.)_

---

## Fora do escopo desta versão

- Geração automática de rotas via API do Google Maps (será tratada em etapa específica)
- Edição de disponibilidade após criação
- Listagem pública geral de disponibilidades (consulta externa ocorrerá via RF-007)
- Rastreamento em tempo real do veículo
- Cálculo de frete ou custos de transporte
- Verificação documental do transportador ou veículo
- Comunicação direta com Doador/Receptor

---

## Critérios de conclusão

Todos os critérios de aceite cobertos por testes automatizados passando na suíte do back-end (`Spec 0005`).
