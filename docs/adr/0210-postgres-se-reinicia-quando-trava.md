# 0210 — O Postgres se reinicia sozinho quando trava

## Status

Aceita e implementada. Nasce do [incidente de 2026-11-17](../incidentes/2026-11-17-banco-do-dev-travado-em-recuperacao.md) e complementa a
[ADR-0209](./0209-resiliencia-do-deploy.md) (resiliência do deploy).

## Contexto

- O Postgres do dev ficou **3 dias** em "recovery mode", embora o último desligamento tenha sido limpo (14/11, 21:34). A API não
  conectava e entrou em `CrashLoopBackOff` (mais de 800 reinícios), e o login devolvia 502.
- O banco tinha só `readinessProbe` (ADR-0209): o Kubernetes o marcava como não pronto, mas não o reiniciava. Um
  `rollout restart` resolveu em segundos.
- É a segunda trava do processo do Postgres sem erro no log. A primeira foi a do `gosu` em 14/11. A causa no servidor ainda está
  em investigação. Reproduzido localmente com a mesma imagem e os mesmos limites (256 MiB e ¼ de CPU), o banco subiu normalmente
  com as 91 migrações, com pico de 141 MiB. Os limites e a versão fixa da imagem não explicam a trava.

## Decisão

O Deployment do Postgres ganha duas verificações com `pg_isready`, a mesma da prontidão:

- **`startupProbe`** de até 15 minutos (90 × 10 s), para uma recuperação de verdade (depois de queda de energia, por exemplo) não
  ser interrompida;
- **`livenessProbe`** que reinicia o container depois de 2 minutos sem aceitar conexão (6 × 20 s).

Os dados ficam no volume: reiniciar o container não perde nada, e o Postgres faz a recuperação normal na subida.

## Alternativas descartadas

- **Só o vigia avisar:** o aviso chega, mas alguém ainda precisa reiniciar o banco à mão; em dev, ninguém olhou por 3 dias.
- **Liveness por consulta SQL (`select 1`):** precisaria da senha na verificação; o `pg_isready` já distingue banco aceitando
  conexão de banco em recuperação ou travado.
- **Janela de vida curta (menos de 1 minuto):** um pico de CPU no servidor, com o banco a ¼ de CPU, poderia reiniciar um banco
  saudável.

## Consequências

- Um banco travado volta sozinho em cerca de 2 minutos, nos quatro ambientes.
- Uma recuperação que passe de 15 minutos seria interrompida e recomeçaria em laço. Com o volume atual (dezenas de MB de WAL),
  isso não acontece. Se acontecer, aumentar o `failureThreshold` do `startupProbe`.

## Testes

- **JUnit `ManifestoPostgresTest`:** o manifesto tem `startupProbe` e `livenessProbe` com `pg_isready`, subida de pelo menos 10
  minutos e de 1 a 5 minutos travado até reiniciar. O teste falha sem a correção.
