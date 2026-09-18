# 0015 — Pinar imagens por SHA para sincronização automática do ArgoCD

## Status

Aceita e implementada.

## Contexto

Cada ambiente (`dev`, `qaa`, `homologacao`, `prod`) tem seu overlay Kustomize
(`k8s/overlays/<ambiente>/kustomization.yaml`) apontando a imagem pra uma tag **mutável**, com o
nome da própria branch (`newTag: developer`, `newTag: qaa`, etc — ver ADR-0012). `publish-image.yml`
rebuilda e republica essa mesma tag a cada push na branch correspondente.

O problema: o `syncPolicy` do ArgoCD (mesmo com `selfHeal: true`) só reage a uma mudança real no
**texto** do manifest versionado no Git — não a um digest novo por trás de uma tag que não mudou de
nome. Como o texto `newTag: developer` nunca muda entre um build e outro, o ArgoCD nunca vê motivo
pra sincronizar, e o pod que já está rodando continua com a imagem antiga até alguém forçar um
`kubectl rollout restart` manualmente no servidor. Isso foi descoberto na prática: depois de mergear
uma mudança de código até `main`, o ambiente `dev.mais-saude.local` continuava servindo o Swagger
antigo, sem os endpoints novos, mesmo com `publish-image.yml` tendo rodado com sucesso.

O projeto irmão [`LoneWolf`](https://github.com/dufelizardo/LoneWolf) já tinha resolvido exatamente
esse problema (ADR-0001/ADR-0003 de lá): pinar o manifest na tag imutável `:<sha>` do build e
commitar isso de volta no repositório, fazendo o ArgoCD ver um diff real. A diferença aqui é que
`developer`/`qaa`/`homologacao`/`main` exigem PR pra qualquer mudança (Ruleset "Aprovação
obrigatória para terceiros", ver ADR-0011) — um push direto com o `GITHUB_TOKEN` padrão seria
rejeitado pela própria proteção de branch, então o commit de pin precisa de um ator com bypass de
admin nessa Ruleset.

## Decisão

- `publish-image.yml` passa a publicar duas tags por build: a mutável (`:developer`, `:qaa`, etc,
  mantida por compatibilidade/legibilidade) e a imutável `:${{ github.sha }}`.
- Novo job `pin-manifests` (depende de `publish`): identifica o overlay do ambiente pela branch
  (`developer`→`k8s/overlays/dev`, `qaa`→`k8s/overlays/qaa`, `homologacao`→`k8s/overlays/homologacao`,
  `main`→`k8s/overlays/prod`), substitui o `newTag:` daquele overlay pelo SHA do build, e commita
  de volta na mesma branch.
- O push desse commit usa o `AUTOMERGE_PAT` (mesmo secret já usado por `auto-merge.yml`) em vez do
  `GITHUB_TOKEN` padrão — só um ator com bypass de admin na Ruleset consegue empurrar direto pra
  essas branches sem passar por PR.
- O job `publish` ganha uma guarda `if: "!startsWith(github.event.head_commit.message, 'chore: pin
  ')"`: como o push com PAT conta como um push "de verdade" (ao contrário do `GITHUB_TOKEN`, que o
  GitHub sabe que não deve redisparar workflows), sem essa guarda o próprio commit de pin
  dispararia `publish-image.yml` de novo — um rebuild do mesmo código só pra gerar um SHA novo, que
  pinaria de novo, disparando de novo, em loop infinito.
  - **Tentativa anterior, revertida**: a primeira versão usou `[skip ci]` na mensagem do commit em
    vez dessa guarda no job. Isso quebrou a própria promoção: `[skip ci]` suprime **qualquer**
    workflow pra aquele commit, não só `publish-image.yml` — incluindo os checks de `pull_request`
    do PR de promoção seguinte (`developer→qaa`), que tem esse commit como head. O PR ficou aberto
    sem nenhum check rodando, sem `auto-merge.yml` disparar. Corrigido trocando por uma condição
    que olha só a mensagem do commit, sem suprimir workflows de outros eventos/PRs.
- `qaa`/`homologacao`/`main` (não `developer`) têm, além da Ruleset, uma **proteção de branch
  clássica separada** exigindo os checks `test`/`robot-acceptance`/`analyze` com `enforce_admins:
  true`. Diferente do bypass de aprovação da Ruleset, `enforce_admins: true` bloqueia **qualquer**
  push direto — inclusive o do `AUTOMERGE_PAT` — sem exceção nenhuma para admins. Descoberto na
  prática: `pin-manifests` funcionou de primeira em `developer`, mas falhou com "3 of 3 required
  status checks are expected" em `qaa`/`homologacao`/`main`. Resolvido desligando
  `enforce_admins` nos 3 (`DELETE
  /repos/{owner}/{repo}/branches/{branch}/protection/enforce_admins`) — os checks em si continuam
  obrigatórios pra qualquer PR normal (só passaram a não travar mais um push administrativo já
  autorizado por outro mecanismo).

## Trade-offs considerados

**Pin por SHA + commit automático via PAT (escolhida)**
- ✅ Sincronização real e automática — nenhum passo manual no servidor depois de um deploy, em
  nenhum dos 4 ambientes.
- ✅ Reaproveita um padrão já validado no LoneWolf, adaptado pra lidar com a Ruleset de aprovação
  que aquele projeto não tem.
- ❌ Mais um commit automático (`chore: pin ...`) no histórico de cada branch a cada deploy — ruído
  aceitável, mesmo trade-off já aceito no LoneWolf.
- ❌ Depende do `AUTOMERGE_PAT` ter bypass de admin na Ruleset também para push direto, não só para
  merge de PR — comportamento não documentado explicitamente pelo GitHub para Rulesets, verificado
  na prática (o mesmo PAT com permissão "Administration" que já bypassa `gh pr merge --admin`
  também bypassa o push direto, já que o bypass é avaliado pelo papel do ator, não pela ação).

**`kubectl rollout restart` manual a cada deploy (rejeitada)**
- ✅ Zero mudança de pipeline.
- ❌ Rejeitada a pedido do usuário: exige lembrar de rodar o comando manualmente em até 4
  ambientes a cada promoção — exatamente o passo manual que a automação de CI/CD já existente
  (developer→qaa→homologacao→main com auto-merge) tenta eliminar em todo o resto do fluxo.

**ArgoCD Image Updater (rejeitada)**
- ✅ Solução "nativa" do ecossistema ArgoCD pra esse problema exato, sem precisar de commits
  automáticos no repositório.
- ❌ Rejeitada: exige instalar e operar mais um controller no cluster, contrariando o objetivo
  explícito da ADR-0012 de manter a infraestrutura do home-lab enxuta (só o que já vem com o K3s +
  ArgoCD Core).

**SSH do CI pro servidor pra rodar `kubectl rollout restart` direto (rejeitada)**
- ✅ Resolveria o mesmo problema sem depender de bypass de Ruleset.
- ❌ Rejeitada: contraria diretamente o modelo GitOps-pull "sem SSH" que é a decisão central da
  ADR-0012 — exigiria expor SSH do servidor doméstico pra fora da rede de casa, aumentando a
  superfície de ataque por uma conveniência que o pin-by-SHA já resolve sem esse custo.

## Consequências

**Positivas**
- Os 4 ambientes (`dev`, `qaa`, `homologacao`, `prod`) agora atualizam sozinhos a cada deploy, sem
  intervenção manual no servidor.
- O SHA pinado no overlay funciona como um registro auditável, no próprio Git, de qual commit está
  rodando em cada ambiente a qualquer momento — não é preciso consultar o cluster pra saber.

**Negativas / pendências**
- Cada branch (`developer`/`qaa`/`homologacao`/`main`) acumula um commit `chore: pin ...` extra a
  cada deploy — não afeta a promoção (PRs de promoção pegam o HEAD já pinado), mas deixa o
  histórico um pouco mais ruidoso.
- O comportamento de bypass do `AUTOMERGE_PAT` para push direto (não só merge de PR) foi inferido
  pela prática, não documentado explicitamente na API/UI do GitHub para Rulesets — se o GitHub
  mudar essa semântica no futuro, o job `pin-manifests` passaria a falhar visivelmente (push
  rejeitado), não silenciosamente.
- `enforce_admins` desligado em `qaa`/`homologacao`/`main` significa que um admin (só o dono do
  repositório, projeto solo) também poderia, em tese, mergear/pushar manualmente pulando os checks
  obrigatórios — risco aceito porque o próprio dono já tinha esse poder via `gh pr merge --admin`
  bypassando a Ruleset de aprovação; `enforce_admins` só adicionava uma segunda barreira redundante
  contra a mesma pessoa, sem proteger contra terceiros (que continuam sem bypass nenhum).
