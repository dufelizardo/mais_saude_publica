# 0209 — Resiliência do deploy e alta disponibilidade (R1)

## Status

Aceita e implementada. Nasce do [incidente de 2026-11-14](../incidentes/2026-11-14-api-fora-depois-da-v1-13-0.md) (issue #489), em que
a API dos quatro ambientes ficou fora depois da v1.13.0. Complementa a [ADR-0012](./0012-infraestrutura-local-k3s-homelab.md)
(infraestrutura do home-lab) e a [ADR-0145](./0145-saude-da-aplicacao-para-o-kubernetes.md) (as verificações do Kubernetes).

## Contexto

- **O incidente teve duas causas.** O backend cresceu além do limite do pod (512Mi, meia CPU), e o CI não via isso. Depois de um
  reinício do servidor, o Postgres travou no `gosu`, com o Kubernetes mostrando o banco como pronto. As correções imediatas foram as
  v1.13.1 e v1.13.2.
- **O pedido do usuário:** "a aplicação tem que ser capaz de se autogerir para não ficar fora; isto tem que ser escalável".
- **O limite físico:** um servidor só (6,7 GiB, 4 CPUs, disco SSD), dividido entre os quatro ambientes e o projeto `lonewolf`. Dá para
  resistir a deploy ruim, a reinício e à queda de uma réplica; mais carga de verdade pede mais máquina.

## Decisão

1. **Uma versão ruim nunca tira a que está no ar.** A estratégia do backend é `RollingUpdate` explícita com `maxUnavailable: 0` e
   `maxSurge: 1`: a réplica antiga só sai quando a nova fica pronta. As migrações só acrescentam (remoção fica para a release
   seguinte). O Flyway tem `ignore-migration-patterns=*:future` explícito, para a versão anterior sempre subir com o banco já migrado
   por uma versão nova. O Flyway 11 já faz isso por padrão; a configuração documenta e protege contra uma mudança de padrão.
2. **O CI barra a imagem que não cabe (orçamento de recursos).** O job `orcamento-de-recursos` constrói a imagem de produção pelo
   mesmo Dockerfile e a sobe com os limites do Kubernetes, lidos dos próprios manifests: a memória e a CPU de
   `k8s/base/deployment-app.yaml` e o `JAVA_TOOL_OPTIONS` do overlay de produção. Reprova o PR se a API não ficar pronta em
   **7 minutos** ou se o pico de memória passar de **85% do limite**. O resumo do job mostra a subida e o pico. A promoção automática
   (`auto-merge`) espera também esse check.
3. **A memória acompanha o limite do pod.** `-XX:MaxRAMPercentage=58` em vez de `-Xmx` fixo (58% de 768Mi dá cerca de 446 MiB de
   heap): mudar o limite é mudar um número só. `-XX:+ExitOnOutOfMemoryError` faz a JVM sair, e o Kubernetes reiniciar, em vez de
   ficar viva e quebrada.
4. **Subida mais rápida com Class Data Sharing.** O Dockerfile extrai o jar e faz uma subida de treino sem banco
   (`spring.context.exit=onRefresh`, Flyway desligado, sem metadados da conexão); as classes carregadas ficam em `app.jsa` e a JVM de
   produção as reaproveita (`-Xshare:auto`: se o arquivo faltar, sobe sem ele, mais lenta, nunca quebrada). Medido com os limites de
   produção: **137 s** contra 166 s sem CDS.
5. **Produção com duas réplicas e autoescala.** `HorizontalPodAutoscaler` de 2 a 3 réplicas pela CPU (70%), com o `metrics-server`
   do k3s; o manifest de produção não fixa `replicas`, que passa a ser do autoscaler. `PodDisruptionBudget` com `minAvailable: 1`. O
   frontend de produção tem 2 réplicas. Requisitos que já estão no código:
   - **trava das tarefas agendadas entre réplicas** (`tb_trava_agendamento`, V91; `TravaDeAgendamento`): antes de rodar, a tarefa
     marca a própria linha até `agora + duração` por um UPDATE atômico; só uma réplica consegue, e a duração também evita a mesma
     tarefa rodar duas vezes quando os relógios diferem por segundos. Vale para os alertas da auditoria, a retenção e a
     reconciliação do responsável;
   - **trava das rotinas de subida** (`TravaDeArranque`, `pg_advisory_lock` numa conexão própria): o catálogo de acesso, o
     administrador inicial e os ajustes da Farmácia rodam uma réplica de cada vez. Eles já eram idempotentes; a trava evita lançar o
     mesmo ajuste duas vezes;
   - o login é por JWT, sem sessão no servidor; o volume de documentos é `ReadWriteOnce`, que serve às réplicas no mesmo nó.
6. **Produção sai por último.** `PriorityClass` `mais-saude-producao` na API, no banco, no frontend e no vigia de produção. Se o
   servidor ficar sem memória, o Kubernetes tira antes os pods de dev, QA e homologação.
7. **O Postgres não muda sozinho.** A imagem passa de `postgres:16-alpine` (etiqueta móvel) para `postgres:16.16-alpine3.24`, a mesma
   versão que a etiqueta trazia. Desde a v1.13.2 ele roda como o usuário postgres (sem `gosu`) e só fica pronto quando `pg_isready`
   responde.
8. **Aviso automático de queda (vigia).** Um CronJob de produção, de 5 em 5 minutos, lê pela API do Kubernetes os Deployments `app` e
   `postgres` dos quatro ambientes (uma conta de serviço com `get`/`list` de Deployments) e considera fora o que está com
   `Available=False` há mais de **10 minutos**.
   - Com o secret opcional `alerta-github` (chave `token`, um token do GitHub com permissão de issues no repositório), abre uma issue com
     as etiquetas `alerta` e `bug` e os comandos de diagnóstico; quando o serviço volta, comenta e fecha a issue.
   - Sem o secret, só escreve no log do Job.
   - O GitHub notifica o dono do repositório por e-mail e no aplicativo, sem depender do SMTP do sistema, ainda não escolhido.

## Alternativas descartadas

- **Mais réplicas em todos os ambientes:** não cabe no servidor (cada réplica pede 512Mi); dev, QA e homologação ficam com uma.
- **Argo Rollouts (canário com reversão automática):** pesado para o home-lab. A combinação de `maxUnavailable: 0`, readiness e o
  orçamento no CI cobre o caso que derrubou a v1.13.0.
- **ShedLock:** faria o mesmo, mas é uma dependência nova para três tarefas; a trava própria tem poucas linhas, usa a tabela
  e o banco que já existem e é testada junto com o resto.
- **Vertical Pod Autoscaler:** ajustaria a memória sozinho, mas reinicia pods para isso e exige componentes extras no cluster; o
  percentual de memória e o orçamento no CI resolvem o mesmo problema com controle.
- **Alerta por e-mail:** depende do SMTP ainda não escolhido; a issue no GitHub já notifica por e-mail.

## Consequências

- **Memória do servidor:** produção pede 2 × 512Mi para a API. Os quatro ambientes somam 2,5 GiB de pedido e 3,75 GiB de limite para as
  APIs, com cerca de 4,6 GiB disponíveis medidos no servidor.
- **O CI fica mais longo** em cerca de 10 minutos por PR (a construção da imagem), em paralelo ao Robot de tela.
- **Ação manual (opcional):** criar o secret `alerta-github` no namespace `mais-saude-prod` para o vigia abrir as issues; o passo está
  no [registro de incidentes](../incidentes/README.md).
- **Fora do escopo:** um segundo servidor (nó), que daria disponibilidade contra a queda da própria máquina; o desenho já está pronto
  para ele (réplicas, PDB, prioridade).

## Testes

- **JUnit `ResilienciaDoDeployTest`:**
  - a trava de agendamento deixa uma réplica só até vencer, e tarefas diferentes não se bloqueiam;
  - a trava de subida segura a outra réplica até ser solta;
  - a configuração do Flyway ignora a migração "do futuro" e valida o banco à frente.
- **JUnit existentes** da reconciliação do responsável e dos alertas da auditoria, com a trava no caminho.
- **CI `orcamento-de-recursos`:** a própria medida, em todo PR.
- **Medido localmente** com a imagem e os limites de produção: subida em 137 s, pico de 513 MiB de 768, login respondendo.
