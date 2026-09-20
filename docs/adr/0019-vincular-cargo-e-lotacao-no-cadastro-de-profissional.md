# 0019 — Vincular cargo e lotação (e portanto salário) no cadastro de profissional

## Status

Aceita e implementada.

## Contexto

O usuário identificou, testando o cadastro de profissional (`/profissionais/novo`), que o
formulário não vincula o profissional a nenhum **cargo** nem **unidade de saúde**. Pelo desenho já
existente do domínio de RH (`docs/rh/MODELO-RH.md`, seção 2.4), isso é um problema real, não só
cosmético: **não existe "salário do profissional" solto** — o salário é sempre derivado de
`Lotacao` (vínculo profissional↔unidade↔cargo, com histórico) → `Cargo` → `TabelaSalarial` vigente.
Sem uma `Lotacao`, o profissional recém-cadastrado não tem cargo, e sem cargo não há de onde
derivar salário nenhum.

Conferido no código: `ProfissionalService.create()` sempre só gravou dados pessoais/contato — nunca
criou uma `Lotacao`. O endpoint que faz isso (`POST /api/v1/lotacao/`, `LotacaoService.criar`,
implementado na Fase 0 do backend) já cobre admissão/transferência/mudança de cargo com a regra de
fechar a lotação vigente antes de abrir uma nova — só nunca tinha sido ligado ao fluxo de cadastro.

## Decisão

### Orquestração no frontend, sem mudar `ProfissionalService`

`POST /api/v1/profissional/` continua com o mesmo escopo de sempre (dados pessoais/contato). O
formulário Angular de cadastro passa a encadear, depois do sucesso, mais duas chamadas:

1. `GET /api/v1/profissional/{cpf}` (método já existente, reaproveitado de
   `profissional-desligar.ts`) — necessário porque `POST /api/v1/profissional/` só devolve a
   matrícula gerada embutida numa string livre (`details: "Nome: X, Matrícula: Y"`), não um campo
   estruturado. Fazer parsing dessa string seria frágil; buscar de novo por CPF dá a matrícula
   tipada.
2. `POST /api/v1/lotacao/` com a matrícula obtida, a Unidade e o Cargo escolhidos no formulário, e
   `dataInicio` = a mesma `dataAdmissao` já preenchida (não duplica campo de data), `motivo` fixo
   em `"Admissão"` (mesmo vocabulário livre que `Lotacao.motivo` já usa).

Mantém `LotacaoService` como dono único das regras de negócio de lotação (fechar a vigente antes de
abrir nova, etc.) — mesmo que, pra um profissional novo, a regra de "fechar vigente" nunca se
aplique. Evita duplicar essa lógica em dois lugares. Mesmo padrão de encadeamento de chamadas já
usado no formulário (busca de CEP em cadeia).

### Campos novos, obrigatórios

Unidade de saúde e Cargo (selects) — obrigatórios porque `LotacaoRequestDto` já exige os dois
(`@NotNull`). `dataAdmissao`, que já existia como campo opcional, **passa a ser obrigatória** — sem
data não dá pra criar a lotação. `jornadaSemanalHoras` fica de fora por ora: não é o que foi pedido
e não tem exigência clara de domínio ainda (entra quando a tela de Ponto, fatia 6, precisar dela).

### Gap de backend pequeno, resolvido: `uuid` em `UnidadeSaudeResponseDto`

`GET /api/v1/unidade-saude/` retornava `UnidadeSaudeResponseDto` sem `uuid` — só `nome` e campos de
negócio, porque o subsistema de `UnidadeDeSaude` sempre foi endereçado por nome (`{nome}` como path
variable em todos os outros endpoints). Mas `LotacaoRequestDto.unidadeId` exige UUID. Sem expor o
`uuid`, não daria pra popular um `<select>` de unidades e depois criar a lotação. Adicionado o
campo (+ construtor + `fromHierarquicoResponseDto`) — puramente aditivo, nenhum endpoint ou
comportamento existente muda.

### Falha parcial: profissional criado, lotação não

Se a criação do profissional funcionar mas a busca por CPF ou a criação da lotação falharem (rede,
etc.), o profissional **já foi criado** — não existe endpoint pra desfazer isso. A UI mostra uma
mensagem explícita com a matrícula gerada, pedindo pra registrar a lotação manualmente. Não existe
ainda uma tela de "criar lotação" avulsa pra isso (é a fatia 3 do frontend de RH, não construída) —
o registro manual, por ora, significa chamar a API diretamente ou esperar a fatia 3.

## Trade-offs considerados

**Orquestrar no frontend (escolhida)**
- ✅ Mantém `ProfissionalService`/`LotacaoService` com responsabilidades separadas, sem duplicar a
  regra de negócio de lotação.
- ✅ Não muda o contrato de `POST /api/v1/profissional/`, usado também pelos testes Robot
  existentes.
- ❌ Cadastro de profissional passa a depender de 3 chamadas HTTP sequenciais em vez de 1 — mais
  superfície pra falha parcial (ver seção acima).

**Fazer `ProfissionalService.create()` criar a `Lotacao` internamente, numa transação só (rejeitada)**
- ✅ Uma chamada só, sem risco de estado parcial.
- ❌ Rejeitada: `ProfissionalService` passaria a conhecer `UnidadeDeSaude`/`Cargo`/regras de
  lotação — join de responsabilidades que o desenho de Fase 0 deliberadamente separou em serviços
  distintos. Também exigiria mudar o contrato de `ProfissionalRequestDto`, usado pelos testes Robot
  existentes desde a ADR-0014.

## Consequências

**Positivas**
- Um profissional cadastrado pelo formulário já sai com cargo e unidade vinculados — e, por
  consequência, com salário derivável (ainda sem uma tela que componha e mostre esse valor; isso é
  a fatia 4, "Composição remuneratória", ainda não construída).

**Negativas / pendências**
- Estado parcial possível (profissional sem lotação) se a 2ª/3ª chamada falhar — mitigado só por
  mensagem de erro clara, não por um mecanismo de retry ou compensação automática.
- `jornadaSemanalHoras` não é coletado no cadastro — quando a fatia 6 (Ponto) precisar dele, terá
  que ser preenchido depois, via uma futura tela de edição de lotação.

## Referências

- [MODELO-RH.md](../rh/MODELO-RH.md) seção 2.4 — desenho de `Lotacao` e derivação de salário.
- [ADR-0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md),
  [ADR-0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — contrato de `Profissional`
  que este ADR não altera.
- [ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) — mesmo padrão de
  documentação de decisões de frontend.
