# 0002 — Modelar a hierarquia de 4 níveis como uma única entidade autorreferenciada

## Status

Aceita (decisão já implementada — este ADR documenta retroativamente uma escolha que nunca havia sido registrada).

## Contexto

A ADR 0001 descreve a restauração de uma API REST de gestão hierárquica de unidades de saúde em 4 níveis (Zero, Um, Dois, Três), mas não documenta *como* essa hierarquia é modelada no banco de dados — só que existe em camadas controller/service/repository/DTO.

Ao inspecionar o código restaurado, a hierarquia **não** é modelada como 4 entidades JPA separadas (`HierarquicoZero`, `HierarquicoUm`, `HierarquicoDois`, `HierarquicoTres`), nem com um esquema de herança JPA (`@Inheritance`). Em vez disso:

- Existe **uma única entidade `@Entity`**: `UnidadeDeSaude` (tabela `TB_UNIDADE_DE_SAUDE`).
- Ela tem um campo autorreferenciado `unidadeSuperior` (`@ManyToOne(fetch = LAZY)`, FK `unidade_superior_id`), que aponta para outra linha da mesma tabela — é assim que a árvore de 4 níveis é representada.
- O "nível" de uma linha é determinado por um campo `tipo` (`int` solto), cujo valor é comparado com os ordinais do enum `TipoUnidadeDeSaude` (`ADMINISTRACAO1..4`, mais `UBS` e `HOSPITAL`) — mas o enum **não é usado como tipo da coluna** (ver pendência já registrada na ADR 0001).
- Os 4 "níveis" (Zero/Um/Dois/Três) existem apenas como um conceito da camada de serviço/controller: há 4 pares `Hierarquico{Zero,Um,Dois,Tres}Service`/`UnidadeDeSaudeHierarquico{Zero,Um,Dois,Tres}Controller`, todos operando sobre a mesma tabela `TB_UNIDADE_DE_SAUDE`, filtrando por `tipo`, e compartilhando CRUD comum via `AbstractHierarquicoService<RES>`.
- Campos específicos de nível ficam na mesma tabela, populados de forma esparsa pelo construtor correspondente: `regiao` só é preenchido pelo nível Dois; `municipio`/`estados` só pelo nível Um; Zero e Três deixam esses campos nulos.
- O nível Zero é a raiz da hierarquia (não tem `unidadeSuperior`); os níveis Um/Dois/Três recebem um `administracaoSuperior` (nome) no DTO de request, resolvido via `UnidadeDeSaudeRepository.findByNome` e vinculado ao campo `unidadeSuperior`.

## Decisão

Manter o modelo já implementado — uma única entidade `UnidadeDeSaude` autorreferenciada, com discriminador `tipo` e tabela compartilhada — e documentá-lo formalmente, em vez de migrar para 4 entidades/tabelas separadas ou para um esquema `@Inheritance` do JPA.

## Trade-offs considerados

**Entidade única autorreferenciada (escolhida, já implementada)**
- ✅ Uma tabela só, uma FK (`unidade_superior_id`) percorre a árvore inteira de 4 níveis sem precisar de UNION entre tabelas.
- ✅ CRUD comum aos 4 níveis fica centralizado em `AbstractHierarquicoService`, evitando duplicação de queries/mapeamento.
- ❌ Tabela esparsa: `regiao`, `municipio`, `estados` ficam `NULL` dependendo do nível, sem constraint de banco que documente essa regra.
- ❌ Nada no schema impede uma hierarquia inválida (ex.: uma unidade Zero apontando para outra Zero como `unidadeSuperior`, ou um Três apontando para outro Três) — a validação de "nível imediatamente superior correto" não existe hoje em nenhuma camada.
- ❌ O enum `TipoUnidadeDeSaude` existe mas não é o tipo real da coluna `tipo` (pendência já aberta na ADR 0001) — o discriminador de nível é um `int` sem garantia de que só valores 0-3 sejam usados nesse contexto (a mesma tabela reaproveitaria também `UBS`/`HOSPITAL`, ordinais 4 e 5, sem os 4 controllers de hierarquia tratarem esses casos).

**4 entidades/tabelas separadas (rejeitada, não foi a escolha implementada)**
- ✅ Cada nível teria seu próprio schema, sem colunas esparsas.
- ❌ Navegar a hierarquia completa exigiria JOINs entre 4 tabelas diferentes por FK explícita nível a nível, em vez de uma única auto-FK.
- ❌ Seria uma migração de schema disruptiva a partir do estado atual, sem ganho claro dado que o volume de dados e a regra de negócio (só 4 níveis fixos) não pedem crescimento do modelo.

**Herança JPA (`@Inheritance` `SINGLE_TABLE`/`JOINED`) (não avaliada explicitamente no código, mas relacionada)**
- ⚠️ Resolveria a tipagem do discriminador (JPA gerencia isso nativamente), mas ainda exigiria 4 subclasses e não foi o caminho seguido pelo código restaurado — não há evidência de que essa alternativa tenha sido considerada quando o código original foi escrito.

## Consequências

**Positivas**
- Schema simples (uma tabela, um enum de tipo, um self-join) e fácil de consultar para achar toda a árvore hierárquica a partir de qualquer nó.
- `AbstractHierarquicoService` reaproveita esse modelo único para eliminar duplicação entre os 4 services concretos (ver ADR 0001).

**Pendências herdadas / não resolvidas por este ADR**
- As 3 pendências da ADR 0001 (campo `tipo` como `int` solto, typos `ErrorExcepitionResponse`/`datautilexception`, bug HTTP 500 vs 404 em `administracaoSuperior` inexistente) continuam válidas e não são afetadas por este documento.
- Não existe validação de que `unidadeSuperior` aponte para o nível imediatamente superior correto (ex.: Três só pode ter Dois como superior) — isso é uma lacuna de integridade de dados não coberta nem pelo schema nem pela camada de serviço hoje.
