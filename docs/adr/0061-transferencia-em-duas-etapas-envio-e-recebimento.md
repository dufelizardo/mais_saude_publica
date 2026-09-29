# 0061 — Transferência em duas etapas: envio, trânsito e recebimento conferido

## Status

Aceita e implementada. Revê a decisão "transferência imediata" da
[ADR-0059](./0059-transferencia-de-estoque-entre-unidades.md).

## Contexto

A ADR-0059 fez a transferência entre unidades numa etapa só: a saída na origem e a entrada no
destino eram gravadas juntas, por quem enviava. Na revisão, o usuário levantou que não existir
"em trânsito" é um risco de auditoria. A análise confirmou três falhas:

1. **Quem enviava registrava a entrada no destino em nome de quem recebia.** O profissional gravado
   nos dois lançamentos era o mesmo; ninguém do destino confirmava nada.
2. **Perda no transporte ficava invisível.** Se saíam 30 e chegavam 27, o destino ficava com 30 no
   sistema; as 3 unidades nunca apareciam, e não havia como saber se sumiram no envio, no caminho ou
   no recebimento.
3. **Não existia o estado "saiu daqui e ainda não chegou lá"**, então o estoque em trânsito não era
   visível para ninguém.

## Decisão

1. **A transferência tem status:** `EM_TRANSITO` → `RECEBIDA` | `RECEBIDA_COM_DIVERGENCIA`, ou
   `EM_TRANSITO` → `CANCELADA`. Nenhuma outra transição existe; receber ou cancelar fora de
   `EM_TRANSITO` responde 422.
2. **Envio** (`POST /api/v1/transferencia-farmacia/`): lança `TRANSFERENCIA_SAIDA` no lote de origem
   e grava a unidade de destino. **Nada entra no destino ainda.** Regras da ADR-0059 mantidas:
   mesma unidade → 400, lote vencido → 422, saldo insuficiente → 422 sem gravar nada.
3. **Recebimento conferido** (`POST /api/v1/transferencia-farmacia/{id}/recebimento`), com a
   quantidade que efetivamente chegou:
   - lança `TRANSFERENCIA_ENTRADA` **só com o que chegou**, no lote da mesma remessa na unidade de
     destino (criado se ainda não existir — ADR-0060), em nome de **quem conferiu**;
   - **precisa ser outro profissional**, não quem enviou (422) — segregação de funções;
   - receber mais do que foi enviado → 422;
   - chegou menos → **divergência**: `motivoDivergencia` (`AVARIA`, `EXTRAVIO`, `OUTRO`) e
     `justificativaDivergencia` obrigatórios (400 sem eles); a transferência fica
     `RECEBIDA_COM_DIVERGENCIA` e guarda a quantidade divergente. Chegar zero é divergência total.
4. **Cancelamento** (`POST /api/v1/transferencia-farmacia/{id}/cancelamento`), só em trânsito:
   `TRANSFERENCIA_ESTORNO` devolve a quantidade ao lote de origem; motivo e profissional obrigatórios.
5. **Cada etapa guarda quem, quando e o CPF do usuário autenticado** (envio, recebimento,
   cancelamento), e não muda depois de gravada.
6. **Trava da transferência** (`SELECT ... FOR UPDATE`) antes de receber ou cancelar: impede receber
   duas vezes ou receber e cancelar ao mesmo tempo. Ordem das travas em todo o estoque:
   transferência → unidade → lotes.
7. **Listagem filtrável** por `status`, `unidadeOrigemId` e `unidadeDestinoId` — por exemplo, "o que
   está a caminho desta unidade" (`?status=EM_TRANSITO&unidadeDestinoId=...`).
8. **Transferências imediatas já feitas** (ADR-0059) viram `RECEBIDA` na subida, com os dados do
   próprio envio — elas já tinham lançado a entrada no destino. A mesma rotina libera
   `lote_destino_id` para ficar vazio, porque o Hibernate não relaxa uma coluna que nasceu obrigatória.

## Trade-offs considerados

**Divergência registrada na transferência (escolhida)** × **lançar a diferença como perda no livro**
- ✅ O livro continua registrando só movimentos físicos de cada lote: saiu 30 da origem, entrou 27 no
  destino. A diferença não está em nenhum dos dois lotes — está no caminho — e fica registrada onde
  aconteceu, na transferência, com motivo, justificativa e responsável.
- ✅ "Perdas em trânsito" são consultáveis pela listagem (`status=RECEBIDA_COM_DIVERGENCIA`).
- ❌ Um relatório de perdas precisa somar duas fontes: perdas do livro e divergências de transferência.
- Lançar como perda exigiria dar entrada de 30 no destino e perda de 3 — registrando no destino uma
  entrada que fisicamente não aconteceu.

**Exigir outro profissional no recebimento (escolhida)** × **qualquer profissional**
- ✅ Sem isso, o envio em duas etapas não resolve a falha 1: a mesma pessoa poderia enviar e confirmar.
- ❌ Ainda não garante que quem recebe é **da unidade de destino** — isso depende do escopo de acesso
  por unidade (ADR-0054), que não existe ainda. Fica como pendência.

**Cancelar só em trânsito (escolhida)** × **permitir desfazer um recebimento**
- ✅ Depois de recebido, o medicamento está no destino; devolvê-lo é outra transferência, no sentido
  contrário, com o próprio registro.

## Consequências

**Positivas:** toda unidade que sai tem destino rastreável — recebida, divergente com motivo ou
estornada —, com responsáveis diferentes para envio e recebimento e o estoque em trânsito visível.

**Negativas / pendências:**
- **Contrato alterado:** o `POST` de transferência deixa de dar entrada no destino; quem integrava com
  ele precisa chamar o recebimento. Nenhuma tela usava ainda.
- Conferir que o recebedor pertence à unidade de destino depende do escopo por unidade (ADR-0054).
- Não há alerta para transferência parada em trânsito por muito tempo; a listagem filtrada permite
  acompanhar, e um alerta pode vir com o módulo de notificações.
- A gaveta de transferência na tela da Farmácia (envio, "a receber" e conferência) é a próxima fatia.

## Referências

- [ADR-0059](./0059-transferencia-de-estoque-entre-unidades.md) — transferência imediata, revista aqui.
- [ADR-0060](./0060-uma-remessa-um-lote-por-unidade.md) — lote da mesma remessa no destino, com trava da unidade.
- [ADR-0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) — livro de movimentação.
- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md) — escopo de acesso por unidade, ainda não implementado.
