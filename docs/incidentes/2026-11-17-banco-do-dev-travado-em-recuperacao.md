# 2026-11-17 — Banco do dev travado em recuperação

## Resumo

- **Ambiente:** dev (`maissaudepublica-dev.local`). QA, homologação e produção não foram afetados.
- **Sintoma:** login com 502; a API em `CrashLoopBackOff` (mais de 800 reinícios) com `Connection to postgres:5432 refused`.
- **Duração:** cerca de 3 dias (de 14/11 à noite a 17/11, 23:39 UTC), percebido ao conferir o dev antes de promover a 1.14.0.
- **Correção imediata:** `sudo kubectl -n mais-saude-dev rollout restart deploy/postgres`.
- **Correção definitiva:** o Postgres se reinicia sozinho quando trava ([ADR-0210](../adr/0210-postgres-se-reinicia-quando-trava.md)).

## O que aconteceu

O pod do Postgres estava `Running`, mas não pronto, desde a troca para a imagem de versão fixa (R1). O log repetia
`FATAL: the database system is in recovery mode` a cada verificação de prontidão. Depois do reinício, o novo pod mostrou que o
último desligamento tinha sido limpo (`database system was shut down at 2026-11-14 21:34:30 UTC`) e ficou pronto em menos de um
segundo. Uma recuperação desse tamanho leva milissegundos: o processo de subida do banco antigo travou.

Depois do reinício, o banco seguiu avisando `autovacuum worker took too long to start; canceled` de minuto em minuto: o servidor
ainda tem dificuldade para iniciar os processos do Postgres.

## Causa

- **Por que ficou fora 3 dias:** o banco só tinha verificação de prontidão. O Kubernetes o marcava como não pronto, mas não o
  reiniciava.
- **Por que travou:** em investigação. É a segunda trava do processo do Postgres sem erro no log, depois do `gosu` em
  [2026-11-14](./2026-11-14-api-fora-depois-da-v1-13-0.md).
- **O que foi descartado:** os limites do pod e a versão da imagem. A mesma imagem, com os mesmos limites (256 MiB e ¼ de CPU), subiu
  localmente com as 91 migrações, sem pressão de memória e com pico de 141 MiB.

## Acompanhamento

- [x] Startup e liveness no Postgres ([ADR-0210](../adr/0210-postgres-se-reinicia-quando-trava.md)).
- [ ] Causa no servidor: processos do Postgres, `cpu.stat` e `memory.events` do container, kernel e versão do k3s.
- [ ] Criar o secret `alerta-github` para o vigia avisar por issue (ver o [registro de incidentes](./README.md)).
