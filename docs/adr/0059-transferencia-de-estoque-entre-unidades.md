# 0059 — Transferência de estoque da Farmácia entre unidades

## Status

Aceita e implementada.

## Contexto

Na rede municipal, uma unidade com sobra de um medicamento remaneja parte do estoque para outra que
está em falta. Até aqui isso não tinha registro: a ADR-0057 tirou a troca de unidade da edição do
lote justamente porque era "uma transferência sem registro", e deixou a transferência entre
unidades como próxima fatia, "como dois lançamentos ligados no livro, numa transação só".

O que precisava de decisão:

1. Para qual lote a quantidade vai no destino — o sistema não tem o conceito de "o mesmo lote em
   duas unidades": cada `Lote` pertence a uma unidade (ADR-0050).
2. Se a transferência é imediata ou em duas etapas (envio e recebimento), como no Hórus.
3. O que fazer com lote vencido.
4. Como travar os dois lotes sem que transferências cruzadas fiquem esperando uma pela outra.

## Decisão

1. **`TransferenciaFarmacia` registra a operação** (lote de origem, lote de destino, quantidade,
   profissional responsável, observação opcional, hora do servidor e CPF do usuário autenticado).
   Ela não mexe no saldo: gera **dois lançamentos no livro** — `TRANSFERENCIA_SAIDA` no lote de
   origem e `TRANSFERENCIA_ENTRADA` no lote de destino —, os dois ligados a ela
   (`MovimentacaoFarmacia.transferencia`) e gravados na mesma transação. É imutável, como o livro.
2. **O lote de destino é o da mesma remessa na unidade de destino** — mesmo medicamento, mesmo
   número de lote e mesma validade. Se ele ainda não existe, é criado com saldo zero e recebe a
   entrada. Assim a rastreabilidade do número de lote (recall) segue o medicamento de uma unidade
   para a outra, e o extrato de cada lote continua contando só a história daquela unidade.
3. **Transferência imediata, numa etapa.** A saída e a entrada acontecem juntas; não existe o
   estado "em trânsito".
4. **Regras:**
   - destino igual à unidade do lote → 400;
   - lote vencido → 422 ("registre a perda por vencimento") — medicamento vencido não circula na rede;
   - quantidade maior que o saldo → 422, e nada é gravado (nem a transferência, nem o lote de destino);
   - lote, unidade de destino ou profissional inexistente → 404.
5. **Travas em ordem fixa.** O id do lote de destino é descoberto antes, por uma consulta que devolve
   só o id (sem carregar a entidade), e os dois lotes são travados (`SELECT ... FOR UPDATE`) sempre
   em ordem crescente de id. Duas transferências cruzadas da mesma remessa (A→B e B→A) disputam as
   travas na mesma ordem, então uma espera a outra em vez de as duas se bloquearem.
6. **Endpoints** em `/api/v1/transferencia-farmacia/`: `POST` (transferir), `GET` (lista, da mais
   recente para a mais antiga) e `GET {uuid}`. O extrato do livro passa a trazer `transferenciaId`.
7. **Tela:** o extrato mostra os dois tipos novos ("Transferência enviada" / "Transferência
   recebida"). A gaveta para registrar uma transferência pela tela fica para a próxima fatia.

## Trade-offs considerados

**Lote da mesma remessa no destino (escolhida)** × **mover o lote inteiro de unidade** × **um lote
com saldo por unidade**
- ✅ Mantém o modelo atual (lote pertence a uma unidade) e a rastreabilidade por número de lote.
- ❌ A mesma remessa passa a existir como vários `Lote`, um por unidade. É o comportamento esperado
  para estoque por unidade; um relatório "onde está a remessa X" agrupa por número de lote.
- Mover o lote inteiro não serve para transferência parcial, que é o caso comum. Um lote com saldo
  por unidade mudaria o modelo de todas as fatias anteriores.

**Imediata (escolhida)** × **duas etapas (envio + recebimento)**
- ✅ Simples e suficiente para remanejamento dentro da mesma rede municipal, onde o transporte é
  curto e o mesmo sistema atende as duas pontas.
- ❌ Não representa o medicamento "em trânsito" nem divergência na conferência de recebimento. Se isso
  virar requisito, a transferência ganha um status e a entrada no destino passa a acontecer na
  confirmação — o livro e os tipos de lançamento continuam os mesmos.

**Bloquear lote vencido (escolhida)** × **permitir**
- ✅ Medicamento vencido tem destino próprio (perda por vencimento, ADR-0057); transferir só espalha o
  problema pela rede.

## Consequências

**Positivas:** o remanejamento entre unidades passa a ter registro completo — de onde, para onde,
quanto, quem e quando —, e os dois extratos contam a mesma história.

**Negativas / pendências:**
- **Gaveta de transferência na tela da Farmácia** ainda não existe; por enquanto a transferência é
  feita pela API.
- Duas transferências simultâneas para uma unidade que ainda não tem a remessa podem criar dois lotes
  de destino da mesma remessa (não há restrição de unicidade no banco). É raro e não perde saldo; se
  aparecer, uma restrição única em (medicamento, unidade, número, validade) resolve.
- Transferência em duas etapas, se virar requisito (ver trade-offs).

## Referências

- [ADR-0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) — livro de movimentação, que
  recebe os dois lançamentos da transferência.
- [ADR-0050](./0050-lote-segunda-entidade-da-farmacia.md) — lote por unidade.
- [ADR-0049](./0049-medicamento-primeira-entidade-da-farmacia.md) — pesquisa sobre o Hórus
  (movimentação com subtipos, incluindo transferência).
- [ADR-0058](./0058-tela-da-farmacia-abas-gaveta-lateral-e-livro-de-estoque.md) — tela da Farmácia.
