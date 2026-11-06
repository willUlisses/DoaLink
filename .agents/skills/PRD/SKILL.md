---
name: prd
description: Como escrever requisitos de produto neste projeto. Use ao criar ou alterar um PRD em docs/prd/, ou ao adicionar, mudar ou retirar requisitos, campos, critérios de aceite, decisões pendentes e itens fora do escopo.
---

# PRD

Um PRD descreve o que o produto faz, para quem e por quê, com campos, regras de validação e critérios de aceite verificáveis. Como construir fica nas specs (`docs/specs/`) e o motivo das decisões estruturais, nos ADRs (`docs/adr/`).

O PRD é a versão em Markdown do documento de requisitos do projeto (DoaLink). Ele é a fonte de verdade para agentes e pessoas: se o PDF e o PRD divergirem, a divergência deve ser resolvida com a pessoa responsável antes de qualquer implementação.

## Quando escrever

- Crie um PRD para um produto ou uma nova versão com objetivo próprio.
- Altere o PRD existente quando o comportamento esperado do produto mudar.
- Mudanças internas, sem efeito para quem usa, não entram no PRD.

## Arquivo

Caminho: `docs/prd/NNNN-titulo-em-kebab-case.md`, com o próximo número livre de 4 dígitos.

## Modelo

```markdown
# PRD NNNN: Título

- **Status:** Proposto
- **Data:** AAAA-MM-DD
- **Especificações:** links para as specs

## Descrição geral

Contexto, problema e proposta em poucos parágrafos. Inclua o que o sistema
NÃO faz (ex.: acompanhamento da carga, chat entre usuários) quando alguém
puder supor o contrário.

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|

## Requisitos funcionais

### RF-001 — Nome do requisito

**Descrição:** Comportamento observável, em uma ou duas frases.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Nome | VARCHAR(150) | — | Obrigatório. |

Notas dos campos (ex.: "obrigatório só para o perfil X", "CPF e CNPJ são
excludentes").

**Critérios de aceite**

- **CA-001-1:** Dado ..., quando ..., então ...
- **CA-001-2:** ...

## Requisitos não funcionais

- **RNF-01 (tema):** Qualidade exigida, mensurável.

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Critérios mínimos de senha | RF-001, RF-003 | Em aberto |

## Fora do escopo desta versão

## Critérios de conclusão
```

Omita a tabela de campos em requisitos que não têm entrada de dados (ex.: "o participante consegue encerrar a sessão"). Mantenha o requisito curto: se a descrição passar de um parágrafo, divida em dois requisitos.

## Status

- `Proposto`: aguardando a decisão da pessoa responsável. Um agente sempre cria o PRD com este status.
- `Aceito`: os requisitos estão valendo.

## Regras

- **Verificável:** todo critério de aceite precisa poder virar ao menos um teste. Prefira "A listagem mostra 10 ofertas por página" a "A listagem é fácil de usar".
- **Sem implementação:** nomeie o perfil e o comportamento, sem citar classes, tabelas, rotas ou bibliotecas. Os tipos da coluna "Tipo sugerido" são orientação de dados, não decisão de modelagem: quem decide é a spec.
- **Um comportamento por requisito.** Cada critério de aceite cobre um único cenário (sucesso ou um erro específico).
- **Numeração estável:** `RF-001`, `RNF-01` e `CA-001-1` não são renumerados nem reaproveitados, porque as specs os citam. Itens novos recebem o próximo número; itens retirados são marcados como `(retirado)`.
- **Máscaras são de apresentação:** o requisito deve dizer que o valor é armazenado normalizado (sem pontuação) e que a máscara é responsabilidade da interface.
- **Obrigatoriedade por perfil:** indique com `*` os campos obrigatórios e use nota de rodapé quando a obrigatoriedade depender do perfil (ex.: endereço obrigatório só para Receptor).
- **Não invente requisitos.** Tudo o que o documento original marca como "a definir" vai para **Decisões pendentes**, não vira regra. Se a pendência bloquear uma implementação, pergunte à pessoa responsável.
- **Registre em "Fora do escopo"** o que alguém poderia supor incluído.
- **Dados pessoais:** CPF, CNPJ, telefone e endereço são dados pessoais (LGPD). Quando um requisito os exibir, diga quais perfis podem vê-los e se aparecem completos ou parciais.
- **Ao alterar requisitos**, atualize as specs que os citam.

## Exemplo resumido

```markdown
### RF-001 — Cadastro de participante

**Descrição:** O sistema permite o cadastro de participantes nos perfis
Doador, Transportador e Receptor e gera um número de registro por cadastro.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Nome ou organização | VARCHAR(150) | — | Obrigatório. |
| \*CPF ¹ | CHAR(11) | 000.000.000-00 | Só dígitos; valida dígitos verificadores e unicidade. |
| \*CNPJ ¹ | CHAR(14) | 00.000.000/0000-00 | Só dígitos; valida dígitos verificadores e unicidade. |
| \*E-mail | VARCHAR(254) | — | Formato válido, único, sem diferenciar maiúsculas. |
| \*Senha | hash | — | Nunca armazenada em texto puro. |
| \*Telefone | VARCHAR(15) | (00) 00000-0000 | Armazenar só dígitos. |
| Endereço ² | vários campos | CEP 00000-000 | Obrigatório apenas para Receptor. |

¹ CPF e CNPJ são excludentes: informa-se exatamente um. Receptor informa
sempre CNPJ.
² CEP, estado, cidade, bairro, logradouro e número.

**Critérios de aceite**

- **CA-001-1:** Dados obrigatórios válidos e inéditos criam o participante e geram o número de registro.
- **CA-001-2:** CPF/CNPJ ou e-mail inválidos ou já cadastrados são rejeitados.
- **CA-001-3:** Senha fora dos critérios mínimos é rejeitada.
- **CA-001-4:** Cadastro de Receptor sem CNPJ ou sem endereço é rejeitado.
```
