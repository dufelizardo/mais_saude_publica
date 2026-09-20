# 0022 — Endpoint calculado de composição remuneratória (fatia 4)

## Status

Aceita e implementada.

## Contexto

Fatia 4 do roadmap, a única das fatias de frontend com uma lacuna de backend real (identificada já
na primeira análise de gaps da fatia 1): a "Composição remuneratória" — base + anuênio + ajustes
individuais = total — nunca tinha sido implementada. `RegraAnuenio` guarda o percentual desde a
Fase 0, mas nenhum endpoint compunha o valor de verdade. Decisão já tomada antes de começar: um
endpoint de **leitura calculada** (`GET`, não grava nada) — não é a Folha de pagamento.

## Decisão

### Novo `ComposicaoRemuneratoriaController`/`Service`, não acoplado a nenhum dono existente

`GET /api/v1/composicao-remuneratoria/profissional/{matricula}`. Não colocado dentro de
`ProfissionalController` (que só cuida dos dados do próprio Profissional) nem de `LotacaoService`
(que só cuida de lotação) — a composição cruza 4 agregados (`Lotacao`, `TabelaSalarial`,
`RegraAnuenio`, `AjusteIndividual`) que nenhum dos serviços existentes é dono. Segue o mesmo
princípio já usado no resto do módulo: uma entidade/conceito, um serviço.

### Fórmula, exatamente como documentada em `MODELO-RH.md` seção 2.4/2.5

1. `Lotacao` vigente do profissional → `Cargo` → `CategoriaSalarial`.
2. `TabelaSalarial` vigente do cargo (reaproveita a query já existente
   `findFirstByCargo_UuidAndDataVigenciaLessThanEqualOrderByDataVigenciaDesc`).
3. Anuênio: se existir `RegraAnuenio` pra categoria, `anosCompletos = hoje − Profissional.dataAdmissao`
   (desta ficha, não soma tempo de fichas antigas — ADR-0017); `valorAnuenio =
   min(anosCompletos, tetoAnos) × percentualPorAno/100 × valorBase`. Sem regra cadastrada, anuênio
   é zero (não é erro — `RegraAnuenio` é opcional por categoria).
4. `AjusteIndividual` vigentes: filtrados em memória (`dataInicio <= hoje && (dataFim == null ||
   dataFim >= hoje)`) sobre a lista já existente — sem query nova, a lista por profissional é
   sempre pequena.
5. `total = valorBase + valorAnuenio + soma dos ajustes vigentes`.

### Sem lotação ou sem tabela salarial vigente → 404, não zero

Um profissional sem lotação vigente não tem cargo, logo não tem de onde derivar nada — retornar
"tudo zero" mascararia esse estado, que já é sinalizado em outro lugar (aba Lotação do perfil).
`ResourceNotFoundException`, mesmo padrão do resto da API.

### Frontend: nova aba "Composição remuneratória" no perfil, sem formulário

Só leitura — é a mesma tela central do Profissional (ADR-0020), mostrando o total em destaque e a
tabela de componentes (base, anuênio com o detalhe do cálculo, cada ajuste vigente). Recarregada
também depois de registrar uma transferência (aba Lotação, ADR-0021) ou um ajuste individual —
ambos afetam o valor calculado.

## Trade-offs considerados

**Calcular sob demanda a cada consulta (escolhida)**
- ✅ Sempre reflete o estado atual — sem risco de ficar desatualizado depois de uma transferência,
  reajuste de tabela ou novo ajuste individual.
- ❌ Recalcula em toda requisição (barato aqui — poucas queries indexadas por profissional; não
  seria adequado pra um relatório em massa de todos os profissionais, mas não é esse o caso de
  uso).

**Persistir a composição calculada numa tabela própria (rejeitada)**
- ✅ Mais rápido de consultar em volume.
- ❌ Rejeitada: duplicaria dado derivável, com risco real de ficar desatualizado (teria que
  recalcular em todo evento que afeta o valor: nova `TabelaSalarial`, nova `Lotacao`, novo/editado
  `AjusteIndividual`) — complexidade desproporcional pro caso de uso atual (consulta individual).

## Consequências

**Positivas**: fecha a única lacuna de backend que restava das fatias de tela já mapeadas; a
fórmula do anuênio, só descrita em texto desde a Fase 0, agora tem uma implementação real e
testada.

**Negativas / pendências**: nenhum endpoint "composição de todos os profissionais de uma vez" —
não era o caso de uso pedido; se um relatório em massa for necessário no futuro, precisa de desenho
próprio (provavelmente pré-calculado, diferente deste).

## Referências

- [MODELO-RH.md](../rh/MODELO-RH.md) seções 2.4-2.5 — fórmula original.
- [ADR-0020](./0020-tela-central-do-profissional.md) — tela onde a nova aba vive.
