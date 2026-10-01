# 0057 — Livro de movimentação do estoque da Farmácia (auditoria de lotes)

## Status

Aceita e implementada.

## Contexto

A Farmácia (domínio #9) tinha três entidades: `Medicamento` (ADR-0049), `Lote` (ADR-0050) e
`Dispensacao` (ADR-0051). A ADR-0050 manteve `Lote.quantidade` como um contador simples, sem livro
de movimentações, e registrou isso como simplificação consciente "a revisar quando
dispensação/transferência precisarem debitar dele". A pesquisa da ADR-0049 já apontava que o padrão
operacional do Hórus (sistema de referência da assistência farmacêutica do SUS) é movimentação de
entrada/saída com subtipos — dispensação, transferência, perda (vencimento, avaria) e inventário.

Revendo o código antes de continuar o domínio, apareceram quatro problemas de auditoria:

1. **Quantidade editável sem rastro.** O `PATCH /api/v1/lote/{uuid}` sobrescrevia `quantidade`
   livremente — a própria descrição do endpoint dizia que ele "cobre corrigir quantidade após
   dispensação ou perda". Remédio vencido, avaria ou diferença de inventário só se corrigiam editando
   o número, sem registro de quem, quando nem por quê.
2. **Medicamento e unidade editáveis.** O mesmo PATCH trocava o medicamento e a unidade de um lote
   existente. Trocar a unidade é, na prática, uma transferência sem registro.
3. **Venda dupla na dispensação.** A dispensação lia o saldo, conferia e gravava sem trava: duas
   dispensações simultâneas do mesmo lote podiam passar e deixar o saldo errado.
4. **Saídas sem registro.** A dispensação era a única saída com registro próprio; qualquer outra
   redução de estoque era invisível.

## Decisão

1. **`MovimentacaoFarmacia` é o livro de movimentação de cada lote.** Cada lançamento guarda:
   - tipo (`SALDO_INICIAL`, `ENTRADA`, `DISPENSACAO`, `PERDA`, `AJUSTE_INVENTARIO`), variação
     (positiva em entradas, negativa em saídas) e **saldo resultante** (`saldoApos`), o que permite
     reconstruir o histórico;
   - motivo da perda (`VENCIMENTO`, `AVARIA`, `EXTRAVIO`, `OUTRO` — este último exige justificativa)
     e justificativa (obrigatória no ajuste);
   - profissional responsável (por matrícula, como na dispensação) e o CPF do usuário autenticado
     quando o toggle de segurança está ligado (ADR-0055) — com o toggle desligado a requisição é
     anônima e o campo fica nulo;
   - hora do servidor (`registradoEm`), nunca informada pelo cliente;
   - a dispensação que originou o lançamento, quando é o caso.
2. **Lançamentos são imutáveis.** Não há edição nem exclusão pela API, e as colunas são
   `updatable = false`. Um lançamento errado se corrige com um ajuste de inventário. O livro também
   não é apagado em cascata junto com o lote — o histórico de estoque não pode sumir.
3. **`Lote.quantidade` só muda pelo livro.** `MovimentacaoFarmaciaService.lancar` é o único ponto que
   altera o saldo:
   - criar um lote o grava com saldo zero e lança a `ENTRADA` com a quantidade recebida (responsável
     opcional, `profissionalMatricula`, para não quebrar quem já cadastra lotes);
   - a dispensação lança a saída `DISPENSACAO`; estoque insuficiente responde 422 e desfaz a
     transação inteira, dispensação incluída;
   - toda movimentação busca o lote com trava de linha (`SELECT ... FOR UPDATE`,
     `LoteRepository.findByIdParaMovimentar`), o que fecha a venda dupla.
4. **A edição de lote só corrige número do lote e validade** (`LoteAtualizacaoRequestDto`). Quantidade,
   medicamento e unidade saíram da edição; campos extras no corpo são ignorados.
5. **Perda e ajuste entram pela API** (`POST /api/v1/movimentacao-farmacia/`). O ajuste recebe o
   **saldo contado** na conferência física e o sistema calcula a diferença, como num inventário real.
   Entrada e dispensação não podem ser lançadas à mão (400). Saída que deixaria o saldo negativo é
   recusada (422).
6. **Extrato por lote** (`GET /api/v1/movimentacao-farmacia/lote/{loteId}`), do lançamento mais antigo
   ao mais recente, e busca por id.
7. **Lotes anteriores ao livro** ganham, na subida da aplicação, um lançamento `SALDO_INICIAL` com a
   quantidade que já tinham (`SaldoInicialFarmaciaBackfill`, idempotente), para o extrato de cada
   lote começar de um saldo conhecido.

## Trade-offs considerados

**Livro de movimentação com saldo guardado no lote (escolhida)**
- ✅ Auditável (quem, quando, por quê, saldo resultante) sem abrir mão de ler o saldo direto do lote.
- ✅ Um único ponto de alteração do saldo, com trava de linha — resolve a venda dupla.
- ❌ O saldo existe em dois lugares (lote e último `saldoApos`); a consistência depende de ninguém
  alterar `Lote.quantidade` fora do serviço. Mitigado por esse ser o único caminho no código.

**Calcular o saldo sempre a partir do livro (rejeitada)**
- ✅ Uma única fonte da verdade.
- ❌ Toda leitura de lote somaria o histórico inteiro, e a trava contra venda dupla ficaria mais
  difícil (não há uma linha para travar). Pode ser revisto se o volume justificar.

**Uma entidade por tipo de operação (`Perda`, `InventarioFarmacia`, ...) mexendo no lote cada uma
(rejeitada)**
- ❌ Espalha a alteração do saldo por vários serviços e não dá um extrato único por lote — o
  problema que esta ADR resolve.

**Manter a quantidade editável no PATCH do lote (rejeitada)**
- ❌ É exatamente o caminho sem rastro que motivou a decisão.

## Consequências

**Positivas:** toda alteração de estoque fica rastreável e imutável; perda e divergência de
inventário passam a ter registro com motivo e responsável; a venda dupla deixa de ser possível; a
transferência entre unidades (próxima fatia de backend) nasce em cima do mesmo livro, como dois
lançamentos ligados.

**Negativas / pendências:**
- **Mudança de contrato no `PATCH /api/v1/lote/{uuid}`:** deixa de aceitar quantidade, medicamento e
  unidade. Nenhuma tela usava a edição; os testes JUnit e Robot foram ajustados.
- **Responsável na entrada é opcional**, para não quebrar quem já cadastra lotes. Quando as telas da
  Farmácia existirem, vale torná-lo obrigatório.
- **Transferência entre unidades** e **telas da Farmácia** ainda não existem — ordem combinada em
  [`ESCOPO-FARMACIA.md`](../farmacia/ESCOPO-FARMACIA.md).
- O `registradoPorCpf` só é preenchido com o toggle de segurança ligado; nos ambientes com o toggle
  desligado, o responsável registrado é só o profissional informado.

## Referências

- [ADR-0049](./0049-medicamento-primeira-entidade-da-farmacia.md) — pesquisa sobre o Hórus e o padrão
  de movimentação de estoque farmacêutico.
- [ADR-0050](./0050-lote-segunda-entidade-da-farmacia.md) — `Lote.quantidade` como contador simples,
  simplificação revista por esta ADR.
- [ADR-0051](./0051-dispensacao-terceira-entidade-da-farmacia.md) — dispensação, que passa a lançar no
  livro.
- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md) — toggle de
  segurança e usuário autenticado, origem do `registradoPorCpf`.
- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md) — auditoria transversal
  (`EventoAuditoria`), ainda não implementada; o livro é a auditoria específica do estoque.
