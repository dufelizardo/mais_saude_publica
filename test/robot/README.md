# Testes de aceitação (Robot Framework) — Mais Saúde Pública API

Suíte de testes de aceitação/API em [Robot Framework](https://robotframework.org/), seguindo o
**Layered Keyword-Driven Framework (LKDF)**: [dufelizardo/Layered-Keyword-Driven-Framework-LKDF](https://github.com/dufelizardo/Layered-Keyword-Driven-Framework-LKDF).

Complementa (não substitui) os testes JUnit/MockMvc em `../../src/test/java/` — aqueles rodam
in-process como parte do build Maven (`mvnw test`); estes aqui rodam de fora pra dentro, contra
a aplicação real de pé, via HTTP.

## Camadas (POM → FLOW → SCENARIO → TEST)

Regra de dependência: **estritamente unidirecional, de cima pra baixo**. `TEST` depende de
`SCENARIO`, que depende de `FLOW`, que depende de `POM` — nunca o contrário, e nenhuma camada
pula a imediatamente abaixo.

| Camada | Responsabilidade única | Não pode conter |
|---|---|---|
| **POM** (`src/pom/`) | Chamada HTTP crua (`GET On Session`, `POST On Session`...). Zero asserção. | Regra de negócio, decisão condicional. |
| **FLOW** (`src/flow/`) | Monta payload, decide qual cenário por status code, faz as asserções. | URL/endpoint cru, chamada HTTP direta. |
| **SCENARIO** (`src/scenario/`) | Blueprint declarativo da jornada — só repassa pro FLOW. | Dado hardcoded, asserção. |
| **TEST** (`test/`) | Casos de teste (`*.robot`), injeta os dados de entrada (nome, status code esperado). | Lógica condicional, asserção direta. |

`src/resource/` reúne os agregadores (`config/driven`, `config/schema`) e os dados/schemas por
domínio (`data_driven/`, `schema/`) — importados pelas outras camadas, nunca o contrário.

## Estrutura

```
src/
├── pom/
│   ├── common/                        sessão HTTP compartilhada (Get Mais Saude Publica Session)
│   └── {federal,estadual,municipal,regional}/<operacao>/   8 operações cada, suíte completa
├── flow/
│   ├── common/                        schema_validation_flow.resource (Validate Object/Array Schema)
│   └── {federal,estadual,municipal,regional}/<operacao>/
├── scenario/
│   └── {federal,estadual,municipal,regional}/<operacao>/
└── resource/
    ├── config/{driven,schema}/        agregadores únicos que todo teste importa
    ├── data_driven/{federal,estadual,municipal,regional}/    dicionários de casos negativos
    └── schema/{common,federal,estadual,municipal,regional}/  common = SuccessResponseDto/ErrorExceptionResponse
test/
├── federal/
│   ├── create_federal/                POST   /api/v1/federal/                              201, 409, 400, 422
│   ├── get_all_federal/                GET   /api/v1/federal/                              200, 400
│   ├── get_federal_by_nome/            GET   /api/v1/federal/{nome}                         200, 404, 400
│   ├── update_nome_federal/            PATCH /api/v1/federal/{nome}                         200, 404, 400, 409
│   ├── update_contato_federal/         PATCH /api/v1/federal/contato/{nome}                 200, 404, 400
│   ├── update_horario_funcionamento_federal/  PATCH .../horario-de-funcionamento/{nome}     200, 404, 400
│   ├── update_horario_atendimento_federal/    PATCH .../horario-de-atendimento/{nome}       200, 404, 400
│   └── desabilitar_federal/            DELETE /api/v1/federal/des-habilitar/{nome}          200, 404, 400
├── estadual/                          mesmo conjunto de 8 operações do Federal, mais:
│   └── create_estadual/                POST   /api/v1/estadual/   201, 409, 400, 422 (x2 causas), 404
├── municipal/                         mesmo conjunto de 8 operações do Federal, mais:
│   └── create_municipal/               POST   /api/v1/municipal/  201, 409, 400, 422 (x2 causas), 404
└── regional/                          mesmo conjunto de 8 operações do Federal, mais:
    └── create_regional/                POST   /api/v1/regional/    201, 409, 400, 422 (x2 causas), 404
```

Os **4 domínios** (Federal, Estadual, Municipal, Regional) estão com os 8 endpoints cobertos cada
(28 + 30 + 30 + 30 = 118 casos de teste) — a suíte está completa.

**Estadual, Municipal e Regional têm 2 casos a mais que Federal no create**, porque têm
`administracaoSuperior`: 404 (superior não existe, AQUAQE-13) e um segundo motivo de 422 (superior
de nível errado, AQUAQE-22) — Federal não tem superior, não tem esses dois casos.

A cadeia de seed encadeia os 4 níveis: `Seed A Municipal Unit` chama `Seed A Estadual Unit`, que
chama `Seed A Federal Unit` — criar um Regional de teste, por exemplo, sempre cria os 3 superiores
primeiro.

Os testes de cada domínio carregam o prefixo da história do Jira correspondente nos nomes dos
casos (`CT-XXX - AQUAQE-143: ...` para Federal, `AQUAQE-144:` para Estadual, `AQUAQE-145:` para
Municipal, `AQUAQE-146:` para Regional) — convenção de rastreabilidade adotada pelo time, não
parte do padrão LKDF em si.

## ⚠️ Armadilha arquitetural: variáveis de POM são globais, não por arquivo

Cada `*_pom.resource` originalmente declarava uma variável `${ENDPOINT}` (`/federal`, `/regional`,
etc.). **Isso quebrou silenciosamente ao adicionar o segundo domínio**: no Robot Framework,
variáveis declaradas em `*** Variables ***` de resource files são **globais pro processo inteiro**
— não isoladas por arquivo. A última que for carregada durante o parse "vence" para **todo mundo**,
inclusive keywords de arquivos diferentes que também referenciam `${ENDPOINT}`. Com um domínio só,
nunca dava pra perceber (só existia um valor). Ao criar os POMs de Regional, alguns testes de
Federal passaram a montar o payload certo mas mandar a chamada HTTP pro path errado — silenciosamente,
sem erro de sintaxe, só o corpo da resposta batendo errado.

**Correção aplicada:** cada POM agora usa um nome de variável prefixado por domínio
(`${FEDERAL_ENDPOINT}`, `${ESTADUAL_ENDPOINT}`, `${MUNICIPAL_ENDPOINT}`, `${REGIONAL_ENDPOINT}`).
**Em qualquer novo `*_pom.resource` futuro, sempre use esse padrão** — nunca reintroduza um
`${ENDPOINT}` genérico compartilhado.

Nota sobre `des-habilitar/{nome}`: o controller declara `consumes=application/json` mas **não** lê
o DTO do corpo (`@RequestBody` ausente) — o `ativo` é vinculado como query param, e a requisição
ainda assim recebe 415 sem o header `Content-Type`. Comportamento confirmado contra a aplicação
real antes de escrever o keyword (ver comentário em `src/pom/federal/desabilitar_federal/`).

## Cobertura de HTTP status — o que é real vs. só documentado no Swagger

As anotações `@ApiResponse` dos controllers (`ApiErrorResponsesMutacao`/`Busca`/`Listagem`) listam
200, 201, 400, 401, 403, 404, 409, 422, 500 (e 504 na listagem). Nem todos são caminhos reais na
implementação atual — antes de escrever um teste para um código, confirme se ele é alcançável:

| HTTP | Alcançável (Federal)? | Motivo |
|---|---|---|
| 200 / 201 | ✅ Sim | Sucesso — coberto |
| 400 | ✅ Sim | `MethodArgumentNotValidException` (campo obrigatório em branco, `@Valid`) — coberto |
| 401 | ❌ Não | `ResourceUnauthorizedException` existe e tem handler mapeado, mas nunca é lançada em lugar nenhum — não há autenticação implementada (ADR-0006 é só proposta) |
| 403 | ❌ Não | Não existe exception, handler nem lógica de autorização no código — é documentação Swagger sem nenhum backing |
| 404 | ✅ Sim | Coberto |
| 409 | ✅ Sim | `ResourceConflictException` (nome duplicado) — implementado nesta rodada. Traduzido em dois pontos: catch explícito no `createXxx()` de cada controller (a violação de constraint só aparece no *commit* da transação, não dentro do `try` do service) **e** um `@ExceptionHandler(DataIntegrityViolationException.class)` global em `GlobalExceptionHandler`, que cobre os caminhos sem try/catch local (ex: `PATCH /{nome}` renomeando pra um nome já existente) |
| 422 | ✅ Sim | `ResourceUnprocessableEntityException`. Dois motivos possíveis: (1) `tipo` do payload não corresponde ao endpoint chamado — checado em `AbstractHierarquicoService.salvar()`, vale para os 4 domínios (AQUAQE-214); (2) `vincularSuperiorSeInformado` com nível de superior errado (AQUAQE-22) — só existe em Estadual/Municipal/Regional, que têm unidade superior |
| 500 | ⚠️ Tecnicamente sim | Catch genérico (`Exception.class` → 500), mas forçar isso de propósito exigiria quebrar o servidor deliberadamente — não é um caso de uso real da API, não vale a pena automatizar |
| 504 | ❌ Não | Gateway Timeout é responsabilidade de um proxy/gateway na frente da aplicação — não existe gateway neste ambiente, nem handler no código |

Verifique sempre contra a aplicação real (`curl`/Postman) antes de escrever um teste negativo —
não confie cegamente no que o Swagger documenta.

## Pré-requisitos

```bash
python -m venv .venv
source .venv/bin/activate    # ou .venv\Scripts\activate no Windows
pip install -r requirements.txt
```

A aplicação precisa estar rodando (`../../mvnw spring-boot:run`, a partir da raiz do repo).
Por padrão a suíte aponta para `http://localhost:8080` — **nesta máquina de dev, a porta 8080
já está ocupada por um Jenkins local**, então rode com a porta real via variável de ambiente:

```bash
MSP_HOST_URL=http://localhost:8081 ./run_tests.sh      # bash/macOS/Linux
$env:MSP_HOST_URL="http://localhost:8081"; .\run_tests.ps1   # PowerShell
```

## Rodando

```bash
./run_tests.sh              # ou run_tests.ps1 no Windows — escreve em ./results/
./run_tests.sh --dryrun     # só checagem de sintaxe/import, sem chamada HTTP real
```
