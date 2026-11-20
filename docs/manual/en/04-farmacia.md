# Pharmacy

> **Version:** 1.15.0 · [Português](../pt-BR/04-farmacia.md)

Menu: **Assistência › Farmácia** (Care › Pharmacy). It controls the medicine stock by batch at each unit, dispensing to the
patient, transfers between units and the stock ledger, where every movement is recorded.

> **The balance is kept by the stock ledger.** Nothing is deleted: entry, dispensing, loss, adjustment and transfer become ledger
> lines. To correct a balance, record a loss or an inventory adjustment.

**Who uses it:** the pharmacist, for everything. Managers can view it. The screen has five tabs:

- **Estoque por lote** (stock by batch);
- **Dispensações** (dispensing);
- **Transferências** (transfers);
- **Medicamentos** (medicines);
- **Livro de estoque** (stock ledger).

**The cards at the top:** **Medicamentos ativos** (active medicines), **Lotes com saldo** (batches with balance), those that
**Vencem em até 90 dias** (expire within 90 days) and **Dispensações hoje** (dispensing today).

## 1. Registering a medicine

**What it is for:** the catalogue used in batch entries.

1. Click **Novo medicamento** (new medicine).
2. Fill in:
   - the **Nome** (name, for example *Dipirona 500mg*);
   - and, optionally, the **Princípio ativo** (active ingredient, *Dipirona sódica*), the **Apresentação** (form, *Comprimido* —
     tablet) and the **Código** (code).
3. Save (*Medicamento cadastrado*).

On the **Medicamentos** tab, the pencil edits it. An **inactive** medicine does not appear in new batch entries.

## 2. Recording a batch entry

1. Click **Entrada de lote** (batch entry).
2. Choose the **Medicamento** and the **Unidade** (unit).
3. Enter the **Número do lote** (batch number, for example *L2026-01*), the **Validade** (expiry date) and the **Quantidade**
   (quantity, may be zero).
4. Optionally, the **Matrícula do responsável** (staff number of the person responsible) for the entry.
5. Save (*Entrada de lote registrada*). The entry goes to the stock ledger by itself.

> **Same batch, same expiry date, same unit:** the entry adds to the existing batch instead of creating another. An expired
> batch does not enter the stock.

## 3. Viewing the stock

On the **Estoque por lote** tab, each row shows the medicine, the batch, the unit, the expiry date and the balance.

- Search by **medicine, active ingredient or batch** and filter by unit or by **Vence em até 90 dias**.
- The actions for each batch are:

  | Action | What it does |
  |--------|--------------|
  | **Dispensar** (dispense) | hands it to the patient (item 4) |
  | **Transferir para outra unidade** (transfer to another unit) | item 6 |
  | **Perda ou ajuste** (loss or adjustment) | item 5 |
  | **Extrato do lote** (batch statement) | opens the batch in the **Livro de estoque** |
  | **Corrigir lote** (correct batch) | corrects only the batch number and the expiry date |

> **Corrigir lote** does not change the balance. For the balance, use **Perda ou ajuste**.

## 4. Dispensing to the patient

**What it is for:** recording that the medicine was handed to the patient. **Each dispensing takes the batch down in the stock
ledger.**

1. Click **Dispensar** at the top or on the batch row.
2. Choose the **Lote**. Only batches with balance appear, and the drawer shows the available balance. If the batch is expired,
   the drawer warns and suggests recording a loss for expiry.
3. Under **Paciente**, type part of the name or CPF to filter and choose the person.
4. Enter the **Quantidade** (greater than zero), the **Data e hora** and the **Matrícula do profissional**.
5. If there is one, the **ID da consulta** (consultation ID), which links the dispensing to the source consultation.
6. Click to record (*Dispensação registrada*).

> **Dispensing cannot be edited or deleted.** Check the batch, the patient and the quantity before recording.

The **Dispensações** tab lists them all, with search by patient, medicine or batch. The **Consulta** column says whether the
dispensing is **Vinculada** (linked) or **Sem consulta** (no consultation).

## 5. Recording a loss or inventory adjustment

1. On the batch row, click **Perda ou ajuste**, or use **Registrar perda ou ajuste** in the Livro de estoque.
2. Choose the **Tipo de registro** (record type):
   - **Perda** (loss):
     - the **Quantidade perdida** (quantity lost);
     - the **Motivo da perda** (reason): expiry, damage, loss in transit or other.
   - **Ajuste de inventário** (inventory adjustment):
     - the **Saldo contado fisicamente** (physically counted balance);
     - the system works out the difference from the ledger and shows *No livro* (in the ledger), *Contado* (counted) and
       *Diferença* (difference).
3. Write the **Justificativa** (justification). It is required for adjustments and for losses with the reason *Outro* (other).
4. Save (*Perda registrada no livro* or *Ajuste de inventário registrado*).

## 6. Transferring to another unit

**How it works:** a transfer has two steps. The quantity **leaves the source batch now** and stays **in transit** until another
professional checks the receipt at the destination.

**Sending:**

1. On the batch row, click **Transferir para outra unidade**, or use **Nova transferência** (new transfer) on the
   **Transferências** tab.
2. Choose the **Lote de origem** (source batch). Only batches with balance and within the expiry date appear.
3. Choose the **Unidade de destino** (destination unit, different from the source) and enter the **Quantidade**.
4. Enter the **Matrícula de quem envia** (staff number of the sender) and, optionally, an **Observação** (for example,
   *remanejamento para cobrir falta* — reallocation to cover a shortage).
5. Send. The transfer is **Em trânsito** (in transit).

**Checking the receipt (at the destination):**

1. On the **Transferências** tab, click **Conferir recebimento** (check receipt).
2. Under **Quantidade que chegou** (quantity received), count what arrived, not what the delivery note says.
3. Enter the **Matrícula de quem conferiu** (staff number of the checker). It must be another professional, not the sender.
4. If less arrived, enter:
   - the **Motivo da divergência** (discrepancy reason): damage, loss in transit or other;
   - **O que aconteceu** (what happened; for example, *3 frascos quebrados na caixa* — 3 bottles broken in the box).
5. Save: *Recebimento registrado* or *Recebimento registrado com divergência* (received with a discrepancy). The batch enters the
   destination.

**Cancelling:** if the medicine did not leave or came back to the source, use **Cancelar transferência** (cancel transfer), with
the reason. The quantity goes back to the source batch, and the transfer can no longer be received.

**Statuses:** in transit, received, received with discrepancy or cancelled. The tab filters separate them by status and by
destination unit.

## 7. The stock ledger

**What it is for:** the statement of a batch, from the oldest record to the newest.

1. Choose the batch (or use **Extrato do lote** on the stock tab).
2. Each line shows the date, the movement type, the detail, the person responsible and the quantity:

   | Type | When it appears |
   |------|-----------------|
   | Saldo inicial (opening balance) | the balance before the ledger existed |
   | Entrada (entry) | the batch entry |
   | Dispensação (dispensing) | the hand-over to the patient |
   | Perda / Ajuste de inventário | the records in item 5 |
   | Transferência enviada, recebida or cancelada | item 6 |
   | Administração ao paciente / retificada (administration / amended) | the medication check in the encounter |
   | Incorporado a outro lote / Lote duplicado incorporado (merged batch) | the same delivery registered twice at the unit, unified |

> **Immutable record.** To correct the balance, record an inventory adjustment.

## 8. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Informe o nome do medicamento.* — enter the medicine name | the record has no name | write the name |
| *Informe o número do lote.* / *Informe a validade.* — enter the batch number / expiry date | the entry is incomplete | fill in batch and expiry |
| *Use um número inteiro igual ou maior que zero.* — use a whole number, zero or more | the entry quantity is invalid | use a whole number, zero or more |
| *O lote já está vencido; não entra no estoque.* — the batch is already expired | the expiry date has passed | check the date; expired batches do not enter |
| *A quantidade precisa ser maior que zero.* — the quantity must be greater than zero | the dispensing has no quantity | enter how many units left |
| *Com motivo “Outro”, a justificativa é obrigatória.* — with "other", the justification is required | the loss with *Outro* needs the why | write the justification |
| *A justificativa é obrigatória no ajuste.* — justification is required for adjustments | the adjustment has no justification | explain the difference found |
| *Informe o saldo contado (0 ou mais).* — enter the counted balance | the adjustment has no count | enter what was counted on the shelf |
| *A unidade de destino precisa ser diferente da unidade do lote de origem.* — the destination must differ from the source | the destination is the same unit | choose another unit |
| *O recebimento precisa ser conferido por outro profissional, não por quem enviou.* — receipt must be checked by someone else | the sender tried to check it | another professional checks it |
| *Descreva o que aconteceu com o que não chegou.* — describe what happened to what did not arrive | there is a discrepancy without explanation | describe the damage or loss |
| *Esta transferência é de unidades fora do seu acesso.* — this transfer is between units outside your access | your profile covers neither source nor destination | ask for the profile, if your role needs it |
