# 0051 — Dispensação: terceira entidade do domínio Farmácia

## Status

Aceita e implementada.

## Contexto

`Medicamento` (ADR-0049) e `Lote` (ADR-0050) já estão implementados. Seguindo a disciplina
fatia-por-fatia já usada em todo o domínio (nenhuma ADR antecipada — só quando houver uma decisão
real com trade-off pra registrar, mesma regra escrita no `DER.md`), a próxima candidata é
`Dispensacao` — terceira entidade listada no `DER.md`/`MAPA-DE-DOMINIOS.md` pro domínio Farmácia, e
a primeira que efetivamente movimenta o estoque (`Lote.quantidade`) que `Lote` só armazenava até
agora.

O fluxo original do `DER.md` é `Prescrição → Farmácia → Dispensação → Paciente`. Não existe uma
entidade `Prescricao` própria — o "receituário" já é um campo de texto livre em `Consulta`
(`Consulta.receituario`, ADR-0043) — então `Dispensacao` referencia `Consulta` como o registro da
prescrição, quando existir.

## Decisão

- Nova entidade `Dispensacao`: `uuid`, `lote` (`@ManyToOne Lote`, obrigatório — de qual lote saiu),
  `paciente` (`@ManyToOne Paciente`, obrigatório — pra quem), `profissional`
  (`@ManyToOne Profissional`, obrigatório, FK por matrícula resolvida na fronteira da API — ADR-0034
  — farmacêutico responsável pela dispensação), `consulta` (`@ManyToOne Consulta`, opcional — nem
  toda dispensação nasce de uma consulta registrada no sistema; pode ser continuidade de tratamento
  com receita em papel), `quantidade` (`Integer`, obrigatório, `@Positive` — diferente de
  `Lote.quantidade`, que é `@PositiveOrZero`: dispensar zero não faz sentido), `dataHora`
  (`LocalDateTime`, obrigatório).
- **`Dispensacao` é create-only — sem PATCH.** Diferente de toda outra entidade da plataforma até
  agora (todas têm `criar`+`atualizar`). Uma dispensação é um registro histórico de uma transação já
  ocorrida (paralelo a um lançamento contábil): "editar" uma dispensação passada exigiria reverter e
  reaplicar o efeito no estoque do lote, potencialmente de um lote diferente — complexidade real sem
  nenhum requisito concreto pedindo correção retroativa. Erros se corrigem com uma nova dispensação
  ou (quando houver requisito real) uma futura entidade de estorno — não editando a entrada.
- **Criar uma `Dispensacao` decrementa `Lote.quantidade` como efeito colateral**, dentro da mesma
  operação: `DispensacaoService` busca o `Lote`, valida `lote.quantidade >= dto.quantidade` (senão
  `ResourceUnprocessableEntityException`, 422 — já existe e já tem precedente de uso em
  `AbstractHierarquicoService`, é o código certo pra "requisição válida mas que viola uma regra de
  negócio", diferente de 400 que é erro de validação de campo), subtrai e salva o `Lote`, depois
  salva a `Dispensacao`.
- **Sem extensão do Prontuário nesta fase** — `Dispensacao` não referencia `Atendimento`
  diretamente (só opcionalmente `Consulta`), e nem todo paciente com prontuário vai ter
  dispensações. Revisitar quando houver uma tela real que precise mostrar histórico de medicamentos
  dispensados dentro do prontuário.
- Sem `criado_em`/`atualizado_em` — mesma omissão do resto da plataforma.

## Trade-offs considerados

**Permitir `PATCH` como em toda outra entidade — rejeitada**
- ✅ Consistência com o resto da API.
- ❌ Editar uma dispensação já ocorrida exigiria reverter o efeito no lote antigo e reaplicar no
  lote (possivelmente diferente) da versão editada — uma classe de bug real (double-spend/estoque
  fantasma) sem nenhum requisito concreto pedindo correção retroativa. Histórico de transação deve
  ser imutável; correções viram novos registros.

**`quantidade` insuficiente retornar 400 (Bad Request) — rejeitada**
- ✅ Mais simples, um único tipo de erro de validação.
- ❌ 400 é semanticamente "a requisição está malformada" — mas o payload é válido, só viola uma
  regra de negócio (estoque insuficiente). 422 (`ResourceUnprocessableEntityException`, já existe na
  plataforma) é o código correto pra essa distinção, e evita confundir cliente de API entre "corrija
  o payload" e "essa operação não é possível agora".

**Estender o Prontuário para incluir Dispensacao agora — rejeitada**
- ✅ Visão mais completa do histórico do paciente.
- ❌ `Dispensacao` não tem FK obrigatória a `Atendimento` (só `Consulta`, opcional) — não se encaixa
  na mesma árvore Atendimento→Consulta→Procedimento sem uma decisão de design própria sobre como
  tratar dispensações sem consulta associada. Sem tela real pedindo isso ainda.

## Consequências

**Positivas**: terceira entidade do domínio Farmácia; primeira a de fato movimentar estoque —
`Lote.quantidade` deixa de ser só um número digitado manualmente e passa a refletir consumo real.

**Negativas / pendências**: sem mecanismo de estorno/correção — um erro de dispensação fica
registrado permanentemente (mitigação: nova dispensação ou ajuste manual de `Lote.quantidade` via
seu próprio PATCH). `MovimentacaoFarmacia`, `TransferenciaEntreUnidades`, `Perda`,
`InventarioFarmacia` continuam não modelados.

## Referências

- [`DER.md`](./DER.md), apêndice "Farmácia (#9)" — fluxo `Prescrição → Farmácia → Dispensação → Paciente`.
- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — domínio #9, posição no roadmap da plataforma.
- [ADR-0049](./0049-medicamento-primeira-entidade-da-farmacia.md) e
  [ADR-0050](./0050-lote-segunda-entidade-da-farmacia.md) — `Medicamento`/`Lote`, referenciados aqui.
- [ADR-0043](./0043-consulta-registrada-durante-o-atendimento.md) — `Consulta.receituario`, a
  "prescrição" que esta ADR referencia via FK opcional.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de FK
  por matrícula resolvida na fronteira da API.
