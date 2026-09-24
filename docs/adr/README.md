# Architecture Decision Records (ADR)

Este diretório guarda os registros de decisões de arquitetura do projeto — o "porquê" por trás de escolhas que têm trade-offs relevantes e não são óbvias a partir do código sozinho.

Cada ADR é um arquivo `NNNN-titulo-curto.md`, numerado sequencialmente, seguindo o formato leve de Michael Nygard:

- **Status** — Proposta, Aceita, Rejeitada, Substituída (por qual ADR) ou Obsoleta.
- **Contexto** — o problema, restrição ou situação que motivou a decisão.
- **Decisão** — o que foi decidido.
- **Trade-offs considerados** — as alternativas avaliadas e por que a escolhida venceu.
- **Consequências** — o que essa decisão traz de bom, e o que ela deixa como dívida ou pendência conhecida.

Crie um novo ADR quando uma decisão envolver trade-offs (não para escolhas triviais ou reversíveis a baixo custo), especialmente quando a decisão contraria a alternativa "óbvia" e alguém no futuro provavelmente vai perguntar "por que não fizeram X em vez disso?".

## Índice

| ADR | Título | Status |
|---|---|---|
| [0001](./0001-restaurar-e-refatorar-em-vez-de-reescrever.md) | Restaurar e refatorar o código apagado, em vez de reescrever do zero | Aceita |
| [0002](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md) | Modelar a hierarquia de 4 níveis como entidade única autorreferenciada | Aceita |
| [0003](./0003-adocao-clean-architecture.md) | Adoção de Clean Architecture | Proposto |
| [0004](./0004-adocao-padroes-projeto.md) | Adoção de padrões de projeto (Factory, Builder, Strategy, Repository) | Proposto |
| [0005](./0005-separacao-dominio-jpa.md) | Separação entre entidades de domínio e entidades JPA | Proposto |
| [0006](./0006-seguranca-jwt.md) | Estratégia de segurança e autenticação com JWT | Proposto |
| [0007](./0007-deploy-docker.md) | Estratégia de deploy com Docker e CI/CD | Proposto |
| [0008](./0008-frontend-angular.md) | Escolha de front-end: Angular + TypeScript | Aceita (parcialmente implementada) |
| [0009](./0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md) | Renomear a hierarquia genérica (Zero/Um/Dois/Três) para as esferas de gestão do SUS (Federal/Estadual/Municipal/Regional) + criar o nível Unidade de Saúde | Aceita (parcialmente implementada) |
| [0010](./0010-fluxo-de-branches-e-pipeline-de-promocao.md) | Fluxo de branches (developer → qaa → homologacao → main) com pipeline de gate no GitHub Actions + troca de MySQL para PostgreSQL | Aceita |
| [0011](./0011-fase-2-melhorias-de-pipeline-e-branching.md) | Backlog de melhorias de branching/pipeline (Fase 2): PAT dedicado, feature/fix/hotfix, aprovações obrigatórias, testes de performance | Proposta |
| [0012](./0012-infraestrutura-local-k3s-homelab.md) | Infraestrutura local (home-lab K3s + ArgoCD) para os 4 ambientes, resolvendo a pendência de URL persistente em QA/Homologação | Proposta |
| [0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) | Implementar o 5º nível (Unidade de Saúde) e o vínculo de supervisão regional | Aceita |
| [0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) | Módulo Profissional/RH com vínculo fraco resolvido por reconciliação | Aceita |
| [0015](./0015-pinar-imagens-por-sha-para-sincronizacao-automatica-do-argocd.md) | Pinar imagens por SHA para sincronização automática do ArgoCD (sem SSH/restart manual) | Aceita |
| [0016](./0016-deploy-do-frontend-angular-no-ambiente-dev.md) | Deploy do front-end Angular no ambiente dev, em host separado | Aceita (escopo: dev) |
| [0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) | Número de matrícula automático e CPF deixa de ser único em Profissional | Aceita |
| [0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) | App shell de navegação e decisões de frontend da fatia 1 do módulo de RH | Aceita |
| [0019](./0019-vincular-cargo-e-lotacao-no-cadastro-de-profissional.md) | Vincular cargo e lotação (e portanto salário) no cadastro de profissional | Aceita |
| [0020](./0020-tela-central-do-profissional.md) | Tela central do Profissional (fatia 2) | Aceita |
| [0021](./0021-fluxo-de-transferencia-na-aba-lotacao.md) | Fluxo de transferência de lotação embutido no perfil, sem tela própria (fatia 3) | Aceita |
| [0022](./0022-composicao-remuneratoria-endpoint-calculado.md) | Endpoint calculado de composição remuneratória (fatia 4) | Aceita |
| [0023](./0023-afastamentos-e-licencas-no-perfil.md) | Afastamentos e Licenças no perfil (fatia 5) | Aceita |
| [0024](./0024-ponto-com-filtro-de-periodo.md) | Ponto com filtro de período (fatia 6a) | Aceita |
| [0025](./0025-folha-de-pagamento-por-competencia.md) | Folha de pagamento por competência (fatia 6b, fecha a fatia 6) | Aceita |
| [0026](./0026-historico-funcional-consolidado.md) | Histórico funcional consolidado (fatia 7) | Aceita |
| [0027](./0027-fatia-8-treinamento-avaliacao-sst-recrutamento.md) | Fatia 8: Treinamento, Avaliação, SST, Recrutamento | Aceita |
| [0028](./0028-fatia-9-beneficios.md) | Fatia 9: Benefícios (+ endpoint `encerrar` de AdesaoBeneficio) | Aceita |
| [0029](./0029-listagem-e-edicao-de-contato-do-profissional.md) | Listagem de profissionais e edição de contato | Aceita |
| [0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) | Setor Administrativo e relação com Unidade de Saúde | Aceita |
| [0031](./0031-perfil-administrativo-por-tipo-de-unidade.md) | Perfil Administrativo por tipo de unidade | Aceita |
| [0032](./0032-catalogo-de-capacidades-administrativas.md) | Catálogo de capacidades administrativas | Aceita |
| [0033](./0033-processos-administrativos-por-capacidade.md) | Processos administrativos por capacidade | Aceita |
| [0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) | Integração Administrativo ↔ RH sem duplicar Profissional | Aceita |
| [0035](./0035-responsabilidade-administrativa-separada-da-lotacao.md) | Responsabilidade administrativa separada da lotação | Aceita |
| [0036](./0036-necessidade-de-pessoal-encaminhada-ao-rh.md) | Necessidade de pessoal encaminhada ao RH | Aceita |
| [0037](./0037-criterio-para-especializacao-administrativa.md) | Critério para especialização administrativa | Proposta |

As ADRs 0003 a 0008 formam um roadmap de arquitetura futura (não implementado) — ver notas de reconciliação em cada arquivo. Prioridade de implementação combinada: 0003 (arquitetura), 0006 (segurança) e 0007 (deploy) antes de novos domínios de negócio; 0008 (front-end) é a de menor prioridade.

As ADRs 0030 a 0037 formam o roadmap do Setor Administrativo. 0030-0036 (Fases 1-6) já foram implementadas; 0037 (Fase 7+) ainda não — ver [`docs/administrativo/ESCOPO-ADMINISTRATIVO.md`](../administrativo/ESCOPO-ADMINISTRATIVO.md) para o contexto completo e a ordem de implementação planejada.

Ver também [`DER-atual.md`](./DER-atual.md) (schema real implementado hoje) e [`DER.md`](./DER.md) (modelo de dados clínico futuro/proposto).
