# PRD 0001: Autenticação, Cadastro e Sessão

- **Status:** Proposto
- **Data:** 2026-11-09
- **Especificações:** [Spec 0001 — Autenticação, Cadastro de Usuários e Gestão de Sessão](../specs/0001-autenticacao-e-cadastro.md)

---

## Descrição geral

O DoaLink permite o cadastro de participantes em três perfis distintos: **Doador**, **Transportador** e **Receptor** (ONGs). Cada perfil tem dados específicos além dos dados comuns. O sistema gera um identificador único por cadastro e controla o acesso via sessão stateless com cookies HttpOnly.

O sistema **não faz**:
- Confirmação de e-mail automática no cadastro (requisito desejável separado — RF-010).
- Login social (OAuth2, Google, etc.).
- Suporte a múltiplos perfis em um mesmo cadastro (ver Decisões pendentes).
- Troca de perfil após o cadastro.

---

## Perfis de uso

| Perfil | Quem é | O que precisa fazer |
|---|---|---|
| Doador | Pessoa física ou jurídica que deseja realizar doações | Cadastrar-se, fazer login, encerrar sessão, recuperar senha |
| Transportador | Pessoa física ou jurídica que oferece transporte de cargas | Cadastrar-se, fazer login, encerrar sessão, recuperar senha |
| Receptor | ONG ou organização receptora de doações | Cadastrar-se com CNPJ e endereço obrigatórios, fazer login, encerrar sessão, recuperar senha |

---

## Requisitos funcionais

### RF-001 — Cadastro de participante

**Descrição:** O sistema permite o cadastro de participantes nos perfis Doador, Transportador e Receptor e gera um número de identificação por cadastro. Conforme o perfil, o sistema solicita dados específicos além dos dados comuns.

**Dados comuns (todos os perfis)**

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*Nome ou nome da organização | VARCHAR(150) | — | Obrigatório. Texto livre. |
| \*CPF ¹ | CHAR(11) | 000.000.000-00 | Armazenar apenas os 11 dígitos. Validar dígitos verificadores e unicidade. Aplicável à pessoa física. |
| \*CNPJ ¹ | CHAR(14) | 00.000.000/0000-00 | Armazenar apenas os 14 dígitos. Validar dígitos verificadores e unicidade. Aplicável à pessoa jurídica. |
| \*E-mail | VARCHAR(254) | — | Formato de e-mail válido (não é case-sensitive). Único no sistema. |
| \*Senha | hash | — | Nunca armazenada em texto puro. Mínimo 8 caracteres, máximo 72 caracteres. Sem exigência de caracteres especiais. |
| Número de identificação ² | BIGINT ou UUID | — | Gerado automaticamente pelo sistema; não é editável nem exibido diretamente ao participante. |

¹ CPF e CNPJ são excludentes: informa-se exatamente um. Receptor informa sempre CNPJ.
² O número de identificação é interno ao sistema. A forma de exibição, se houver, é a definir.

**Dados do perfil Receptor (além dos comuns)**

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*CNPJ | CHAR(14) | 00.000.000/0000-00 | Obrigatório. Mesmo campo do dado comum, aqui sempre CNPJ. |
| \*CEP ³ | CHAR(8) | 00000-000 | Obrigatório. Armazenar apenas os 8 dígitos. |
| \*Estado (UF) ³ | CHAR(2) | — | Obrigatório. Sigla da unidade federativa brasileira. |
| \*Cidade ³ | VARCHAR(255) | — | Obrigatório. |
| \*Bairro ³ | VARCHAR(255) | — | Obrigatório. |
| \*Logradouro ³ | VARCHAR(255) | — | Obrigatório. |
| Número do endereço | VARCHAR(20) | — | Opcional. Quando não informado, o sistema registra `S/N`. |
| Complemento | VARCHAR(100) | — | Opcional. Bloco, apartamento, ponto de referência. |

³ CEP, estado, cidade, bairro e logradouro são obrigatórios somente no cadastro do perfil Receptor. Dados de endereço (telefone, endereço) nos demais perfis são facultativos.

**Dados do perfil Doador**

O Doador preenche apenas os dados comuns.

**Dados do perfil Transportador**

O Transportador preenche apenas os dados comuns.

**Critérios de aceite**

- **CA-001-1:** Dado dados obrigatórios válidos e inéditos, quando o participante enviar o cadastro, então o sistema cria o participante e gera seu número de identificação.
- **CA-001-2:** Dado CPF/CNPJ ou e-mail inválidos ou já associados a outro cadastro, quando o participante tentar cadastrar, então o sistema rejeita o cadastro e informa o campo em conflito.
- **CA-001-3:** Dado uma senha com menos de 8 caracteres ou mais de 72 caracteres, quando o participante tentar cadastrar, então o sistema rejeita e informa o critério não atendido.
- **CA-001-4:** Dado um cadastro de Receptor sem CNPJ, sem CEP, sem estado, sem cidade, sem bairro ou sem logradouro, quando o participante tentar cadastrar, então o sistema rejeita e informa os campos ausentes.
- **CA-001-5:** Dado um cadastro de Receptor sem número de endereço, quando o participante enviar o cadastro, então o sistema registra `S/N` no campo número.

---

### RF-002 — Login do usuário

**Descrição:** O sistema permite que participantes cadastrados entrem com e-mail e senha e encerrem a sessão.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*E-mail de acesso | VARCHAR(254) | — | Deve corresponder a um e-mail cadastrado. |
| \*Senha | — | — | Validada contra o hash armazenado. Nunca exposta. |

**Critérios de aceite**

- **CA-002-1:** Dado credenciais válidas, quando o participante fizer login, então o sistema concede acesso às funcionalidades autorizadas para o seu perfil e emite os tokens de sessão.
- **CA-002-2:** Dado credenciais inválidas (e-mail não cadastrado ou senha incorreta), quando o participante tentar fazer login, então o sistema não concede acesso e informa que a autenticação falhou — sem revelar qual campo está incorreto.
- **CA-002-3:** Dado uma sessão ativa, quando o participante solicitar o encerramento, então o sistema invalida os tokens de sessão e o participante perde o acesso.

---

### RF-003 — Recuperação de senha

**Descrição:** O sistema permite que um participante cadastrado solicite a recuperação de senha informando o e-mail associado à conta. O sistema envia um link de uso único para o e-mail cadastrado; ao acessar o link válido, o participante cadastra uma nova senha. A nova senha deve atender aos mesmos critérios de RF-001. O sistema não armazena nem exibe a senha em texto puro.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| \*E-mail de acesso | VARCHAR(254) | — | Deve corresponder a um e-mail cadastrado. |
| \*Senha nova | hash | — | Mesmos critérios de RF-001. Nunca armazenada em texto puro. |
| Token de recuperação | VARCHAR (hash) | — | Link de uso único enviado ao e-mail. Deve ter validade e ser invalidado após o uso. A validade é a definir. |

**Critérios de aceite**

- **CA-003-1:** Dado um e-mail associado a uma conta cadastrada, quando o participante solicitar a recuperação, então o sistema envia um link de uso único para o e-mail informado e inicia o fluxo de recuperação.
- **CA-003-2:** Dado um e-mail não cadastrado, quando o participante solicitar a recuperação, então o sistema não permite a alteração de senha de outra conta — a resposta não deve indicar se o e-mail existe ou não.
- **CA-003-3:** Dado um fluxo de recuperação válido, quando o participante informar uma nova senha que atenda aos critérios de RF-001, então o sistema atualiza a senha da conta.
- **CA-003-4:** Dado um link de recuperação inválido, expirado ou já utilizado, quando o participante tentar definir uma nova senha, então o sistema rejeita a solicitação e mantém a senha atual.
- **CA-003-5:** Após a recuperação concluída, quando o participante tentar entrar com a nova senha, então o sistema autentica com sucesso.
- **CA-003-6:** Após a recuperação concluída, quando o participante tentar entrar com a senha anterior, então o sistema não autentica.

---

### RF-010 — Confirmação de e-mail por código (desejável)

**Descrição:** Após o cadastro, o sistema envia um código único ao e-mail do participante para confirmar a titularidade do endereço de e-mail. O participante informa o código para ativar a conta. Requisito desejável; não bloqueia o cadastro na versão atual.

| Campo | Tipo sugerido | Máscara | Validação e observações |
|---|---|---|---|
| Código de confirmação | 6 dígitos numéricos | 000-000 | Enviado ao e-mail cadastrado. Uso único. Validade a definir. |

**Critérios de aceite**

- **CA-010-1:** Dado um cadastro recém-realizado, quando o sistema enviar o código de confirmação, então o e-mail cadastrado deve receber o código.
- **CA-010-2:** Dado um código válido, quando o participante informá-lo, então o sistema marca a conta como confirmada.
- **CA-010-3:** Dado um código inválido ou expirado, quando o participante informá-lo, então o sistema rejeita e permite reenvio.

---

## Requisitos não funcionais

- **RNF-01 (Segurança — senhas):** Senhas armazenadas exclusivamente como hash BCrypt. Nenhum endpoint expõe senha em texto puro ou hash.
- **RNF-02 (Segurança — sessão):** Sessão stateless com access token JWT (15 min) e refresh token opaco (7 dias), ambos em cookies `HttpOnly`, `SameSite=Strict`. O front-end nunca lê nem armazena tokens.
- **RNF-03 (LGPD):** CPF, CNPJ, e-mail e endereço são dados pessoais coletados com finalidade de identificação única e operação do serviço. São visíveis apenas ao próprio participante e, parcialmente, a outros participantes nas situações descritas nas specs. Nenhum dado de documento (CPF/CNPJ) é exposto pela API de resposta pública.
- **RNF-04 (Unicidade):** E-mail e documento (CPF ou CNPJ) são únicos no sistema, garantidos por constraint de banco e verificação antecipada no serviço com retorno 409 em conflito.

---

## Decisões pendentes

| Item | Requisitos afetados | Situação |
|---|---|---|
| Validade do link de recuperação de senha | RF-003 | Em aberto |
| Validade do código de confirmação de e-mail | RF-010 | Em aberto |
| Múltiplos perfis por cadastro | RF-001 | Não suportado nesta versão — decisão futura pendente |
| Número de identificação: forma de exibição ao participante | RF-001 | Em aberto |

---

## Fora do escopo desta versão

- Confirmação de e-mail no fluxo de cadastro (implementado como RF-010 desejável)
- Login social (OAuth2, Google, Facebook)
- Troca de perfil após o cadastro
- Múltiplos perfis em um mesmo cadastro

---

## Critérios de conclusão

- RF-001, RF-002 e RF-003: todos os critérios de aceite cobertos por testes automatizados passando.
- RF-010: implementado quando a decisão de validade do código for tomada e a integração de envio de e-mail estiver definida.

