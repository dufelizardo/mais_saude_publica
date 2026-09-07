# 0008 — Escolha de front-end: Angular + TypeScript (proposta futura)

## Status

Proposto — **não implementado**. O repositório é hoje uma API pura (sem nenhum código de front-end). Esta proposta depende de ADR 0006 (JWT) e ADR 0007 (Docker) estarem em andamento, e é a de **menor prioridade** entre as propostas — deve vir depois de arquitetura, segurança e deploy, e depois de expandir os domínios de negócio se aplicável.

## Contexto

Uma interface seria necessária para que gestores, profissionais e administradores interajam com os dados de saúde pública: CRUD de unidades/profissionais/pacientes/atendimentos, dashboards e relatórios, consumindo a API REST com autenticação JWT (ADR 0006).

## Decisão

Adotar **Angular 17+** com **TypeScript**, **Angular Material** para componentes de UI, e **Playwright** para testes end-to-end.

- Estrutura de pastas proposta: `core/` (serviços singleton, interceptors), `shared/` (componentes reutilizáveis), `features/` (módulos por domínio).
- `AuthService` decodifica o JWT e guarda em `localStorage`; `JwtInterceptor` injeta o header `Authorization` em toda requisição HTTP.
- Testes E2E com Playwright cobrindo fluxos de login, listagem, criação, edição e exclusão de unidades.
- Deploy do front-end via `Dockerfile` próprio (`node:18-alpine` para build → `nginx:alpine` para servir os arquivos estáticos), integrado ao `docker-compose.yml` da ADR 0007.

## Trade-offs considerados

| Alternativa | Vantagens | Desvantagens | Decisão |
|---|---|---|---|
| Angular | Framework completo, TypeScript nativo, boa opção para equipes com background Java/OO | Curva de aprendizado, mais verboso que alternativas mais leves | ✅ Escolhida |
| React | Popular, ecossistema grande, mais flexível | Exige montar mais peças manualmente (roteamento, forms, etc.) | ❌ Rejeitada |
| Vue | Curva de aprendizado suave | Ecossistema menor para projetos corporativos | ❌ Rejeitada |
| Svelte | Performance, bundle pequeno | Ecossistema imaturo para o porte deste projeto | ❌ Rejeitada |
| Angular Material (UI) | Integração nativa com Angular, componentes acessíveis | Visual mais "padrão Google" | ✅ Escolhida sobre PrimeNG/Bootstrap/Tailwind |
| Playwright (E2E) | Rápido, multi-browser, boa DX | Ferramenta mais nova que Selenium | ✅ Escolhida sobre Cypress/Selenium |

## Consequências

**Positivas**: interface única para os diferentes perfis de usuário (ADMIN/GESTOR/PROFISSIONAL/CONSULTOR, ver ADR 0006); testes E2E cobrindo os fluxos críticos desde o início.

**Negativas**: introduz uma stack de front-end inteiramente nova para manter, versionar e fazer deploy junto com a API; exige conhecimento em Angular além do Java/Spring já usado no backend; nenhum código de front-end existe hoje — é a maior peça de trabalho entre todas as propostas deste roadmap.

**Neutras**: adiciona um `Dockerfile` e um serviço a mais no `docker-compose.yml` da ADR 0007.
