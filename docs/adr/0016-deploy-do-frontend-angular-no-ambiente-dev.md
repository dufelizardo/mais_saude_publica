# 0016 — Deploy do front-end Angular no ambiente dev, em host separado

## Status

Aceita e implementada. **Escopo ampliado (2026-09-20)**: replicado para `qaa`, `homologacao` e
`prod` — ver seção "Atualização: escopo ampliado para os 4 ambientes" no fim deste documento.

## Contexto

A ADR-0008 já tinha decidido a stack (Angular + TypeScript) pro front-end, mas nunca tinha sido
implementada — o repositório era só a API até a landing page ser construída (`frontend/`, mirror do
mockup estático em `modelo_front/Landing Page.html`, sem autenticação por enquanto — login depende
da ADR-0006/JWT, ainda não implementada). Depois de validar a landing page localmente (`ng serve`),
o próximo passo natural era colocá-la no ar no home-lab, a pedido explícito do usuário — mas só no
ambiente `dev`, não nos 4 ambientes de uma vez.

Cada ambiente já tem um Ingress próprio (`k8s/base/ingress.yaml`, host patchado por overlay — ver
ADR-0012) roteando **todo** o tráfego de `<ambiente>.mais-saude.local` pro backend (Swagger em
`/swagger-ui.html`, OpenAPI em `/v3/api-docs`, API em `/api/v1/...`). Colocar o front-end no mesmo
host exigiria um split por path (como o LoneWolf fez com `/api` vs `/`) — mas lá o backend não tinha
nada servido fora de `/api`, e aqui o Swagger e o `/v3/api-docs` ficam em paths de topo-de-nível que
não cabem debaixo de um prefixo `/api` sem reescrever rotas.

## Decisão

- Novo host dedicado só pro front-end: `frontend-dev.mais-saude.local` (Ingress próprio,
  `k8s/overlays/dev/frontend-ingress.yaml`) — `dev.mais-saude.local` continua 100% do jeito que
  estava, servindo só a API.
- Novos manifests (`frontend-deployment.yaml`, `frontend-service.yaml`) ficam **só no overlay
  `dev`** (`k8s/overlays/dev/`), não em `k8s/base/` — os outros 3 ambientes (`qaa`, `homologacao`,
  `prod`) não ganham automaticamente o front-end só por promoverem o código; precisariam do mesmo
  tratamento explícito se/quando for a vez deles.
- `frontend/Dockerfile`: build multi-stage (`node:22-alpine` → `nginx:1.27-alpine`), mesmo padrão já
  validado no LoneWolf (`app/Dockerfile` de lá) — sem reinventar.
- `publish-image.yml` vira uma matrix de 2 imagens (`mais_saude_publica` e
  `mais_saude_publica-frontend`), builda as duas a cada push nas 4 branches — mesmo sem os outros 3
  ambientes consumirem a imagem do front-end ainda, ela já fica publicada e pronta pra quando forem
  ganhar o deploy.
- `pin-manifests` (ADR-0015) não precisou de nenhuma mudança: o `sed` que pina `newTag:` já
  substitui todas as ocorrências no overlay, então tanto a imagem do backend quanto a do front-end
  (quando presentes no mesmo `kustomization.yaml`) recebem o mesmo SHA do build automaticamente.

## Trade-offs considerados

**Host dedicado por serviço (escolhida)**
- ✅ Zero risco de quebrar o Swagger/API já funcionando em `dev.mais-saude.local` — nenhuma rota
  existente muda.
- ✅ Mais simples de entender e depurar (cada Ingress roteia um serviço só, sem regra de path).
- ❌ Consome mais um hostname `.local` por ambiente quando o front-end for pros outros 3 — mais
  entradas de `hosts`/DNS local pra manter (aceitável, sem custo real além de mais uma linha).

**Split por path no mesmo host (rejeitada)**
- ✅ Um host só por ambiente, mais parecido com o padrão do LoneWolf.
- ❌ Rejeitada: exigiria reescrever/mover `/swagger-ui.html` e `/v3/api-docs` pra debaixo de um
  prefixo, ou criar uma regra de path por endpoint especial — mais complexidade e mais risco de
  quebrar o acesso ao Swagger que já está em uso, por uma economia de hostname que não compensa.

## Consequências

**Positivas**
- Landing page acessível via `http://frontend-dev.mais-saude.local` (depois de um entry no `hosts`
  do cliente apontando pro IP do MetalLB, mesmo padrão já usado pros outros hosts `.local`).
- Nenhuma mudança de comportamento em `dev.mais-saude.local` (API/Swagger).

**Negativas / pendências**
- Sem HTTPS/TLS em nenhum host `.local` — consistente com o resto da infraestrutura do home-lab
  (ADR-0012), não é uma lacuna nova introduzida aqui.

## Atualização: escopo ampliado para os 4 ambientes (2026-09-20)

A pendência "front-end só existe em dev" foi resolvida: os mesmos 3 manifests
(`frontend-deployment.yaml`, `frontend-service.yaml`, `frontend-ingress.yaml`) foram replicados
para `k8s/overlays/qaa/`, `k8s/overlays/homologacao/` e `k8s/overlays/prod/`, cada um com seu
próprio host — mesmo padrão de nomenclatura já usado pela API em cada ambiente:

| Ambiente | Host do frontend |
|---|---|
| `dev` | `frontend-dev.mais-saude.local` |
| `qaa` | `frontend-qaa.mais-saude.local` |
| `homologacao` | `frontend-homologacao.mais-saude.local` |
| `prod` | `frontend.mais-saude.local` (sem sufixo, mesmo padrão que a API usa em prod) |

`deployment.yaml`/`service.yaml` são idênticos entre ambientes (a imagem correta é resolvida pelo
`images:` override de cada `kustomization.yaml`, mesmo mecanismo já usado para a imagem do
backend) — só `frontend-ingress.yaml` muda, por causa do host. `publish-image.yml` já buildava a
imagem do frontend para todas as branches desde a decisão original; esta atualização só passa a
consumi-la nos 3 ambientes que antes não tinham manifest nenhum.
