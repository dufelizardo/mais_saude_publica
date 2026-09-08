# 0011 — Backlog de melhorias de branching/pipeline (Fase 2)

## Status

Proposta — backlog, nada aqui foi implementado ainda.

## Contexto

A Fase 1 (branches `developer → qa → cert → main`, gate `test → analyze → robot-acceptance`,
auto-merge automático, segurança nativa do GitHub ligada) está implementada e documentada na
[ADR-0010](0010-fluxo-de-branches-e-pipeline-de-promocao.md). Durante a última promoção real por
esse fluxo, foi descoberto que merges feitos com o token padrão da GitHub Action (`github.token`,
usado pelo `auto-merge.yml`) não disparam outros workflows via `push` — proteção do próprio
GitHub contra loop infinito. Isso quebrou silenciosamente o deploy automático pra produção; foi
contornado com um `workflow_dispatch` manual em `deploy-prod.yml`, mas a correção definitiva (um
token de acesso pessoal dedicado) ficou combinada para uma rodada futura.

Nessa mesma conversa, o usuário trouxe um documento de referência ("Estratégia de Branching e
Fluxo de Promoção v2.1") com convenções de um fluxo mais amplo, orientado a times maiores:
branches `feature/*`/`fix/*`/`hotfix/*`, aprovações humanas obrigatórias por estágio de promoção,
isenção de aprovação para um "Tech Lead", calendário fixo de release semanal, testes de
performance, template de Pull Request e um procedimento de resposta a incidentes por severidade
(P1–P4).

Pedido explícito do usuário: incorporar essas sugestões ao backlog de melhorias **sem alterar** o
que já foi construído e validado na Fase 1. Isso não é automático — vários pontos do documento de
referência **contradizem decisões já tomadas e em produção** (0 aprovações obrigatórias em
`qa`/`cert`/`main`, auto-merge total, `enforce_admins: true` sem nenhum bypass). Esta ADR separa o
que é puramente aditivo (não exige decidir nada, pode virar tarefa a qualquer momento) do que
exige uma resposta explícita antes de qualquer mudança de código — para nunca implementar
silenciosamente algo que desfaça o que já está no ar.

## Decisão

Registrar o backlog abaixo como referência para priorização futura. Nenhum item foi implementado
nesta rodada; nenhum workflow, configuração de branch protection ou arquivo de build foi alterado.

### Grupo A — aditivo, sem conflito com o que existe hoje

Pode virar tarefa a qualquer momento, isoladamente, sem exigir nenhuma decisão prévia:

1. **PAT dedicado para `auto-merge.yml`** — trocar `github.token` por um Personal Access Token
   salvo como secret do repositório, para que merges automáticos voltem a disparar
   `deploy-prod.yml` via `push` normal, sem depender do `workflow_dispatch` manual. Achado desta
   sessão, é o item mais concreto e com maior valor imediato do grupo.
2. **Convenção local `feature/*` e `fix/*`**, partindo sempre de `developer` — organiza o trabalho
   individual *antes* de chegar em `developer`; não muda nada no gate automatizado de
   `qa`/`cert`/`main`, que continua operando exatamente como hoje.
3. **Template de Pull Request** (`.github/pull_request_template.md`) — estrutura a abertura manual
   do PR (descrição, testes realizados, breaking changes, checklist), sem introduzir nenhuma
   aprovação obrigatória — só preenche o campo de descrição com uma estrutura consistente.
4. **Validação de nome de branch na CI** — um step que rejeita nomes fora do padrão (`feature/`,
   `fix/`, `hotfix/`, `developer`, `qa`, `cert`, `main`). Só faz sentido depois do item 2 existir
   de fato.
5. **Testes de performance** como estágio adicional futuro (ferramenta ainda não escolhida —
   JMeter, Gatling ou k6 seriam as opções mais óbvias) — hoje não existe nenhuma cobertura de
   performance no projeto.
6. **CodeQL com threshold de severidade** — refinar o estágio `analyze` (já obrigatório desde a
   Fase 1) para só bloquear a promoção acima de uma severidade configurável, em vez de qualquer
   achado.
7. **Procedimento de incidentes (P1–P4)** como guia operacional documentado — não mexe no
   pipeline, é só uma referência de "o que fazer quando" para consulta futura.

### Grupo B — contradiz decisão já tomada, precisa de resposta explícita antes de virar tarefa

1. **Aprovações humanas obrigatórias — só para PRs de terceiros.** Clarificado pelo usuário: a
   exigência de aprovação vale apenas para Pull Requests abertos por colaboradores externos; o
   Tech Lead/dono do repositório continua exatamente como hoje (0 aprovações obrigatórias,
   auto-merge automático via `auto-merge.yml`) — não é uma mudança de comportamento pra ele, só
   uma trava adicional para quem não é o autor original. Isso não é mais uma pergunta em aberto de
   *política*, mas ainda é um problema de *mecanismo*: a proteção de branch **clássica** em uso
   hoje aplica `required_approving_review_count` igual pra qualquer autor de PR, sem distinguir
   "é o dono do repo" de "é um terceiro". Pra implementar exatamente o que foi pedido, sem afetar
   o autor original, é necessário migrar de branch protection clássica para **Rulesets** (recurso
   mais novo do GitHub) com uma *bypass list* contendo o dono do repositório — aí sim, um terceiro
   precisa de aprovação e o dono continua passando direto pelo `auto-merge.yml` como hoje. Migrar
   pra Rulesets é uma mudança de mecanismo não-trivial (a proteção atual de `qa`/`cert`/`main`
   precisaria ser recriada nesse novo modelo) — por isso continua no Grupo B, não no A.
2. **Nome de branch `qaa`**, usado no documento de referência, não bate com a `qa` já criada, em
   uso e com histórico real de promoções mergeadas. Muito provavelmente é um erro de digitação no
   documento original — **precisa de confirmação explícita** antes de renomear qualquer coisa
   (renomear uma branch remota com PRs e branch protection já associados tem custo real).
3. **Hotfix com merge direto para `main`.** Hoje `enforce_admins: true` bloqueia até push direto
   do dono do repositório — de propósito, para que ninguém pule os checks obrigatórios, nem em
   emergência. Um fluxo de hotfix de verdade precisa de um desenho cuidadoso (por exemplo, uma
   exceção via Rulesets condicionada a um label específico, não desligar `enforce_admins` por
   completo) — não é trivial, e merece cautela redobrada por se tratar de um sistema com dados de
   saúde pública.
4. **Calendário fixo de release semanal** (quarta a sexta). Não conflita tecnicamente com nada do
   que existe, mas é um processo de time — não uma peça de pipeline —, e não faz sentido impor
   isso hoje a um projeto de um único desenvolvedor. Fica registrado como ideia para quando (e se)
   o time crescer.

## Trade-offs considerados

**Separar em dois grupos (escolhida)** — permite adotar o Grupo A incrementalmente, item a item,
sem bloquear nada em discussão; e evita a armadilha de "incorporar sugestões" silenciosamente
desfazer decisões já tomadas e validadas em produção (que era exatamente o risco apontado pelo
próprio usuário ao pedir esta ADR).

**Adotar o documento de referência por completo (rejeitada)** — o documento foi escrito pensando
num time com múltiplos desenvolvedores, Tech Lead e PO distintos, e calendário fixo de releases;
aplicá-lo integralmente hoje criaria processo sem quem o execute (aprovações que nunca chegam,
isenções que não têm como ser configuradas) e pioraria o fluxo que acabou de ser validado
ponta a ponta nesta sessão.

## Consequências

**Positivas**
- Nenhuma sugestão foi descartada — tudo o que o usuário trouxe está registrado e rastreável.
- O Grupo A dá um caminho de execução imediato sem depender de nenhuma decisão de processo/equipe.
- O Grupo B protege o pipeline validado na Fase 1 de ser alterado por decisão implícita — cada
  contradição precisa ser resolvida conscientemente, com a pergunta certa já formulada.

**Negativas / pendências**
- Esta ADR não resolve nada sozinha — é só o registro do backlog. As perguntas do Grupo B seguem
  em aberto até serem respondidas numa conversa futura.
- Se o time crescer, várias peças do Grupo B (aprovações, isenções, calendário) deixam de ser
  hipotéticas e passam a exigir decisão em prazo curto.
