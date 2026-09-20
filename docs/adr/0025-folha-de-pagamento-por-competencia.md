# 0025 — Folha de pagamento por competência (fatia 6b, fecha a fatia 6)

## Status

Aceita e implementada.

## Contexto

Última lacuna de backend do roadmap de telas: `FolhaPagamentoRepository` só listava por
profissional — não existia uma visão "todos os profissionais numa competência", que é o caso de
uso real de quem fecha a folha do mês (não se processa um profissional de cada vez pensando nele
isoladamente, se processa "a folha de setembro").

## Decisão

### Competência via query param, não path variable

`competencia` é uma `String` livre no formato `MM/AAAA` (ex.: `"09/2026"`, já era assim desde a
Fase 5) — **contém uma barra**. Usar como path variable (`/competencia/09/2026`) quebraria o
roteamento (Spring interpretaria como 2 segmentos, não 1). Resolvido com query param:
`GET /api/v1/folha-pagamento/competencia?valor=09/2026`.

### Lista vazia é 200, não 404 — diferente do padrão "por profissional"

Todo outro `listarHistorico`/`listarPorProfissional` do módulo retorna 404 em lista vazia ("esse
profissional não tem nada" geralmente é estranho). Aqui é o oposto: "ninguém foi processado ainda
nesta competência" é o estado normal de partida da tela — ela existe justamente pra ir sendo
preenchida aos poucos, conforme cada folha individual é registrada. 404 nesse caso seria tratar um
estado esperado como erro.

### Registro continua por profissional, na aba já existente do perfil

`POST /api/v1/folha-pagamento/` não mudou — continua um registro por vez, por profissional (regra
de negócio: no máximo uma folha por profissional por competência). A aba "Folha de pagamento" no
perfil (ADR-0020) ganha o form de registro + o histórico daquele profissional. A tela nova serve
só pra **consultar** o apanhado da competência, não pra registrar em lote.

### Nova tela standalone `/rh/folha-pagamento` — a primeira desde a fatia 1 que não é sobre um profissional só

Diferente de tudo construído nas fatias 2-6a (que viveu dentro do perfil de um profissional
específico), "todos os profissionais numa competência" é uma visão cruzada — não faz sentido
dentro do perfil de ninguém. Ganhou rota própria e item de menu novo no `AppShell`, mesmo padrão
de nav-item real (sem decoração) já estabelecido na ADR-0018.

## Trade-offs considerados

**Query param pra competência (escolhida)**
- ✅ Evita o problema de roteamento da barra sem precisar trocar o formato de `competencia`
  (mudar pra `AAAA-MM`, por exemplo, quebraria compatibilidade com os dados já gravados desde a
  Fase 5).
- ❌ Levemente menos "RESTful" que um path variable — aceitável, a barra no valor é o fator
  decisivo aqui.

**Endpoint de fechamento em lote (ex.: "processar todos de uma vez") (rejeitada, fora de escopo)**
- Não foi pedido e envolveria decidir de onde vêm os valores de cada profissional (que hoje são
  só informados, não calculados) — mistura papéis que o próprio `MODELO-RH.md` mantém separados.

## Consequências

**Positivas**: fecha a fatia 6 e, com ela, todas as lacunas de backend conhecidas desde a análise
original da fatia 1 (Composição remuneratória, Licenças por profissional, Ponto por período, Folha
por competência).

**Negativas / pendências**: nenhum "fechamento" formal de competência (marcar como concluída,
travar edição) — cada folha continua editável/registrável individualmente, sem um estado de ciclo
pra competência inteira. Fora de escopo pedido.

## Referências

- [ADR-0020](./0020-tela-central-do-profissional.md) — aba "Folha de pagamento" no perfil.
- [ADR-0024](./0024-ponto-com-filtro-de-periodo.md) — outra lacuna da mesma fatia 6, resolvida
  antes desta.
