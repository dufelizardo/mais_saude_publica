# Incidente de 2026-11-14: API fora nos quatro ambientes depois da v1.13.0

- **Gravidade:** alta. Sem login em dev, QA, homologação e produção.
- **Duração:** de 13/11, 15h33, até a v1.13.2 chegar a cada ambiente em 14/11 (dev às 14h45).
- **Issue:** [#489](https://github.com/dufelizardo/mais_saude_publica/issues/489).
- **Correções:** v1.13.1 (memória do backend) e v1.13.2 (Postgres sem `gosu`).
- **Acompanhamento:** a resiliência do deploy (R1, [ADR-0209](../adr/0209-resiliencia-do-deploy.md)), feita.

## Resumo

Depois da subida da **v1.13.0**, a API dos quatro ambientes (dev, QA, homologação e produção) parou de responder: o frontend abria, mas o **login e toda chamada à API davam 502**. Foram duas causas encadeadas, uma de memória e outra do Postgres; as duas foram corrigidas nas versões **1.13.1** e **1.13.2**.

## Linha do tempo (horário de Brasília)

| Quando | O que aconteceu |
|---|---|
| 13/11, 15h33 | v1.13.0 chega à `main` ([#481](https://github.com/dufelizardo/mais_saude_publica/pull/481)); os quatro backends passam a reiniciar em ciclo (`CrashLoopBackOff`, código 137). |
| 14/11, ~12h30 | O servidor reinicia; os quatro Postgres sobem travados. |
| 14/11, ~12h50 | Diagnóstico: o frontend responde 200, a API responde 502 nos quatro ambientes. |
| 14/11, 13h27 | Correção 1 (memória do backend) em `developer` (#482). |
| 14/11, 14h23 | v1.13.1 em produção (#483, #484, #485). |
| 14/11, 14h39 | Correção 2 (Postgres sem `gosu`) em `developer` (#486). |
| 14/11, 14h45 | `dev` volta a responder ao login. |
| 14/11 | v1.13.2 promovida para QA (#487), homologação (#488) e produção. |

## Causa 1: o backend não cabia mais na memória e na CPU do pod

- O backend rodava com **limite de 512Mi, meia CPU e `-Xmx384m`**.
- A v1.13.0 trouxe 29 ADRs e as migrações V65 a V90: a aplicação passou a ter **274 repositórios JPA e cerca de 320 tabelas**.
- **Medido com a imagem de produção e os mesmos limites:** pico de **496 MiB de 512** e subida de **340 s**, com a máquina livre.
- No servidor, com os quatro ambientes subindo juntos, o container era morto (código 137) antes de ficar pronto.
- O CI não pegou: os testes rodam sem os limites do Kubernetes.

**Correção (#482, v1.13.1):**
- limite de **768Mi** (pedido de 512Mi);
- `-Xmx448m`;
- até **1 CPU**;
- `startupProbe` de 10 para **15 minutos**.

Na mesma medição: pico de 519 MiB e subida em 166 s.

## Causa 2: o Postgres travava no `gosu` depois do reinício

- Depois do reinício do servidor, os quatro Postgres apareciam como **`Running` e `1/1`**, mas sem uma linha de log e sem aceitar conexão.
  - O `pg_isready` respondia `no response`.
  - A API recebia `Connection to postgres:5432 refused`.
- Dentro do container, o PID 1 era `gosu postgres docker-entrypoint.sh postgres`, com **23 minutos de CPU**. O `gosu`, que o entrypoint da imagem usa para trocar de root para o usuário `postgres`, estava em laço e nunca iniciou o banco.
- O manifest usava `postgres:16-alpine` sem verificação de prontidão. Por isso o Kubernetes mostrava o banco travado como pronto.

**Correção (#486, v1.13.2):**
- o Postgres roda direto como o usuário **postgres (uid 70)**, o dono dos dados, com `securityContext`. Assim o entrypoint não chama o `gosu`;
- `readinessProbe` com `pg_isready`.

Conferido localmente: num volume inicializado como root e depois subido com `--user 70:70`, o PID 1 é o próprio `postgres`, os dados são reaproveitados e ele aceita conexões. No `dev`, o login voltou 6 minutos depois do merge.

## Por que a versão anterior não segurou o ambiente

- O backend usa `RollingUpdate` com 1 réplica, mas a versão anterior não ficou de pé.
- Durante o ciclo de reinícios, o Deployment substituiu o ReplicaSet antigo pelo novo, que nunca ficava pronto.
- Depois do reinício do servidor, o Postgres travado derrubou qualquer versão.

## Alta disponibilidade: o que veio depois (R1, [ADR-0209](../adr/0209-resiliencia-do-deploy.md))

- [x] **Teste de orçamento de recursos no CI:** rodar a imagem com os mesmos limites do Kubernetes e reprovar o PR se a subida passar do tempo máximo ou a memória passar de 85% do limite. Teria pego a v1.13.0 antes da promoção.
- [x] **Memória ajustada pelo limite do pod** (`-XX:MaxRAMPercentage`) e `ExitOnOutOfMemoryError`.
- [x] **Subida mais rápida** com Class Data Sharing na imagem Java.
- [x] **Produção com 2 réplicas e autoescala (HPA)**, `PodDisruptionBudget` e estratégia `RollingUpdate` com `maxUnavailable: 0` explícita.
  - Pré-requisito já pronto no worktree da R1: trava das tarefas agendadas entre réplicas (`tb_trava_agendamento`, V91) e trava das rotinas de subida (`pg_advisory_lock`).
- [x] **Prioridade de produção** (`PriorityClass`): se faltar memória, dev e QA saem antes.
- [x] **Imagem do Postgres com versão fixa**, em vez da etiqueta móvel `16-alpine`.
- [x] **Aviso automático** quando um backend ou um Postgres ficar fora por mais de alguns minutos (issue no GitHub, com um token no servidor).
- [x] Flyway com `ignore-migration-patterns=*:future` explícito, para a versão anterior sempre poder voltar a subir com o banco já migrado (o padrão atual do Flyway já faz isso; a configuração documenta e protege contra mudança de padrão).

## Referências

- PRs: [#481](https://github.com/dufelizardo/mais_saude_publica/pull/481) (v1.13.0), #482 e #483 a #485 (v1.13.1), #486 e #487 a #488 (v1.13.2).
- ADR-0012: infraestrutura do home-lab, com a nota da revisão de memória.
- [ADR-0209](../adr/0209-resiliencia-do-deploy.md): resiliência do deploy (R1).
