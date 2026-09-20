# 0024 — Ponto com filtro de período (fatia 6a)

## Status

Aceita e implementada.

## Contexto

Fatia 2 (ADR-0020) deixou "Ponto" deliberadamente fora: `GET .../profissional/{matricula}`
devolvia tudo, sem filtro, e uma lista crua de todos os registros de ponto de um profissional
(potencialmente anos de entradas/saídas) não agrega valor real. A decisão registrada foi esperar
a fatia 6, que resolve exatamente isso.

## Decisão

### Filtro por período, opcional, no mesmo endpoint

`GET /api/v1/registro-ponto/profissional/{matricula}?dataInicio=...&dataFim=...` — query params
opcionais, `@DateTimeFormat(iso = DATE)`. Sem os dois, comportamento idêntico ao de antes (lista
tudo) — não quebra nenhum uso existente. Com os dois, filtra `dataHora` entre
`dataInicio.atStartOfDay()` e `dataFim.atTime(LocalTime.MAX)` (inclusive nas duas pontas).

### Aba "Ponto" no perfil, agora que o filtro existe

Segue o mesmo padrão de aba do perfil (ADR-0020) — form de período (De/Até) + tabela. Sem form de
registro: lançar ponto manualmente por um formulário web não é o fluxo real (é tipicamente
automatizado — relógio de ponto, app, biometria), e não foi pedido; a aba é só de consulta.

## Trade-offs considerados

**Mesmo endpoint com filtro opcional (escolhida)**
- ✅ Sem endpoint novo, sem duplicar lógica de busca.
- ✅ 100% retrocompatível — nenhum outro consumidor do endpoint (nenhum existe ainda além desta
  tela) precisa mudar.

**Endpoint separado só pra período (rejeitada)**
- ✅ Assinatura mais "limpa" (sempre exige os dois parâmetros).
- ❌ Rejeitada: duplicaria a query, sem ganho real — o padrão de parâmetro opcional já é usado em
  outros pontos do projeto (ex.: `dataDesligamento` no `des-habilitar`, ADR-0017).

## Consequências

**Positivas**: fecha a primeira metade da fatia 6; a lista de ponto agora é utilizável (filtrada
por competência/período, não uma parede de registros).

**Negativas / pendências**: nenhum agrupamento "dia trabalhado" (entrada+saída consolidadas numa
linha) — mostra os registros crus, um por linha. Se isso for necessário, é uma evolução futura da
mesma aba, não um novo endpoint.

## Referências

- [ADR-0020](./0020-tela-central-do-profissional.md) — decisão original de deixar Ponto de fora
  até ter filtro.
