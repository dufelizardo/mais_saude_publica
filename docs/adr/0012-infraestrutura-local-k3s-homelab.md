# 0012 — Infraestrutura local (home-lab K3s) para os 4 ambientes

## Status

Proposta — não implementada. Depende de uma máquina física que ainda não existe: será formatada,
e o sistema operacional ainda será escolhido. Este documento registra a decisão de arquitetura e o
passo a passo de provisionamento para quando essa máquina estiver pronta.

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

### Sistema operacional do servidor

| Opção | Vantagens | Desvantagens |
|---|---|---|
| **Ubuntu Server 24.04 LTS (recomendado)** | Maior compatibilidade e documentação com K3s; 5 anos de suporte; facilita instalar add-ons futuros (ex.: Pi-hole) | Um pouco mais pesado que Debian por padrão |
| Debian 12 | Footprint mais enxuto, mesma estabilidade, menos pacotes instalados por padrão | Documentação de K3s/K8s geralmente escrita pensando em Ubuntu primeiro |

Recomendação: **Ubuntu Server 24.04 LTS**, pelo suporte mais longo e pela quantidade de
documentação/tutoriais de K3s que assumem Ubuntu como base — reduz atrito ao resolver problemas
sozinho.

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

1. Instalar o sistema operacional (Ubuntu Server 24.04 LTS recomendado).
2. Instalar Docker (opcional — útil para testes locais de imagem antes de publicar em `ghcr.io`).
3. Instalar k3s: `curl -sfL https://get.k3s.io | sh -` (já traz Traefik como Ingress embutido).
4. Instalar MetalLB, com um pool de IPs da rede local reservado para ele.
5. Instalar ArgoCD (`kubectl create namespace argocd` + manifests oficiais).
6. Criar a estrutura `/k8s/{dev,qaa,homologacao,prod}` no repositório, com os manifests de cada
   ambiente (tarefa futura separada — não faz parte desta ADR).
7. Apontar o ArgoCD para o repositório e configurar sincronização automática por diretório/Namespace.
8. Configurar o arquivo `hosts` nas máquinas da rede de casa com o IP fixo do Ingress (MetalLB) e
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
- Depende de uma máquina física que ainda não existe/não foi formatada — nada aqui pode ser
  validado até essa máquina estar pronta.
- Mais peças móveis para operar e depurar sozinho do que a alternativa mais simples (Compose).
- A solução de domínio via `hosts` não escala além de poucas máquinas — se a rede de casa crescer,
  revisar para um DNS local (Pi-hole ou similar).
- Publicar imagens em `ghcr.io` exige que o repositório continue público, ou configurar
  autenticação de pull no cluster se ele se tornar privado no futuro.
- Destino de deploy de produção (`main`) fica em aberto — decidir separadamente se migra para este
  home-lab ou se um novo destino em nuvem substitui o Render (hoje presumivelmente inativo).
- A estrutura `/k8s/*` e os manifests de cada ambiente ainda precisam ser escritos — tarefa futura
  separada desta ADR.
