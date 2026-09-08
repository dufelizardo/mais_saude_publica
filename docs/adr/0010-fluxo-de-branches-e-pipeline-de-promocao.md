# 0010 — Fluxo de branches (developer → qa → cert → main) com pipeline de gate no GitHub Actions

## Status

Aceita — implementada.

## Contexto

Até aqui o repositório só tinha `main` (prod) de fato ativo, mais uma branch remota `developer`
obsoleta (código anterior à restauração do projeto — ver ADR-0001). Não havia `qa` nem `cert`, e
qualquer alteração ia direto para `main` sem nenhum gate automatizado real.

A pipeline então existente (`.github/workflows/main.yml`) estava quebrada: as últimas execuções
falhavam em ~22s porque os testes JUnit (`@ActiveProfiles("test")`) esperavam um banco de dados
no runner do Actions, que não existia — nenhum serviço de banco estava configurado no workflow.
Além disso, o passo de deploy pro Render disparava tanto em `push` quanto em `pull_request` pra
`main`, ou seja, um PR ainda não mergeado já acionava deploy de produção.

Também foi identificado, ao investigar a troca de banco (ver Decisão), que `pom.xml` não tinha a
dependência `org.postgresql:postgresql`, apesar de `application-prod.properties` já referenciar
`org.postgresql.Driver` — o deploy de produção no Render provavelmente já falhava em runtime por
driver ausente.

Existe uma ADR anterior (0007) que desenha uma visão bem mais ampla de deploy (Postgres + Redis +
Flyway + `ghcr.io` + deploy via SSH, com ambientes hospedados persistentes) — ainda não
implementada. Esta ADR-0010 é o subconjunto prático adotado agora, sem essa infraestrutura extra.

## Decisão

Adotar um fluxo de promoção linear entre 4 branches — `developer` → `qa` → `cert` → `main` (prod)
— onde cada promoção só é permitida se uma pipeline automatizada validar o código antes:

- **`developer`**: branch de trabalho livre, sem proteção. Todo `push` roda o gate rápido
  (`ci.yml`): build + suíte JUnit.
- **PR para `qa`, `cert` ou `main`**: além do gate rápido, roda o gate de aceitação
  (`acceptance.yml`) — builda a imagem Docker real do projeto (`Dockerfile` existente), sobe ela
  junto de um PostgreSQL num container efêmero (dura só o job, destruído ao final) e roda a suíte
  de aceitação Robot Framework (`test/robot/`, 118 casos) contra a aplicação de pé. GitHub Actions
  é CI, não hospedagem — por isso QA e Certificação não ganham ambiente persistente com URL fixa
  nesta rodada; a validação acontece só durante o job.
- **Proteção de branch** em `qa`, `cert` e `main`: exige os checks `test` (ci.yml) e
  `robot-acceptance` (acceptance.yml) passando, e força fluxo de PR (sem push direto) — sem exigir
  aprovação humana de revisor, e valendo até para o dono do repositório (`enforce_admins`).
- **Deploy real** (`deploy-prod.yml`, Render) só dispara em `push` (merge) na `main` — nunca em
  `pull_request`, corrigindo o bug de deploy prematuro.
- **Troca completa de MySQL para PostgreSQL** em todos os ambientes (dev local, JUnit, CI) — não
  só no gate de aceitação. Motivo: `application-prod.properties` já usava Postgres; manter MySQL
  em dev/test só pra produção usar outro motor era a causa raiz do bug de dialect conflitante já
  registrado na AQUAQE-217, e exigia manter dois motores de banco no projeto sem necessidade real.
  `mysql-connector-j` saiu do `pom.xml`, substituído por `org.postgresql:postgresql` — o que também
  corrige o driver ausente do deploy de produção.

## Trade-offs considerados

**Gate efêmero no próprio Actions vs. ambientes hospedados persistentes para QA/Cert**
- ✅ Escolhida: gate efêmero. Não exige provisionar/pagar por infraestrutura nova, não exige
  credenciais de um provedor de hospedagem além do que já existe (Render, só para prod), e ainda
  assim valida a aplicação real (imagem Docker de produção) contra um banco real antes de permitir
  a promoção.
- ❌ Rejeitada (por ora): ambientes hospedados persistentes pra QA e Cert (ex: mais serviços no
  Render). Traria URL fixa navegável a qualquer momento (útil pra QA manual/demonstração), mas
  exige provisionamento manual fora do GitHub (o agente que implementou isso não tem acesso à
  conta Render do usuário) e mais secrets. Fica para quando a ADR-0007 for implementada de fato.

**Troca completa para PostgreSQL vs. manter MySQL em dev/test e Postgres só em prod**
- ✅ Escolhida: troca completa. Um único motor de banco em todos os ambientes elimina de vez a
  classe de bug da AQUAQE-217 (dialect/config divergente entre perfis) e simplifica o gate de CI
  (um único tipo de serviço de banco a manter nos workflows).
- ❌ Rejeitada: manter MySQL em dev/test. Exigiria continuar mantendo dois motores de banco
  suportados no projeto (duas dependências JDBC, duas configurações) só para não mudar o hábito de
  desenvolvimento local — sem benefício real, já que prod nunca usou MySQL.

**Exigir aprovação de PR além dos checks automatizados**
- ❌ Rejeitada: o repositório tem um único desenvolvedor ativo hoje; exigir um aprovador humano
  formal não agrega segurança real neste estágio e forçaria um fluxo artificial (auto-aprovação
  por outra conta, ou bypass administrativo). Os checks automatizados (`ci.yml` + `acceptance.yml`)
  são o controle de qualidade real aplicado.

## Atualização — pipeline em um workflow só (sequencial) + auto-merge

`ci.yml` e `acceptance.yml` eram dois workflows separados, disparados juntos pelo mesmo PR e
rodando em paralelo — o estágio caro (build de imagem Docker + suíte Robot Framework) rodava
mesmo quando o JUnit já tinha falhado rápido. Foram unificados em `pipeline.yml`: dois jobs no
mesmo workflow, com `robot-acceptance` declarando `needs: test` — só começa depois do JUnit
passar, e a aba Actions passa a mostrar os dois estágios em sequência (um grafo em linha), não
dois workflows soltos. Os nomes dos jobs (`test`, `robot-acceptance`) foram mantidos, então a
proteção de branch já configurada continua válida sem nenhuma mudança.

Também foi ligado `allow_auto_merge` no repositório: um PR de promoção pode usar
`gh pr merge --auto` para mergear sozinho assim que os checks obrigatórios passarem, sem alguém
precisar clicar em "Merge". A abertura do PR em si continua manual, de propósito — é o ponto onde
alguém decide "quero promover agora"; automatizar isso também faria a promoção acontecer sozinha a
cada commit, o que não é o comportamento desejado aqui. Depois foi criado `auto-merge.yml`,
automatizando também esse "ligar o auto-merge" nas 3 promoções reais, restrito a elas.

## Atualização — branches renomeadas (`qa`→`qaa`, `cert`→`homologacao`)

As branches `qa` e `cert` foram renomeadas para `qaa` e `homologacao`, pra bater com o vocabulário
que o workflow de status do projeto no Jira (AQUAQE) já usava ("QAA", "HOMOLOGACAO" — ver
ADR-0011). O restante do texto desta ADR usa os nomes originais (`qa`/`cert`) como registro
histórico da decisão como foi tomada; onde aparecem hoje no repositório real, os nomes são
`qaa`/`homologacao`. Renomeado via o endpoint oficial de rename do GitHub, que migrou automático
os PRs abertos e a proteção de branch clássica; o Ruleset de aprovação pra terceiros (também da
ADR-0011) precisou de atualização manual das suas condições, já que Rulesets não são migrados
automaticamente no rename.

## Consequências

**Positivas**
- Fica impossível promover código quebrado (`push`/merge bloqueado por branch protection) sem que
  build, testes JUnit e a suíte de aceitação Robot Framework tenham passado — endereça diretamente
  o requisito de "não promover se houver problema".
- Corrige 2 bugs reais herdados da pipeline anterior: ausência de banco de dados no runner (testes
  sempre falhavam) e deploy disparando em PR não mergeado.
- Corrige o driver JDBC ausente que provavelmente já quebrava o deploy de produção no Render.
- `main` foi atualizada (fast-forward) para incluir 19 commits que já existiam em
  `restore-and-refactor` mas nunca tinham chegado a `main` (suíte Robot completa, correções
  AQUAQE-214/215/216/217, config de VS Code) — sem isso, as novas branches nasceriam desatualizadas.

**Negativas / pendências**
- QA e Certificação não têm URL navegável fora do momento do CI — só validam automaticamente, não
  servem para demonstração manual ou testes exploratórios ad-hoc. Se isso vier a ser necessário,
  requer revisitar a ADR-0007 (ambientes hospedados persistentes).
- Quem desenvolve localmente fora do Docker agora precisa de PostgreSQL instalado (antes era
  MySQL) — mudança de hábito de ambiente de desenvolvimento.
- `enforce_admins=true` nas branches protegidas significa que nem o dono do repositório pode
  fazer push direto ou pular os checks em uma emergência sem antes remover a proteção manualmente.
