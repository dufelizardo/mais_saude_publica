# Stock and stores

> **Version:** 1.15.0 · [Português](../pt-BR/30-estoque-e-almoxarifado.md)

Menu: **Administrativo › Estoque e almoxarifado** (stock and stores). These are each unit's general materials and supplies:
batches, entries, consumption, losses, requisitions to the central store, transfers, inventory and alerts.

> **Medicines are in the Pharmacy** (module 04); **vaccines, in the Vaccination room** (module 10).

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Storekeeper | records entries, consumption and losses, fulfils requisitions, transfers and takes inventory |
| Nursing coordination | requisitions from the central store and views |
| Manager | keeps the catalogue and minimum stock, and reserves for emergencies |
| Procurement and maintenance | view |

**The cards at the top:**

- **Materiais em estoque** (materials in stock, with balance or a minimum at this unit);
- **Abaixo do mínimo** (below minimum: order replenishment);
- **Vencendo em 90 dias** (expiring within 90 days);
- **Valor do estoque** (stock value), at entry cost.

## 1. Recording an entry

1. Click **Nova entrada** (new entry) and choose the **Material**.
2. Enter the **Quantidade** (quantity) and the **Origem** (origin): invoice, donation, opening balance or other.
3. If the material is batch-controlled, enter the **Lote** (batch) and **Validade** (expiry).
4. Optionally, the **Custo unitário (R$)** (unit cost) and the **Fornecedor** (supplier).
5. Save. Receiving a supply order in Procurement also records an entry here.

## 2. Stock by material

The list is by material, with its batches. **Withdrawals come from those expiring first.**

On each material:

- **Saída** (withdrawal): consumption, with the quantity, the **Setor que consumiu** (consuming sector) and the batch (optional;
  without a choice, those expiring first). During an emergency, choose **Consumo da emergência** (emergency consumption) to use its
  reserve.
- **Perda** (loss): the quantity and reason (expiry, damage, loss in transit or other).
- **Ajustar** (adjust): the **Quantidade contada** (counted quantity) and the justification.
- **Definir mínimo** (set minimum): the unit's **Estoque mínimo** (minimum stock).
- **Reservar para emergência** (reserve for emergency; manager):
  - choose the **Emergência**, the quantity and the reason (for example, *kits para os abrigos* — kits for the shelters);
  - while it is active, ordinary consumption, losses of valid batches, one-off transfers and requisitions outside the emergency do
    not take from the reserve;
  - demobilisation releases what is left, and **Liberar** (release) returns it earlier.

**Livro de movimentação** (movement ledger): the last 30 days, newest first. **Nothing is edited or deleted.** The types are:

- opening balance, entry and consumption withdrawal;
- loss and inventory adjustment;
- transfer (out, in and reversal).

## 3. Requisitions to the central store

The central store is the unit with the **Central de abastecimento** (supply centre) capability in the Operating model. Without it,
the screen warns.

**Requesting (unit):**

1. Click **Nova requisição** (new requisition) and choose the **Central**.
2. Choose the **Prioridade** (priority: normal, urgent or emergency).
3. Use **Incluir material** (add material) for each item, with the quantity.
4. Enter **Sua matrícula** (your staff number) and send.

**Fulfilling (central store):**

1. On the requisition, click **Atender** (fulfil).
2. The drawer shows the unit's balance, 30-day consumption and the central store's balance.
3. Enter the quantity for each item. Batches expiring first go out. **Zero leaves the item unfulfilled.**
4. The requisition can end up **atendida em parte** (partly fulfilled). Use **Negar** (deny) with the reason, if needed.

## 4. Transfers

Those leaving and arriving at this unit.

**Sending:**

1. Use **Transferir para outra unidade** (transfer to another unit).
2. Choose the **Destino** (destination) and add the **Lotes** (batches) with quantities.
3. Send. A fulfilled requisition also generates the transfer (**pela requisição** — via the requisition).

**Receiving (at the destination):**

1. Click **Receber** (receive) and check each item: **Enviado** (sent) and **Chegou** (arrived).
2. Enter the **Motivo da diferença** (reason for the difference). A shortfall goes back to the source; damage becomes a loss at the
   destination.

## 5. Inventory

1. Use **Abrir inventário** (open inventory). **Opening freezes the unit's batches with balance and stops movement** until it is
   closed or cancelled.
2. Count each batch and record the counted quantity. The list shows batches, those counted and those with differences.
3. Close the inventory. **Closing posts the adjustments.** Or cancel it, without adjustments.

## 6. Alerts

The **Alertas** (alerts) tab shows:

- what is below minimum;
- what expires within 90 days;
- what is expired with a balance;
- **consumption without balance:** supplies that a test collection or examination used without stock balance. Fix the entry or the
  inventory and mark it **Conferido** (checked).

## 7. Materials catalogue

The catalogue **applies to the whole network**.

1. Click **Novo material** (new material).
2. Enter:
   - the **Código** (code) and, if any, the **CATMAT** (federal materials catalogue code);
   - the **Nome** (for example, *Luva de procedimento M* — examination glove M);
   - the category: medical-hospital, PPE, dental, laboratory, cleaning and hygiene, office, linen or other;
   - the **Unidade de medida** (unit of measure: unit, box, pack, pair, roll, litre, bottle…).
3. Tick **Controla lote e validade** (batch and expiry controlled), if applicable. **This cannot change after the first entry.**
4. Leave **Ativo** (active) ticked.

## 8. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| *Nenhuma unidade tem a capacidade Central de abastecimento…* — no unit has the supply centre capability | no central store is registered | add the capability to the central store's Operating model |
| Movement refused | there is an open inventory at the unit | close or cancel the inventory |
| Withdrawal refused during an emergency | the quantity is reserved | use emergency consumption or release the reserve |
| **Sem saldo** (no balance) in Alerts | a test used supplies without balance | fix the entry or inventory and mark it checked |
