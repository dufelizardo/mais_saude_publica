# 0080 — Restrições de enum atualizadas na subida

## Status

Aceita e implementada. Corrige uma perda silenciosa de eventos de auditoria nos ambientes com banco anterior.

## Contexto

O esquema é mantido pelo Hibernate com `ddl-auto=update` (ADR-0005). Ao criar uma tabela, o Hibernate
gera, para cada coluna de enum gravada como texto, uma restrição `CHECK (coluna IN (...))` com os valores do
enum **naquele momento**. O `update` acrescenta colunas e tabelas, mas **nunca refaz essas restrições**.

Quando um enum ganha um valor novo, gravá-lo num banco que já existia falha. Na trilha de auditoria, a
gravação usa `registrarSemFalhar` de propósito (ADR-0070), então **o evento some sem erro visível**. Foi o
que aconteceu com `ACESSO_JUSTIFICADO` (ADR-0076): nos bancos criados antes dele, os acessos justificados
não entravam na trilha.

O CI não pega isso, porque cria o banco do zero a cada execução. O problema só aparece em ambiente com
banco anterior.

## Decisão

1. **Uma rotina de subida, `AtualizacaoRestricoesDeEnum`, roda antes de todas as outras.** Ela lê do
   Postgres as restrições de enum (`coluna = ANY (ARRAY[...])`) e compara cada uma com os enums do código.
2. **Se exatamente um enum contém todos os valores antigos e tem valores a mais,** a restrição é refeita
   com a lista atual, com o mesmo nome.
3. **Só amplia.** Valor removido ou renomeado no código, ou restrição que serviria para mais de um enum,
   fica como está e vai para o log ("conferir à mão").
4. **É idempotente** e não faz nada fora do Postgres.

## Trade-offs considerados

**Ampliar na subida (escolhida)** × **apagar todas as restrições de enum** × **migrações versionadas
(Flyway)**
- ✅ Mantém a proteção do banco contra valor inválido, inclusive para SQL manual.
- ✅ Não exige adotar uma ferramenta de migração agora. A troca do `ddl-auto` por migrações versionadas
  continua sendo a recomendação antes de produção real (ADR-0005), e esta rotina sai junto.
- ❌ É heurística (casa a restrição com o enum pelos valores). Os casos ambíguos ficam no log em vez de
  serem adivinhados.

## Consequências

- Os ambientes com banco anterior passam a gravar `ACESSO_JUSTIFICADO` na trilha, e os enums que ganharem
  valores daqui em diante não perdem mais dados.
- **Os eventos de acesso justificado perdidos antes desta correção não voltam.** Os registros em
  `TB_ACESSO_JUSTIFICADO` (motivo, texto, quem e quando) continuam íntegros, porque essa tabela é nova e
  nasceu com a restrição certa.

## Testes

`AtualizacaoRestricoesDeEnumTest` simula o banco antigo: restringe a coluna `acao` sem
`ACESSO_JUSTIFICADO` e confere que a gravação falha. Depois roda a rotina, grava com sucesso e roda de
novo para confirmar que nada muda.
