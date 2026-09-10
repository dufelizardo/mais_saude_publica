# 0012 — Infraestrutura local (home-lab K3s) para os 4 ambientes

## Status

**Implementada.** Concluída em 2026-09-10: SO instalado, rede configurada, k3s + MetalLB +
ArgoCD (Core) no ar, os 4 ambientes (`dev`/`qaa`/`homologacao`/`prod`) publicados via GitOps e
saudáveis, imagem publicada em `ghcr.io`, domínios locais funcionando via `hosts`. Ver "Passo a
passo de provisionamento" para o histórico completo e "Lições aprendidas" para os dois problemas
reais encontrados e corrigidos durante o rollout.

## Contexto

A [ADR-0010](0010-fluxo-de-branches-e-pipeline-de-promocao.md) resolveu o gate de CI (build +
JUnit + suíte Robot Framework) para promover código entre `developer → qaa → homologacao → main`,
mas deixou uma pendência registrada explicitamente nas suas "Negativas/pendências":

> QA e Certificação não têm URL navegável fora do momento do CI — só validam automaticamente, não
> servem para demonstração manual ou testes exploratórios ad-hoc.

O usuário trouxe uma proposta de infraestrutura ("nuvem privada local"): K3s + MetalLB + Ingress +
ArgoCD rodando num servidor doméstico, um Namespace por ambiente, domínios locais
(`*.mais-saude.local`) acessíveis na rede de casa para ele e a filha testarem.

Avaliei a proposta e, para o objetivo isolado de "só ter os 4 ambientes rodando de forma
confiável", teria recomendado uma alternativa mais simples (Docker Compose + Caddy como reverse
proxy) — bem menos peça móvel para manter sozinho, e mais fácil de recuperar depois de reformatar
a máquina. Perguntei ao usuário qual era o objetivo real por trás da proposta, e a resposta foi
explícita: **também é sobre praticar Kubernetes de verdade**, não só resolver a pendência da forma
mais simples. Isso muda a decisão — o valor de aprendizado justifica a complexidade adicional do
K3s/ArgoCD frente à alternativa mais simples.

A ADR-0007 já propõe Docker + Docker Compose + CI/CD via GitHub Actions com deploy em `ghcr.io` +
SSH — ainda não implementada. Esta ADR-0012 não substitui a ADR-0007 por completo: reaproveita a
parte de containerização (`Dockerfile`, build via `pipeline.yml`), mas troca o destino do deploy
via SSH por um cluster K3s local para os ambientes não-produtivos.

O deploy de produção via Render (`deploy-prod.yml`, ver ADR-0010) provavelmente não existe mais —
era como o usuário publicava em 2024 e ele mesmo indicou que a conta/serviço deve ter expirado.
Isso não é resolvido aqui: fica registrado como pendência em aberto se `main`/produção também
migra para este home-lab ou se um novo destino de deploy em nuvem é escolhido depois.

## Decisão

Adotar **K3s + MetalLB + Ingress (Traefik embutido) + ArgoCD** num servidor doméstico, com um
Namespace por ambiente e GitOps a partir do próprio repositório.

### Ambientes

| Ambiente | Branch | Namespace K3s | Domínio local |
|---|---|---|---|
| Developer | `developer` | `mais-saude-dev` | `dev.mais-saude.local` |
| QAA | `qaa` | `mais-saude-qaa` | `qaa.mais-saude.local` |
| Homologação | `homologacao` | `mais-saude-homolog` | `homologacao.mais-saude.local` |
| Produção | `main` | `mais-saude-prod` | `mais-saude.local` |

> A proposta original usava `cert.mais-saude.local` para o terceiro ambiente — nome desatualizado
> de antes do rename `cert` → `homologacao` já registrado na ADR-0010. Corrigido aqui para bater
> com o nome de branch real.

### Hardware do servidor

Definido: **laptop Acer Aspire 5 A515-41G-13U1**, reaproveitado como servidor doméstico.

| Componente | Especificação | Observação |
|---|---|---|
| CPU | AMD A12-9720P (quad-core, APU de 2017) | Sem uso de GPU planejado — a Radeon RX 540 dedicada fica ociosa num servidor headless |
| RAM | 8 GB DDR4 nominal — **6,7 GB reais utilizáveis** (confirmado via `free -h`) | Diferença esperada (parte reservada pra GPU integrada/firmware); é o número real a usar no orçamento de memória, não os 8 GB nominais |
| Armazenamento | ~~HDD 1 TB 5400 RPM de fábrica~~ **Trocado por SSD (465,8 GB, confirmado via `lsblk`)** | Já feito — ver justificativa abaixo |
| Hostname | `projetos-server` | |
| SO instalado | Ubuntu Server 26.04.1 LTS | Versão mais recente disponível no momento da instalação — supera a recomendação original de 24.04 LTS, mesma lógica de escolha se aplica |
| Rede | Wi-Fi (`wlp2s0`), IP estático `192.168.0.50/24` via netplan | Ethernet (`enp1s0f1`) disponível mas sem cabo conectado; ver nota abaixo sobre a dificuldade prática de configurar Wi-Fi sem acesso remoto |

**Por que trocar o HDD por SSD é importante, não opcional:** o `etcd` (banco de dados interno do
Kubernetes, usado pelo k3s) é sensível a latência de escrita em disco — é uma causa comum e bem
documentada de instabilidade em clusters pequenos rodando sobre HDD mecânico. Rodar o k3s sobre um
HDD 5400 RPM é o tipo de economia que tende a custar mais tempo depurando problemas estranhos do
que gastaria comprando o SSD.

**Orçamento de memória (estimativa, 6,7 GB reais — não 8 GB nominais):**

| Consumidor | Estimativa | Real observado |
|---|---|---|
| SO (Ubuntu Server headless) idle | ~0,3–0,5 GB | 574 MB (`free -h`, antes do k3s) |
| k3s (control plane) + MetalLB + Traefik (Ingress embutido) | ~0,5–0,8 GB | a confirmar após os 3 estarem rodando juntos |
| ArgoCD **Core** (application-controller, applicationset-controller, repo-server, redis — sem server/Dex/notifications) | ~0,5–0,8 GB | instalado; menor que a variante completa por não ter API/UI/SSO |
| 4× (Spring Boot + PostgreSQL), um par por ambiente | ~2–2,8 GB (500–700 MB por ambiente) | **medido: só ~0,8 GB pros 4 juntos** — bem abaixo da estimativa |
| **Total estimado / real** | ~3,3–4,9 GB estimado | **2,8 GB usados, 3,9 GB disponíveis com os 4 ambientes no ar** (`free -h`) — folga bem melhor que o pior caso estimado |

Mitigações efetivamente usadas:
- ✅ **ArgoCD Core** (sem Dex/SSO/notifications-controller/server) — reduz o footprint do control
  plane; acesso é só via `kubectl`/CLI local, sem UI web.
- ✅ **`-Xmx384m`** (via `JAVA_TOOL_OPTIONS`) explícito em todos os Deployments do Spring Boot.
- ❌ **Postgres compartilhado não foi necessário** — a medição real com os 4 ambientes rodando
  mostrou folga confortável (3,9 GB disponíveis); mitigação descartada, não precisou ser aplicada.

### Sistema operacional do servidor

| Opção | Vantagens | Desvantagens |
|---|---|---|
| **Ubuntu Server 24.04 LTS (recomendado)** | Maior compatibilidade e documentação com K3s; 5 anos de suporte; facilita instalar add-ons futuros (ex.: Pi-hole) | Um pouco mais pesado que Debian por padrão |
| Debian 12 | Footprint mais enxuto, mesma estabilidade, menos pacotes instalados por padrão | Documentação de K3s/K8s geralmente escrita pensando em Ubuntu primeiro |

Recomendação original: **Ubuntu Server 24.04 LTS**, pelo suporte mais longo e pela quantidade de
documentação/tutoriais de K3s que assumem Ubuntu como base. Na prática, **instalado Ubuntu Server
26.04.1 LTS** (versão mais recente disponível no momento) — mesma lógica de escolha se aplica,
LTS mais novo com mais tempo de suporte pela frente. Como é um laptop servindo como servidor:
`HandleLidSwitch=ignore` foi configurado em `/etc/systemd/logind.conf` e o carregador fica sempre
conectado — sem isso, fechar a tampa derruba o cluster inteiro.

**Lição aprendida na configuração de rede (Wi-Fi):** o assistente de instalação do Ubuntu Server
não persistiu a configuração de Wi-Fi feita durante a instalação (SSID/senha não ficaram salvos no
netplan gerado). Foi necessário editar `/etc/netplan/00-installer-config.yaml` manualmente após a
instalação, direto no console do servidor (sem copiar/colar disponível nessa etapa — só depois que
o SSH ficou de pé). Config final que funcionou:

```yaml
network:
  version: 2
  wifis:
    wlp2s0:
      dhcp4: false
      access-points:
        "<SSID_DA_REDE>":
          password: "<SENHA_DA_REDE>"
      addresses:
        - 192.168.0.50/24
      routes:
        - to: default
          via: 192.168.0.1
      nameservers:
        addresses: [1.1.1.1]
```

Se for reprovisionar essa máquina (ou outra) do zero, considerar usar Ethernet só durante a
instalação/setup inicial — evita todo esse atrito — e migrar pra Wi-Fi depois, já com SSH
disponível para copiar/colar a config em vez de digitar cada linha manualmente no console.

### Simplificação sobre a proposta original

K3s já vem com **Traefik como Ingress Controller embutido** por padrão — não é necessário instalar
o NGINX Ingress separadamente, como a proposta original sugeria. Uma peça a menos para manter.

### Origem das imagens de container

O `pipeline.yml` já existente builda a aplicação a cada PR — a proposta é estender esse pipeline
para publicar a imagem em **`ghcr.io`** (grátis para repositório público) a cada promoção. O
servidor K3s só puxa a imagem já pronta; nenhum build acontece no home-lab. Isso reaproveita o CI
que já existe em vez de criar um processo de build paralelo na máquina doméstica.

### Domínios locais

Solução via arquivo `hosts` (`/etc/hosts` no Linux/Mac, `C:\Windows\System32\drivers\etc\hosts` no
Windows) nas poucas máquinas da rede de casa (a do usuário + a da filha). MetalLB continua
necessário para dar um IP fixo ao Ingress dentro da rede local — sem ele, o IP do Ingress pode
mudar a cada reinício do cluster. Um servidor DNS local (ex.: Pi-hole) fica registrado como
melhoria futura, não necessário agora para só duas máquinas.

**Pool de IPs do MetalLB configurado:** `192.168.0.200-192.168.0.210` — faixa alta, escolhida pra
minimizar risco de conflito com o DHCP do modem (Sagemcom F@ST 3895; não foi possível confirmar o
range exato do DHCP dele, então optou-se por uma faixa improvável de colidir em vez de aguardar
essa confirmação). Se algum conflito de IP aparecer no futuro, revisar essa faixa.

### GitOps

Manifests do Kubernetes (Deployment, Service, Ingress, ConfigMap, Secret) organizados em
`/k8s/base` (genérico) + `/k8s/overlays/{dev,qaa,homologacao,prod}` (Kustomize, um overlay por
ambiente — namespace, host, tag de imagem e nome do banco variam por overlay). Os 4
`Application` do ArgoCD vivem em `/k8s/argocd-apps/`, cada um apontando pro `targetRevision` da
branch correspondente, com `syncPolicy.automated: {prune: true, selfHeal: true}`. A senha do
Postgres nunca entra no Git (repositório é público) — criada manualmente por namespace via
`kubectl create secret`, referenciada por nome (`secretKeyRef`) nos manifests.

### Lições aprendidas no rollout (2026-09-10)

Dois problemas reais apareceram só com o cluster de verdade, não eram previsíveis por estimativa:

1. **ArgoCD Core não cria o `AppProject` "default" sozinho** (a instalação completa cria; a Core
   não). Sem ele, todo `Application` fica preso em Sync Status "Unknown" para sempre — erro real
   nos logs do `application-controller`: `appproject.argoproj.io "default" not found`. Corrigido
   criando o `AppProject` manualmente (`k8s/argocd-apps/appproject-default.yaml`, aplicado uma vez
   no bootstrap).
2. **Liveness probe matava o container no meio da subida** — Exit Code 143 (SIGTERM do kubelet,
   não OOM), crash loop no primeiro deploy do ambiente `dev`. O CPU modesto do home-lab (AMD
   A12-9720P, 2017) faz o Spring Boot + criação de schema do Hibernate demorar mais que os
   `initialDelaySeconds` inicialmente configurados. Corrigido com um `startupProbe`
   (`failureThreshold: 36, periodSeconds: 5` — até 180s antes de liveness/readiness passarem a
   valer), em `k8s/base/deployment-app.yaml`.

Depois dessas duas correções, `qaa`, `homologacao` e `prod` subiram de primeira, sem crash loop.

## Passo a passo de provisionamento

Concluído em 2026-09-10:

1. ✅ Trocar o HDD pelo SSD M.2 NVMe.
2. ✅ Instalar o sistema operacional (Ubuntu Server 26.04.1 LTS).
3. ✅ Configurar rede (Wi-Fi, IP estático `192.168.0.50`) — ver lição aprendida na seção de
   hardware.
4. ✅ Desabilitar suspensão ao fechar a tampa; laptop sempre na energia AC.
5. ✅ Confirmar a RAM real disponível (`free -h`) contra o orçamento de memória — 6,7 GB reais,
   registrado na seção de hardware.
6. ✅ Instalar k3s: `curl -sfL https://get.k3s.io | sh -` (Traefik embutido confirmado).
7. ✅ Instalar MetalLB, pool `192.168.0.200-192.168.0.210`.
8. ✅ Instalar ArgoCD (variante Core) + `AppProject` default (ver "Lições aprendidas").
9. ✅ Escrever `/k8s/base` + overlays + `Application` CRs; `publish-image.yml` publicando
   `ghcr.io/dufelizardo/mais_saude_publica:<branch>` a cada push nas 4 branches.
10. ✅ Bootstrap do ArgoCD por ambiente (`kubectl create secret` + `kubectl apply` do
    `Application`), um de cada vez, medindo RAM real a cada um — `dev` → `qaa` → `homologacao` →
    `prod`, todos saudáveis, sem precisar da mitigação de Postgres compartilhado.
11. ✅ Arquivo `hosts` configurado (`192.168.0.200` para os 4 domínios) — ambientes acessíveis
    pela rede de casa.

## Trade-offs considerados

**K3s + MetalLB + ArgoCD (escolhida)**
- ✅ Resolve a pendência da ADR-0010 (ambientes navegáveis e persistentes).
- ✅ Aprendizado real de Kubernetes — objetivo explícito do usuário, não só a solução técnica.
- ❌ Mais peças móveis para manter sozinho (cluster, MetalLB, Ingress, ArgoCD) do que uma
  alternativa baseada em containers simples.
- ❌ Mais custoso de recuperar do zero depois de uma reformatação de máquina do que subir algumas
  stacks de container.

**Docker Compose + Caddy como reverse proxy (rejeitada, seria a recomendação padrão)**
- ✅ Muito mais simples: recuperar tudo após reformatar a máquina é `docker compose up` por
  ambiente, sem bootstrapar um cluster inteiro.
- ✅ Mesmo isolamento por ambiente (uma stack por ambiente), mesmo custo zero, roteamento por
  hostname com poucas linhas de configuração (Caddy faz isso nativamente, com HTTPS automático).
- ❌ Rejeitada aqui porque o objetivo explícito inclui praticar Kubernetes de verdade — essa
  alternativa não entrega esse valor.

## Consequências

**Positivas**
- Resolve de vez a pendência registrada na ADR-0010 sobre QA/Homologação sem URL persistente —
  os 4 ambientes estão no ar, saudáveis, acessíveis pela rede de casa via domínio próprio.
- RAM sobrou mais que o esperado (2,8 GB usados / 3,9 GB disponíveis com os 4 ambientes rodando) —
  a mitigação de Postgres compartilhado, cogitada por causa do orçamento apertado inicial, não foi
  necessária.
- Aprendizado real de Kubernetes como valor adicional explícito, não um efeito colateral — dois
  problemas reais de operação (`AppProject` ausente, liveness matando container na subida) foram
  encontrados e corrigidos ao vivo.
- Reaproveita o pipeline de CI já existente (build) em vez de duplicar esse processo no home-lab —
  `publish-image.yml` só adiciona o passo de publicar em `ghcr.io`.

**Negativas / pendências**
- Mais peças móveis para operar e depurar sozinho do que a alternativa mais simples (Compose) —
  já se provou na prática (2 problemas reais de bootstrap do ArgoCD/probes, ambos documentados nas
  "Lições aprendidas").
- A solução de domínio via `hosts` não escala além de poucas máquinas — se a rede de casa crescer,
  revisar para um DNS local (Pi-hole ou similar).
- Publicar imagens em `ghcr.io` exige que o repositório continue público, ou configurar
  autenticação de pull no cluster se ele se tornar privado no futuro.
- Destino de deploy de produção (`main`) fica em aberto — hoje o ambiente `prod` deste home-lab
  está no ar (`mais-saude.local`), mas o Render antigo nunca foi formalmente desativado/substituído
  como destino de produção "real" (fora da rede de casa); decidir isso separadamente.
