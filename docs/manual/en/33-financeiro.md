# Finance

> **Version:** 1.15.0 · [Português](../pt-BR/33-financeiro.md)

Menu: **Administrativo › Financeiro** (Finance). It follows the Municipal Health Fund **alongside SIAFIC** (the official public
accounting system). SIAFIC remains the official system; here you have:

- budget allocations and commitments, mirrored with the same number;
- liquidation (verification of the expense) and payment;
- payables carried over and execution;
- costs per unit, with cost allocation.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Finance | records allocations, commitments, liquidations and payments, and calculates costs |
| Managers and procurement | view |

Choose the **Exercício** (financial year) at the top. The screen has the tabs:

- **Dotações** (allocations) and **Empenhos** (commitments);
- **A liquidar** (to liquidate), **Liquidações** (liquidations) and **Restos a pagar** (payables carried over);
- **Execução** (execution) and **Custos** (costs).

## 1. Allocations

1. Click **Nova dotação** (new allocation).
2. Enter:
   - the **Programa de trabalho** (programme of work, for example *10.301.0010.2045*) and the **Natureza da despesa** (expense
     nature, for example *3.3.90.30*);
   - the **Descrição da ação** (action description), the **Fonte** (funding source) and the **Código da fonte** (source code);
   - the **Valor da lei (R$)** (amount in the budget law).
3. Click **Registrar** (record).

The allocation shows **autorizado** (authorised), **empenhado** (committed) and **disponível** (available). **Alterações**
(changes): the amount and the **Ato** (act, for example *Decreto 12/2026*), through **Registrar alteração** (record change).

## 2. Commitments

> **The commitment is issued in SIAFIC;** here it is recorded with the same number to follow execution.

1. Click **Registrar empenho** (record commitment).
2. Enter:
   - the **Número** (number, for example *2026NE000123*) and the **Data** (date);
   - the **Dotação**, the **Fornecedor** (supplier) and, if any, the **Ata ou contrato** (price register or contract);
   - the **Valor (R$)** (amount).
3. Click **Registrar**.

The commitment shows what is committed and the balance, the linked **Ordens de fornecimento** (supply orders, from Procurement)
and **Reforços e anulações** (top-ups and cancellations), through **Registrar movimento** (record movement), with amount and reason.

## 3. Liquidation

**A liquidar:** the attestations waiting for liquidation. Final receipt in Procurement becomes an attestation here.

1. On the attestation, click **Liquidar** (liquidate).
2. Check the **Empenho** and the **Nota fiscal** (invoice) and enter:
   - the **Valor bruto (R$)** (gross amount);
   - the withholdings: **ISS** (municipal service tax), **IRRF** (income tax), **INSS** (social security) and **Outras** (other).
3. The **Líquido** (net amount) is calculated. Enter the **Vencimento** (due date; empty means 30 days).

**Liquidação avulsa** (one-off liquidation): for an expense that did not go through receipt on the platform.

1. Use **Liquidação avulsa** and enter the **Empenho**, the invoice and the amounts.
2. Write the **Justificativa (sem atesto na plataforma)** (justification: no attestation on the platform) and the **Matrícula de
   quem atestou** (staff number of the person who attested).
3. Choose the **Centro de custo** (cost centre).

## 4. Payment

The **Liquidações** tab shows gross and net amounts, due date and status: **a pagar** (to pay) or **vencida** (overdue).

1. Click **Pagar** (pay) and enter the **Ordem bancária** (bank order) and the date.
2. **Chronological order:** if another liquidation fell due before this one, write the **Justificativa fora da ordem cronológica**
   (justification for paying out of chronological order).

## 5. Payables carried over and execution

- **Restos a pagar** (payables carried over): commitments from previous years:
  - **Processados** (processed: liquidated and not paid);
  - **Não processados** (not processed: committed and not liquidated).
- **Execução** (execution):
  - by source and by allocation: authorised, committed, liquidated and paid;
  - the **dias do atesto ao pagamento** (days from attestation to payment).

## 6. Out-of-area treatment (TFD) per diems

Per diems and expense allowances for Out-of-Area Treatment (TFD) are granted on the **Frota e transporte** (fleet and transport)
screen, on the authorised TFD request:

- one per diem per beneficiary;
- the amount is the days (1 to 30) times the municipality's daily rate.

They enter Finance's execution and the unit's cost.

## 7. Costs

**The accounting month:**

1. Choose the **Competência** (only months already ended) and calculate.
2. **Refazer a apuração** (recalculate) runs again while the month is open.
3. **Fechar a competência** (close the month) freezes the figures. **Once closed, the month no longer changes.**

**The cards:**

- the amount **sem centro** (without a centre: payroll without posting, older one-off liquidations);
- **saídas sem preço** (withdrawals without a price), outside the total;
- the **não rateado** (not allocated): support and administration without a destination in the allocation. An older calculation
  shows *apurada antes do rateio; refaça para ratear* (calculated before allocation; recalculate to allocate).

**By unit:**

- cost, encounters and cost **Por atendimento** (per encounter);
- patient-days and cost **Por paciente-dia** (per patient-day);
- the **acima da média** (above average) mark, for a unit 30% above its type's average.

In a mixed unit, the inpatient cost goes to patient-days, and the rest to encounters.

**By cost centre:** each centre's **Direto** (direct), **Recebido** (received), **Enviado** (sent) and **Final** cost.

**Rateio** (allocation): step-down, following the PNGC (national cost management programme):

- administrative centres, then support centres, send what they have to centres that have not yet allocated and to the unit's
  productive centres;
- the table shows the step, from, to, the criterion and the amount.

**Cost centres:**

1. Use **Novo centro do setor** (new sector centre): the **Setor** and the type (if not given, from the sector).
2. Choose the **Critério do rateio** (allocation criterion): staff, direct cost, area or requisitions. If not given, staff applies to
   administrative centres and direct cost to support centres. **Productive centres have no criterion.**
3. For the area criterion, enter the **Área (m²)**.

## 8. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| *Nenhum atesto à espera da liquidação.* — no attestation awaiting liquidation | there is no receipt to liquidate | nothing to do |
| Payment out of chronological order | an earlier liquidation is overdue | justify it or pay the oldest first |
| **sem centro** in the calculation | there is cost without a cost centre | link the posting to a sector or choose the centre in the one-off liquidation |
| *apurada antes do rateio; refaça para ratear* | the month was calculated before allocation existed | recalculate, if it is still open |
| *Nenhum custo na competência.* — no cost in the month | the month has not been calculated yet | calculate the month |
