# 0090 — Migrações versionadas com Flyway

## Status

Aceita e implementada.
- Cumpre o que as ADRs [0007](./0007-deploy-docker.md) e [0060](./0060-uma-remessa-um-lote-por-unidade.md)
  previam.
- Substitui a rotina provisória da [ADR-0080](./0080-restricoes-de-enum-atualizadas-na-subida.md).

## Contexto

- O esquema era mantido pelo Hibernate com `ddl-auto=update` em todos os ambientes. O `update`:
  - acrescenta tabelas e colunas, mas não remove nem altera nada;
  - não refaz as restrições CHECK das colunas de enum;
  - não deixa registro do que mudou nem de quando.
- A ADR-0080 remediava a restrição de enum na subida, com uma rotina que comparava o banco com os enums
  do código. Ela mesma se declarava provisória até haver migrações.
- Cada domínio novo (a Regulação, o Laboratório, a agenda) cria tabelas e enums. Antes de haver dado real
  em produção, trocar o mecanismo é barato; depois, não.
- Os quatro ambientes estão em versões diferentes: `dev` na `developer`; `qaa`, `homologacao` e
  `prod` na v1.4.0. Todos têm banco criado pelo `ddl-auto`.

## Decisão

1. **Flyway** (`spring-boot-starter-flyway` e `flyway-database-postgresql`), com as migrações em
   `src/main/resources/db/migration`.
2. **V1 é o esquema da v1.4.0**, gerado pelo próprio Hibernate da v1.4.0 num banco vazio e extraído com
   `pg_dump --schema-only`. Não se edita.
3. **V2 traz o que mudou entre a v1.4.0 e a entrada do Flyway:** a ação `EXPORTACAO` na auditoria e as três
   tabelas da Regulação. É idempotente (`CREATE TABLE IF NOT EXISTS` e chave estrangeira só se não
   existir), porque o `dev` já tinha essas tabelas.
4. **Banco que já existia é marcado na V1 sem rodar nada** (`spring.flyway.baseline-on-migrate=true`,
   `baseline-version=1`) e recebe só as migrações seguintes. Banco vazio (CI, ambiente novo) roda desde a
   V1.
5. **O Hibernate não altera mais o banco:**
   - **Ambientes e uso local:** `ddl-auto=none`.
   - **Perfil de teste:** `ddl-auto=validate`. JUnit e os jobs de Robot do CI sobem contra o esquema
     migrado, então tabela ou coluna esquecida numa migração derruba o CI, e não a produção.
6. **A rotina da ADR-0080 sai.** Em seu lugar, o teste `MigracoesDoEsquemaTest` confere duas coisas:
   - a migração chegou à última versão;
   - cada restrição CHECK de enum tem **exatamente** os valores de algum enum do código. Valor novo num
     enum sem a migração que amplia a restrição falha no CI.
7. **Rotinas de dados na subida continuam** (catálogo de acesso, saldo inicial da farmácia, incorporação de
   lotes duplicados), porque tratam dado, não esquema. O índice único de lote da ADR-0060 já está na V1,
   e a rotina o cria só se faltar.

## Como mudar o esquema daqui em diante

1. Alterar a entidade.
2. Criar `V<n>__<descricao_curta>.sql` com o mesmo efeito: tabela, coluna, índice, chave e restrição.
   - Para enum, refazer a restrição: `DROP CONSTRAINT IF EXISTS` e `ADD CONSTRAINT ... CHECK (... ANY
     (ARRAY[...]))` com todos os valores.
   - Os nomes que o Hibernate daria saem de `ddl-auto=create` num banco descartável, quando ajudar.
3. Rodar os testes. `validate` e o `MigracoesDoEsquemaTest` apontam o que faltou.
4. Migração aplicada não se edita. Correção é uma migração nova.

## Consequências

- Cada mudança de esquema fica versionada, revisada no PR e registrada em `flyway_schema_history` em
  cada banco.
- Remover ou renomear coluna passa a ser possível, com migração explícita.
- Na próxima subida de cada ambiente, o Flyway cria a tabela de histórico, marca a V1 e aplica a V2:
  - em `qaa`, `homologacao` e `prod`, cria as tabelas da Regulação e amplia a restrição da auditoria;
  - no `dev`, a V2 passa sem efeito, porque tudo já existia.
- Mudar entidade sem migração não quebra o ambiente na subida, porque lá não há validação. A rede de
  proteção é o CI.

## Testes

- `MigracoesDoEsquemaTest` (JUnit), conferido localmente em três bancos:
  - **vazio:** roda V1 e V2;
  - **criado pela v1.4.0:** marca V1 e aplica V2;
  - **criado pela `developer` antes do Flyway:** marca V1, e a V2 passa sem recriar nada.
- A suíte JUnit inteira e os jobs de Robot do CI passam a subir com `validate` contra o esquema migrado.
