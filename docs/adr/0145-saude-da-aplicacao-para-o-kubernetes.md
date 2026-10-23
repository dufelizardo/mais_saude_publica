# 0145 — Saúde da aplicação para as verificações do Kubernetes

## Status

Aceita e implementada. Revê a escolha do `/v3/api-docs` como verificação de saúde dos manifestos
([ADR-0012](./0012-infraestrutura-local-k3s-homelab.md)) e da espera do CI.

## Contexto

- **Em 2026-10-23, a API de dev entrou em ciclo de reinício:** 54 reinícios em 5h30, e a versão anterior com 78.
  - O app subia sem erro, em cerca de 230 segundos.
  - O Kubernetes o matava (código 137) porque a verificação de subida falhava: "Startup probe failed: `/v3/api-docs`
    … Client.Timeout exceeded", 751 vezes.
- **As três verificações** (startup, readiness e liveness) chamavam o `/v3/api-docs`, que monta o Swagger da API
  inteira. Com mais de 100 controllers e meia CPU, a primeira geração levou **86 segundos**. A verificação espera 1
  segundo por padrão.
- **Ninguém quebrou nada de uma vez:** o documento cresceu a cada fatia até passar do tempo. QA, homologação e produção
  usam a mesma base de manifestos e teriam o mesmo problema na próxima promoção.
- **O CI também esperava o app subir pelo `/v3/api-docs`.**

## Decisão

1. **Spring Boot Actuator, só com o health:**
   - `management.endpoints.web.exposure.include=health`, sem detalhes (`show-details=never`);
   - os grupos de vivacidade (`/actuator/health/liveness`) e de prontidão (`/actuator/health/readiness`, que inclui o
     banco) ligados;
   - o restante do Actuator não é exposto.
2. **Segurança:** `/actuator/health` e `/actuator/health/**` liberados sem login. Só devolvem o estado (`UP` ou
   `DOWN`), sem componente, versão ou dado. Os interceptadores de autorização e auditoria seguem só em `/api/**`.
3. **Manifesto da API** (`k8s/base/deployment-app.yaml`):
   - **subida:** `liveness`, com até 10 minutos (120 × 5 s, como antes);
   - **prontidão:** `readiness`;
   - **vivacidade:** `liveness`, a cada 20 s com três falhas;
   - todas com `timeoutSeconds: 5`.
4. **CI:** as esperas do `pipeline.yml` e do `scheduled-tests.yml` usam o `/actuator/health`.
5. **Log:** `spring.jpa.show-sql=false` no perfil `prod`, o dos quatro ambientes do home-lab. Escrever cada SQL pesava
   no servidor e poluía o log; o perfil local e o de teste seguem como estão.

## Consequências

- **A verificação de saúde passa a medir o que importa:** o app responde, e a prontidão confirma que o banco está de
  pé. O Swagger deixa de ser gerado a cada verificação.
- **O Swagger continua lento na primeira chamada** (cerca de 86 s no home-lab). Isso afeta quem abre o
  `/swagger-ui.html`, não a disponibilidade. Agrupar a documentação ou gerá-la no build fica para quando fizer falta.
- **A subida de 230 segundos continua.** A tolerância de 10 minutos cobre o servidor ocupado; reduzir o tempo de subida
  é outro assunto.

## Testes

- **JUnit:** `SaudeActuatorTest`, com segurança ligada. Health, liveness e readiness respondem sem token, só com o
  estado; env, beans e info não respondem.
- **Robot:** `test/saude/SaudeDaAplicacao.robot`, com os mesmos casos contra a aplicação no ar.
