# 0050 — Lote: segunda entidade do domínio Farmácia

## Status

Aceita e implementada.

## Contexto

`Medicamento` (ADR-0049) já está implementado — catálogo raiz do domínio Farmácia, sem FK. O
`DER.md`/`MAPA-DE-DOMINIOS.md` listam `Lote` como o segundo candidato do domínio, pré-requisito de
`Dispensacao` e `MovimentacaoFarmacia` (nenhuma das duas faz sentido sem um lote físico pra
referenciar).

A pesquisa já feita para a ADR-0049 (sistema Hórus, referência nacional de gestão farmacêutica do
SUS) já estabeleceu que lote e validade são exigidos desde a entrada do medicamento em estoque, e
que o controle é rastreado por unidade (farmácia/almoxarifado) — não existe um "estoque único" da
rede inteira. Isso bate com o próprio desenho já implementado da plataforma: `UnidadeDeSaude` é a
entidade central de localização física (ver ADR-0002/0009/0013), e `Atendimento` já referencia
`unidadeId` da mesma forma que `Lote` precisaria fazer.

## Decisão

- Nova entidade `Lote`: `uuid`, `medicamento` (`@ManyToOne Medicamento`, obrigatório, FK direta por
  uuid — `Medicamento` não tem identificador externo tipo matrícula, então é referenciado
  diretamente, mesmo padrão de `Atendimento.unidade`), `unidade` (`@ManyToOne UnidadeDeSaude`,
  obrigatório, FK direta por uuid — mesmo padrão de `Atendimento.unidade`), `numeroLote` (`String`,
  obrigatório — o código impresso na embalagem pelo fabricante, usado pra rastreabilidade em caso de
  recall; diferente do `uuid` interno), `validade` (`LocalDate`, obrigatório), `quantidade`
  (`Integer`, obrigatório, `@PositiveOrZero`).
- **`Lote` referencia duas FKs — `Medicamento` (o quê) e `UnidadeDeSaude` (onde)** — porque a mesma
  remessa de um medicamento pode estar fisicamente em unidades diferentes, cada uma com sua própria
  validade/quantidade.
- **`quantidade` é um contador simples, editável via PATCH** — não uma soma calculada a partir de um
  ledger de movimentações. `MovimentacaoFarmacia` (entrada/saída) ainda não existe; quando existir,
  pode se tornar a fonte de verdade e `quantidade` vira um campo calculado — decisão a revisitar
  naquela fase, não agora.
- **Sem campo de status (`ATIVO`/`VENCIDO`/`ESGOTADO`)** — vencimento e esgotamento são derivados
  (`validade < hoje`, `quantidade == 0`), não armazenados; evita duplicar estado que já pode ser
  calculado a partir dos campos existentes.
- CRUD no mesmo formato de `Triagem`/`Consulta` (duas FKs resolvidas por uuid direto):
  `criar`/`atualizar` substituem os campos editáveis por inteiro, `listar`/`buscarPorId` sem filtro.
- `LoteRepository` ganha `findByMedicamentoUuid` — útil pra uma futura tela "estoque deste
  medicamento por unidade", mesmo raciocínio de `findByAtendimentoUuid` em `ConsultaRepository`.
- Sem `criado_em`/`atualizado_em` — mesma omissão do resto da plataforma.

## Trade-offs considerados

**Uma FK só (`Medicamento`), sem `UnidadeDeSaude` — rejeitada**
- ✅ Modelo mais simples, menos um relacionamento pra resolver.
- ❌ Não reflete a prática real (Hórus rastreia estoque por unidade) nem o desenho já implementado
  da plataforma, onde `UnidadeDeSaude` é a entidade central de localização física. Sem essa FK,
  `Lote` implicaria um estoque único pra rede inteira, o que não é verdade.

**`quantidade` como campo calculado a partir de `MovimentacaoFarmacia` desde já — rejeitada**
- ✅ Fonte de verdade única, sem risco de divergência entre lançamentos e saldo.
- ❌ `MovimentacaoFarmacia` ainda não existe; construir o ledger agora antecipa uma entidade sem
  consumidor real. `quantidade` como contador simples resolve o caso de uso atual (registrar o que
  entrou), revisitar quando dispensação/transferência precisarem debitar dele.

**Campo de status (`ATIVO`/`VENCIDO`/`ESGOTADO`) armazenado — rejeitada**
- ✅ Consulta mais barata (não precisa comparar datas/quantidade toda vez).
- ❌ Estado duplicado que pode ficar dessincronizado (ex.: lote com `validade` no passado mas
  `status` ainda `ATIVO` se ninguém rodar uma rotina de atualização). Derivar na leitura evita essa
  classe de bug — mesmo raciocínio já aplicado a `ClassificacaoRisco` como campo simples em
  `Triagem` (não uma entidade), e a `Prontuário` como agregação sob demanda (ADR-0045).

## Consequências

**Positivas**: segunda entidade do domínio Farmácia; junto com `Medicamento`, já permite responder
"quanto tem, de quê, vencendo quando, em qual unidade" — a base de qualquer relatório de estoque
farmacêutico futuro.

**Negativas / pendências**: `Dispensacao`, `MovimentacaoFarmacia`, `TransferenciaEntreUnidades`,
`Perda`, `InventarioFarmacia` continuam não modelados. `quantidade` como contador simples (não
ledger) é uma simplificação consciente que pode precisar de revisão quando dispensação/transferência
forem implementadas. **Colisão de nome a observar**: o domínio Estoque e Almoxarifado (#13, ainda não
iniciado) também lista um candidato `Lote` próprio no `MAPA-DE-DOMINIOS.md` — são conceitos
deliberadamente separados (ADR-0032/ADR-0049: medicamento tem regra própria, diferente de material
de almoxarifado), mas a classe Java `Lote` já está ocupada por este domínio; quando Estoque for
implementado, seu lote precisará de outro nome (ex.: `LoteEstoque`).

## Referências

- [`DER.md`](./DER.md), apêndice "Farmácia (#9)" — esboço original.
- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — domínio #9, posição no roadmap da plataforma.
- [ADR-0049](./0049-medicamento-primeira-entidade-da-farmacia.md) — `Medicamento`, referenciado
  aqui; pesquisa sobre Hórus e movimentação de estoque farmacêutico, base desta decisão.
- [ADR-0041](./0041-atendimento-registra-entrada-do-paciente-na-rede.md) — precedente de FK direta
  a `UnidadeDeSaude` por uuid (`Atendimento.unidade`).
