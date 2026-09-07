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
| [0008](./0008-frontend-angular.md) | Escolha de front-end: Angular + TypeScript | Proposto |
| [0009](./0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md) | Renomear a hierarquia genérica (Zero/Um/Dois/Três) para as esferas de gestão do SUS (Federal/Estadual/Municipal/Regional) + criar o nível Unidade de Saúde | Proposta |
| [0010](./0010-fluxo-de-branches-e-pipeline-de-promocao.md) | Fluxo de branches (developer → qa → cert → main) com pipeline de gate no GitHub Actions + troca de MySQL para PostgreSQL | Aceita |

As ADRs 0003 a 0008 formam um roadmap de arquitetura futura (não implementado) — ver notas de reconciliação em cada arquivo. Prioridade de implementação combinada: 0003 (arquitetura), 0006 (segurança) e 0007 (deploy) antes de novos domínios de negócio; 0008 (front-end) é a de menor prioridade.

Ver também [`DER-atual.md`](./DER-atual.md) (schema real implementado hoje) e [`DER.md`](./DER.md) (modelo de dados clínico futuro/proposto).
