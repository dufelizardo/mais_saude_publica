# 0001 — Restaurar e refatorar o código apagado, em vez de reescrever do zero

## Status

Aceita.

## Contexto

O commit `a6fa80b` ("Novo estudo") apagou todo o `src/main/java`, além de `README.md`, `LICENSE`, `CONTRIBUTING.md`, `SECURITY.md` e `Dockerfile`, e reescreveu o `pom.xml` (migração para Spring Boot 4.0.3). Esse commit já estava publicado em `origin/main` quando o problema foi identificado.

O histórico do git ainda tinha, no commit `7a6466e` (o último antes da exclusão), uma API REST funcional de gestão hierárquica de unidades de saúde (4 níveis — Zero/Um/Dois/Três —, em camadas controller/service/repository/DTO, com JPA, Bean Validation e documentação Swagger/OpenAPI), fruto de várias PRs mescladas.

Uma análise desse código, porém, revelou problemas reais, não só estilísticos:

- `HierarquicoTresService.buscarUnidadeDeSaudePorNome` filtrava por `TipoUnidadeDeSaude.ADMINISTRACAO2` em vez de `ADMINISTRACAO4` — bug de copy-paste que fazia updates no nível Três nunca encontrarem o registro.
- `GlobalExceptionHandler` só tratava `ResourceNotFoundException` e `Exception` genérica; `ResourceBadRequestException`, `ResourceUnauthorizedException` e `ResourceUnprocessableEntityException` caíam no handler genérico e retornavam HTTP 500 em vez de 400/401/422.
- Os 4 services (`Hierarquico{Zero,Um,Dois,Tres}Service`) eram ~95% código idêntico copiado entre si.
- Cada um dos 4 controllers tinha ~680 linhas, das quais ~90% eram blocos `@ApiResponse` (400/401/403/404/409/422/500/504) repetidos em cada um dos endpoints.

Diante disso, havia duas alternativas: reescrever o domínio do zero, ou restaurar o código de `7a6466e` e corrigir/refatorar os problemas encontrados.

## Decisão

Restaurar o código-fonte e os arquivos de projeto a partir do commit `7a6466e` (commit `f3c236a`), e então refatorar a duplicação estrutural encontrada (commit `1a80274`):

- Extração de `AbstractHierarquicoService<RES>`, com o CRUD comum aos 4 níveis; cada service concreto ficou reduzido a `getTipo()`, `toResponseDto()` e o `create()` específico.
- Extração de 3 anotações Swagger compostas (`ApiErrorResponsesListagem`, `ApiErrorResponsesBusca`, `ApiErrorResponsesMutacao`), que colapsam os blocos de `@ApiResponse` repetidos em cada endpoint × controller, sem alterar a documentação OpenAPI gerada.
- Correção do `GlobalExceptionHandler` para mapear `ResourceBadRequestException`/`ResourceUnauthorizedException`/`ResourceUnprocessableEntityException` aos status HTTP corretos.

## Trade-offs considerados

**Restaurar + refatorar (escolhida)**
- ✅ Menor esforço e menor risco: os problemas encontrados eram localizados (bugs de copy-paste, duplicação estrutural), não sintomas de um design fundamentalmente errado.
- ✅ Preserva o histórico de ~30 PRs mescladas documentando a evolução do domínio.
- ✅ Preserva a arquitetura em camadas já validada (hierarquia auto-referenciada em JPA, DTOs versionados, tratamento de exceções, documentação Swagger/OpenAPI) e o contrato de API existente (mesmos paths, mesmos payloads).
- ❌ Herda decisões de design questionáveis que não foram corrigidas nesta rodada por estarem fora do escopo do refactor então acordado (ver Consequências).

**Reescrever do zero (rejeitada)**
- ✅ Eliminaria toda a dívida técnica herdada de uma vez, com liberdade para escolher a tipagem/estrutura ideal desde o início.
- ❌ Descartaria o histórico de decisões já tomadas e validadas em produção conceitual (endpoints, contratos, exemplos de documentação Swagger já escritos).
- ❌ Custo e risco maiores (reimplementar 4 níveis hierárquicos, DTOs, exceptions, docs) sem garantia de que o resultado final teria menos bugs do que um refactor direcionado sobre uma base já testada manualmente.

A escolha por restaurar + refatorar se apoiou no diagnóstico de que o problema era duplicação e bugs pontuais, corrigíveis com um refactor de escopo conhecido — não uma falha de arquitetura que justificasse descartar o trabalho anterior.

## Consequências

**Positivas**
- 3 bugs reais corrigidos (filtro `ADMINISTRACAO2`→`ADMINISTRACAO4`, mapeamento de exceptions para status HTTP, checagem morta `if (nome.isEmpty())` removida).
- ~2400 linhas de duplicação eliminadas entre services e controllers.
- Contratos de API e documentação OpenAPI preservados byte-a-byte por endpoint (validado via `/v3/api-docs` antes e depois do refactor).

**Pendências aceitas conscientemente (fora de escopo desta rodada)**
- O campo `tipo` continua `int` solto nas entidades/DTOs, em vez de usar o enum `TipoUnidadeDeSaude` já existente — mudaria o contrato de API, não é puramente estrutural.
- Nomes com erro de digitação não foram corrigidos (`ErrorExcepitionResponse`, pacote `datautilexception`) — deixados para uma limpeza futura separada.
- Um bug pré-existente permanece: ao criar uma unidade Um/Dois/Três referenciando uma `administracaoSuperior` inexistente, a `ResourceNotFoundException` lançada é capturada pelo `catch (Exception e)` genérico no `create()` do controller e retorna HTTP 500 em vez de 404. Esse comportamento já existia no código original (`7a6466e`) e não foi alterado, por estar fora do escopo acordado para este refactor.
