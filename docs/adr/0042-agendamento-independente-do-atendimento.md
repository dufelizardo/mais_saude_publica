# 0042 — Agendamento, independente do Atendimento

## Status

Aceita e implementada (terceira fatia da onda Operação Assistencial — [PR #211](https://github.com/dufelizardo/mais_saude_publica/pull/211)).

## Contexto

Com `Paciente` (ADR-0040) e `Atendimento` (ADR-0041) implementados, a ordem definida em
ADR-0039/`MAPA-DE-DOMINIOS.md` segue para `Agendamento` — quando e onde os serviços serão
realizados. O modelo original (DER.md, seção 13) já existia desde antes da reconciliação:
`paciente_id`, `profissional_id`, `data_hora`, `status` (AGENDADO/CONFIRMADO/REALIZADO/CANCELADO),
`tipo` (CONSULTA/PROCEDIMENTO/RETORNO), `observacao`.

A ADR-0041 já registrou a decisão de que `Atendimento` existe independente de `Agendamento` — um
agendamento pode nunca virar atendimento (não comparecimento), e um atendimento pode não ter
agendamento (acolhimento espontâneo) — e deixou explicitamente pendente o campo
`Atendimento.agendamento` (FK opcional) para quando esta entidade existisse.

## Decisão

- Nova entidade `Agendamento`: `uuid`, `paciente` (`@ManyToOne Paciente`, obrigatório, por uuid —
  mesmo padrão de `Atendimento.paciente`), `profissional` (`@ManyToOne Profissional`, obrigatório,
  FK interna por uuid resolvida pela API via `profissionalMatricula` — mesmo padrão de
  `Atendimento.profissional`, ADR-0034/ADR-0041), `dataHora`, `status` (novo enum
  `StatusAgendamento`: `AGENDADO, CONFIRMADO, REALIZADO, CANCELADO`), `tipo` (novo enum
  `TipoAgendamento`: `CONSULTA, PROCEDIMENTO, RETORNO`), `observacao` (opcional).
- **Sem `unidade`/`setor`** — o desenho original do DER.md nunca teve esses campos para
  `Agendamento` (diferente de `Atendimento`); um agendamento é entre paciente e profissional, a
  unidade só entra quando o atendimento de fato acontece.
- **Sem `criado_em`/`atualizado_em`** — nenhuma entidade do projeto usa timestamps de auditoria
  hoje (mesma omissão já feita em `Atendimento`, ADR-0041); revisitar apenas quando o domínio
  transversal de Auditoria (ADR-0039, `MAPA-DE-DOMINIOS.md` #18) for implementado.
- **`Atendimento.agendamento` é acrescentado agora** (FK opcional, `@ManyToOne Agendamento`) —
  cumprindo a decisão pendente da ADR-0041. `AtendimentoRequestDto` ganha `agendamentoId` opcional;
  omitir o campo mantém o comportamento de "acolhimento espontâneo" já existente.
- CRUD no mesmo formato do `Setor`/`Atendimento`: `criar`/`atualizar` substituem os campos
  editáveis por inteiro, incluindo `status` — sem endpoint dedicado de "confirmar"/"cancelar".

## Trade-offs considerados

**`Agendamento` referenciar `Setor`/`UnidadeDeSaude` como o `Atendimento` (rejeitada)**
- ✅ Permitiria saber de antemão em qual unidade o agendamento vai acontecer.
- ❌ Não é o que o desenho original do usuário pedia, e nenhum requisito concreto força isso agora —
  quando o atendimento de fato ocorre, ele já carrega sua própria unidade/setor; adicionar duplicaria
  informação sem consumidor.

**Deixar `Atendimento.agendamento` para depois de novo, esperando um requisito real (rejeitada)**
- ✅ Evitaria alterar `Atendimento` outra vez, na fatia seguinte.
- ❌ A ADR-0041 já registrou a intenção explícita de acrescentar esse campo assim que `Agendamento`
  existisse — adiar de novo sem motivo novo contradiria a própria decisão anterior; o cenário
  "atendimento nasce de um agendamento" é exatamente o caso de uso central que motivou `Agendamento`
  existir como entidade separada.

## Consequências

**Positivas**: fecha o ciclo Paciente → Atendimento → Agendamento descrito na ADR-0039; um
atendimento pode agora referenciar o agendamento que o originou, sem forçar essa referência.

**Negativas / pendências**: sem filas de espera ou verificação de conflito de horário
(`profissional` com dois agendamentos sobrepostos) — YAGNI, nenhum requisito concreto pede isso
ainda. `Prontuário` (última fatia desta onda) ainda não existe.

## Referências

- [`DER.md`](./DER.md), seção 13 (desenho original de Agendamento) e "Modelo revisado para a
  próxima onda" (Atendimento).
- [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) e
  [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — roadmap da onda Assistência.
- [ADR-0041](./0041-atendimento-registra-entrada-do-paciente-na-rede.md) — entidade `Atendimento`,
  cuja decisão pendente (campo `agendamento`) esta ADR cumpre.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de FK
  por matrícula resolvida na fronteira da API.
