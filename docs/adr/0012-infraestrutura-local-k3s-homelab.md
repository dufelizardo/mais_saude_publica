# 0012 — Infraestrutura local (home-lab K3s) para os 4 ambientes

## Status

Proposta — não implementada. A máquina física já está definida (ver "Hardware do servidor"
abaixo), mas ainda será formatada. Este documento registra a decisão de arquitetura e o passo a
passo de provisionamento para quando essa formatação acontecer.

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
| RAM | 8 GB DDR4 de fábrica | 1 slot acessível + parte soldada, conforme o modelo — expansão além de 8 GB não é garantida pelo fabricante; confirmar a capacidade real depois de instalado o SO (`sudo dmidecode --type memory`) antes de planejar upgrade |
| Armazenamento | HDD 1 TB 5400 RPM de fábrica | **Trocar por SSD M.2 NVMe antes de instalar o k3s** — ver justificativa abaixo |

**Por que trocar o HDD por SSD é importante, não opcional:** o `etcd` (banco de dados interno do
Kubernetes, usado pelo k3s) é sensível a latência de escrita em disco — é uma causa comum e bem
documentada de instabilidade em clusters pequenos rodando sobre HDD mecânico. Rodar o k3s sobre um
HDD 5400 RPM é o tipo de economia que tende a custar mais tempo depurando problemas estranhos do
que gastaria comprando o SSD.

**Orçamento de memória (estimativa, 8 GB totais):**

| Consumidor | Estimativa |
|---|---|
| SO (Ubuntu Server headless) + k3s (control plane) | ~0,8–1 GB |
| ArgoCD (application-controller, repo-server, server, redis, dex) | ~1–1,5 GB |
| Traefik (Ingress embutido) + MetalLB | ~0,2–0,3 GB |
| 4× (Spring Boot + PostgreSQL), um par por ambiente | ~2–2,8 GB (500–700 MB por ambiente) |
| **Total estimado** | **~4–5,6 GB de 8 GB** — viável, mas com pouca folga |

Recomendações para não estourar essa margem:
- Definir `-Xmx` explícito (heap da JVM) em cada Deployment do Spring Boot, em vez de deixar a JVM
  decidir sozinha — evita que um ambiente consuma memória além do previsto.
- Considerar instalar a variante **ArgoCD Core** (sem Dex/SSO nem notifications-controller,
  desnecessários para um usuário único) para reduzir o footprint do control plane.
- Se a margem apertar na prática, considerar **1 único PostgreSQL compartilhado com 4 databases
  lógicos** (um por ambiente) em vez de 4 pods de Postgres separados — troca isolamento total do
  banco por memória; decisão a tomar depois de medir o consumo real.

### Sistema operacional do servidor

| Opção | Vantagens | Desvantagens |
|---|---|---|
| **Ubuntu Server 24.04 LTS (recomendado)** | Maior compatibilidade e documentação com K3s; 5 anos de suporte; facilita instalar add-ons futuros (ex.: Pi-hole) | Um pouco mais pesado que Debian por padrão |
| Debian 12 | Footprint mais enxuto, mesma estabilidade, menos pacotes instalados por padrão | Documentação de K3s/K8s geralmente escrita pensando em Ubuntu primeiro |

Recomendação: **Ubuntu Server 24.04 LTS**, pelo suporte mais longo e pela quantidade de
documentação/tutoriais de K3s que assumem Ubuntu como base — reduz atrito ao resolver problemas
sozinho. Como é um laptop servindo como servidor: desabilitar suspensão ao fechar a tampa
(`HandleLidSwitch=ignore` em `/etc/systemd/logind.conf`) e manter sempre na energia AC — sem isso,
fechar a tampa derruba o cluster inteiro.

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

### GitOps

Manifests do Kubernetes (Deployment, Service, Ingress, ConfigMap, Secret) organizados em
`/k8s/{dev,qaa,homologacao,prod}` dentro do próprio repositório — consistente com o projeto já
operar como repositório único. ArgoCD monitora esse diretório e sincroniza automaticamente cada
subpasta com o Namespace correspondente. A criação efetiva desses manifests é uma tarefa futura
separada, fora do escopo desta ADR (que é só a decisão de arquitetura + provisionamento da base).

## Passo a passo de provisionamento

1. **Trocar o HDD pelo SSD M.2 NVMe** antes de tudo — ver justificativa na seção de hardware.
2. Instalar o sistema operacional (Ubuntu Server 24.04 LTS recomendado).
3. Desabilitar suspensão ao fechar a tampa e confirmar que o laptop fica sempre na energia AC.
4. Confirmar a RAM real disponível (`sudo dmidecode --type memory`) contra o orçamento de memória
   estimado na seção de hardware.
5. Instalar Docker (opcional — útil para testes locais de imagem antes de publicar em `ghcr.io`).
6. Instalar k3s: `curl -sfL https://get.k3s.io | sh -` (já traz Traefik como Ingress embutido).
7. Instalar MetalLB, com um pool de IPs da rede local reservado para ele.
8. Instalar ArgoCD — considerar a variante Core (ver seção de hardware) dado o orçamento de RAM.
9. Criar a estrutura `/k8s/{dev,qaa,homologacao,prod}` no repositório, com os manifests de cada
   ambiente, incluindo `-Xmx` explícito no Deployment do Spring Boot (tarefa futura separada — não
   faz parte desta ADR).
10. Apontar o ArgoCD para o repositório e configurar sincronização automática por diretório/Namespace.
11. Configurar o arquivo `hosts` nas máquinas da rede de casa com o IP fixo do Ingress (MetalLB) e
    os 4 domínios da tabela acima.

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
- Resolve de vez a pendência registrada na ADR-0010 sobre QA/Homologação sem URL persistente.
- Ambientes navegáveis e persistentes para os 4 estágios (`dev`/`qaa`/`homologacao`/`main`),
  acessíveis por toda a rede de casa via domínio próprio.
- Aprendizado real de Kubernetes como valor adicional explícito, não um efeito colateral.
- Reaproveita o pipeline de CI já existente (build) em vez de duplicar esse processo no home-lab.

**Negativas / pendências**
- Máquina definida (Acer Aspire 5 A515-41G-13U1) mas ainda não formatada — nada aqui pode ser
  validado até essa formatação acontecer, incluindo a troca de HDD por SSD.
- Hardware modesto (APU quad-core de 2017, 8 GB RAM) deixa pouca folga de memória — o orçamento
  estimado (~4–5,6 GB de 8 GB) é viável mas exige as mitigações já listadas (`-Xmx` explícito,
  ArgoCD Core, possível Postgres compartilhado); precisa ser validado com medição real depois do
  provisionamento, não só estimativa.
- Mais peças móveis para operar e depurar sozinho do que a alternativa mais simples (Compose).
- A solução de domínio via `hosts` não escala além de poucas máquinas — se a rede de casa crescer,
  revisar para um DNS local (Pi-hole ou similar).
- Publicar imagens em `ghcr.io` exige que o repositório continue público, ou configurar
  autenticação de pull no cluster se ele se tornar privado no futuro.
- Destino de deploy de produção (`main`) fica em aberto — decidir separadamente se migra para este
  home-lab ou se um novo destino em nuvem substitui o Render (hoje presumivelmente inativo).
- A estrutura `/k8s/*` e os manifests de cada ambiente ainda precisam ser escritos — tarefa futura
  separada desta ADR.
