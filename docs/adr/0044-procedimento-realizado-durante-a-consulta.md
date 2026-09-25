# 0044 — Procedimento realizado durante a Consulta

## Status

Aceita e implementada (quinta fatia da onda Operação Assistencial — último insumo antes da
agregação do Prontuário — [PR #213](https://github.com/dufelizardo/mais_saude_publica/pull/213)).

## Contexto

Com `Paciente` (ADR-0040), `Atendimento` (ADR-0041), `Agendamento` (ADR-0042) e `Consulta`
(ADR-0043) implementados, falta `Procedimento` — o terceiro e último dos insumos reais
(`Atendimento`/`Consulta`/`Procedimento`) que o futuro endpoint de `Prontuário` (agregação de
leitura, ADR-0039 decisão 6) vai reunir por paciente.

O desenho original (DER.md, seção 8) já existia desde antes da reconciliação: `consulta_id`,
`profissional_id`, `tipo` (`VARCHAR(100)`, "CIRURGIA, EXAME, etc." — um conjunto **aberto**,
diferente das listas fechadas de `Atendimento.tipo`/`Agendamento.tipo`), `descricao`,
`data_realizacao`, `status` (AGENDADO/REALIZADO/CANCELADO).

## Decisão

- Nova entidade `Procedimento`: `uuid`, `consulta` (`@ManyToOne Consulta`, obrigatório, por uuid),
  `profissional` (`@ManyToOne Profissional`, obrigatório, FK interna por uuid resolvida pela API via
  `profissionalMatricula` — mesmo padrão de `Consulta.profissional`, ADR-0034/ADR-0043), `tipo`
  (**`String`, texto livre — não um enum**), `descricao` (opcional), `dataRealizacao`, `status`
  (novo enum `StatusProcedimento`: `AGENDADO, REALIZADO, CANCELADO`).
- **`tipo` fica como `String`, não um enum fechado** — diferente de todas as outras entidades desta
  onda (`TipoAtendimento`, `TipoAgendamento`, `TipoConsulta`), porque o próprio desenho original já
  descrevia um catálogo aberto ("CIRURGIA, EXAME, etc.") em vez de uma lista fechada. Inventar um
  enum fechado aqui contrariaria o desenho original e quebraria assim que um tipo de procedimento
  não previsto aparecesse.
- CRUD no mesmo formato de `Atendimento`/`Agendamento`/`Consulta`: `criar`/`atualizar` substituem os
  campos editáveis por inteiro.
- Sem `criado_em`/`atualizado_em` — mesma omissão já feita nas demais entidades desta onda.

## Trade-offs considerados

**`tipo` como enum fechado (`CIRURGIA, EXAME, ...`), igual às demais entidades (rejeitada)**
- ✅ Consistência com `TipoAtendimento`/`TipoAgendamento`/`TipoConsulta`.
- ❌ O próprio desenho original já sinalizava um conjunto aberto ("etc."); catálogos de
  procedimentos médicos reais (CIRURGIA, EXAME, CURATIVO, VACINA, ...) são grandes e crescem com
  frequência — um enum fechado exigiria alterar código a cada procedimento novo, sem nenhum
  requisito concreto pedindo essa rigidez.

## Consequências

**Positivas**: completa os três insumos (`Atendimento`, `Consulta`, `Procedimento`) que o endpoint
de `Prontuário` vai agregar por paciente — a próxima fatia da onda pode agora implementar essa
agregação de leitura sem esperar por mais nenhuma entidade nova.

**Negativas / pendências**: `tipo` como texto livre não valida contra um catálogo — aceito
conscientemente (YAGNI), revisitar só se um requisito real de relatório/indicador por tipo de
procedimento aparecer.

## Referências

- [`DER.md`](./DER.md), seção 8 (desenho original).
- [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) e
  [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — roadmap da onda Assistência, decisão 6
  (Prontuário como agregação).
- [ADR-0043](./0043-consulta-registrada-durante-o-atendimento.md) — entidade `Consulta`,
  referenciada aqui.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de FK
  por matrícula resolvida na fronteira da API.
