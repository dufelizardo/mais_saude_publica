# Vaccination room

> **Version:** 1.15.0 · [Português](../pt-BR/10-sala-de-vacina.md)

Menu: **Assistência › Sala de Vacina** (Care › Vaccination room). It brings together:

- recording doses;
- overdue people for active search and vaccination coverage;
- vaccine stock and reallocations;
- the cold chain and batch alerts;
- the vaccination schedule.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Nurses, nursing technicians and nursing coordination | record doses, look after stock and the cold chain |
| Immunisation management: managers, nursing coordination and epidemiological surveillance | the schedule and targets, the cold rooms, assessing suspect batches and the alerts |

**To start:** choose the **Unidade** (unit). Without it, the screen asks for the unit to show the day's doses, overdue people,
stock and cold chain.

## 1. Recording a dose

1. Click **Registrar vacinação** (record vaccination). It can also be done from the patient's card (module 01, Vaccination tab).
2. Under **Paciente**, search by name, CPF or SUS card.
3. Choose **Aplicada aqui** (given here).
4. Check the **Unidade** and the **Data e hora** (date and time; empty means now).
5. Choose the **Vacina** (vaccine) and the **Dose**, and enter **Quem aplicou (matrícula)** (staff number of who gave it).
6. Choose the **Lote do estoque** (stock batch) the dose came from. If there is no applicable batch of the vaccine in the unit's
   stock, the drawer warns.
7. Optionally:
   - the **Via** (route) and **Local de aplicação** (site, for example right or left deltoid);
   - the **Estratégia** (strategy): routine, campaign, outbreak control, intensification or special (CRIE — special
     immunobiologicals reference centre);
   - **Gestante** (pregnant).
8. A dose outside the schedule needs the **Justificativa (dose fora do calendário)** (justification).
9. Click **Registrar** (record).

The dose appears in **Doses do dia** (today's doses), with the time, patient, vaccine, batch and who gave it.

**Transcrever da caderneta** (transcribe from the vaccination card): for a dose given elsewhere (for example, at the maternity
hospital):

1. choose **Transcrita da caderneta** (transcribed from the card);
2. enter the **Data da dose** (dose date), the vaccine and the dose;
3. enter **Onde foi aplicada** (where it was given, for example *Maternidade Municipal*), the **Lote** (batch) and the
   **Fabricante** (manufacturer).

A transcription does not take stock.

> **Multi-dose vial:** the vial opens with the first dose given. The Stock tab shows how much is left and until when it is valid.

## 2. The patient's vaccination card

The card (here or in module 01) shows the patient's **Calendário** (schedule), with the planned doses and their status. **Mostrar
futuras e fora da idade** (show future and out-of-age doses) widens the list. Below are the **Doses registradas** (recorded
doses). On each dose:

- **Retificar** (amend): corrects a detail, with the **Motivo da retificação** (reason).
- **Anular** (void): only for a record made by mistake (wrong patient, a dose that did not happen), with the reason.
- **Notificar ESAVI** (report AEFI): opens the adverse event following immunisation report, which goes to Surveillance.

**Contraindication:** a dose contraindicated because of an AEFI only returns to the schedule with the opinion of the CRIE
reassessment. Use **Revogar (CRIE)** (revoke) and write the **Parecer da reavaliação no CRIE** (reassessment opinion). Revocation
belongs to the CRIE reassessment (physician).

## 3. Overdue people and active search

The **Atrasados** (overdue) tab lists people enrolled with the unit's teams who have any scheduled dose overdue, the oldest delay
first.

1. Filter by **Equipe** (team) and **Microárea** (micro-area).
2. Each row shows the age, team and micro-area, where the person lives and the **Doses atrasadas** (overdue doses).
3. **Imprimir lista** (print list) opens the active search list for printing and handing to the community health worker.

## 4. Vaccination coverage

The **Cobertura** (coverage) tab shows, for the unit, team or micro-area:

- per vaccine: the cohort, the vaccinated, coverage, target, homogeneity and dropout;
- in the summary: the **Homogeneidade entre vacinas** (homogeneity across vaccines), the **Vacinas na meta** (vaccines on target)
  and the **Vacinas com abandono alto (10% ou mais)** (vaccines with high dropout, 10% or more).

## 5. Vaccine stock

**The cards:**

- the **Doses aplicadas no mês** (doses given this month);
- the **Frascos abertos agora** (vials open now);
- the **Perda técnica no mês** (technical loss this month), from expired vials;
- the **Perda física no mês** (physical loss this month).

**Entrada de lote** (batch entry):

1. Click **Entrada de lote**.
2. Enter the **Vacina**, the **Número do lote** (batch number), the **Fabricante** (optional) and the **Validade** (expiry).
3. Enter the **Doses recebidas** (doses received) and the **Câmara** (cold room). In a cold room, the batch becomes suspect if a
   reading goes out of range.
4. Click **Registrar**.

**On each batch:**

- **Perda** (loss): the **Doses perdidas** (doses lost) and the **Motivo**: broken vial, batch expiry, equipment failure, power
  cut, transport problem, temperature excursion or other;
- **Ajuste** (adjustment): the **Saldo contado** (counted balance), compared with the system balance, with the
  **Justificativa**;
- **Remanejar** (reallocate): item 6;
- **Livro** (ledger): the batch statement.

Status marks are: **Vencido** (expired), **Vence em 30 dias** (expires within 30 days) and **Suspenso na rede (ESAVI)**
(suspended network-wide, AEFI).

**Frascos multidose abertos** (open multi-dose vials): the vaccine and batch, the time it was opened, the doses left and the open
vial's validity.

## 6. Reallocations between rooms

**Sending:**

1. On the stock batch, click **Remanejar**.
2. Choose the **Sala de vacina de destino** (destination vaccination room) and enter the **Doses**.
3. Record. The doses leave the stock now and stay in transit until the destination receives or refuses them.

**Receiving (at the destination):** under **A receber** (to receive):

- **Receber** (receive): enter the **Temperatura da caixa na chegada (°C)** (box temperature on arrival) and, optionally, the cold
  room. Outside 2 to 8 °C, the batch becomes suspect and waits for the coordination's assessment.
- **Recusar** (refuse): with the reason. The doses return to the source stock.

The source can **Cancelar** (cancel) while the reallocation is in transit. **Enviados e recebidos** (sent and received) shows the
history.

## 7. Cold chain

**Cold rooms:** **Nova câmara** (new cold room) registers it with the name (for example, *Câmara 1*) and the **Mínima** and
**Máxima** temperature. Immunisation management registers them.

**Readings:**

1. On the cold room, click **Registrar leitura** (record reading).
2. Enter the **Atual** (current), **Mínima do período** and **Máxima do período** (period minimum and maximum, °C), and a note if
   you want.
3. Save. The reading appears in **Leituras do dia** (today's readings), with the shift (morning or afternoon) and the mark **Na
   faixa** (in range) or **Excursão** (excursion).

**Leituras pendentes** (pending readings) shows what is still to be read today.

**Lotes sob suspeita** (suspect batches): a batch in a cold room that had an excursion, or one that arrived outside 2 to 8 °C,
becomes suspect. **It is not used** until the coordination assesses it, consulting the manufacturer or the cold chain network.

1. Click **Avaliar** (assess; immunisation management).
2. Write the **Parecer** (opinion, for example *consulta ao fabricante, estabilidade, decisão da rede de frio*).
3. Decide: release the batch, or **Inutilizar** (discard), which takes the whole balance out of stock as physical loss due to a
   temperature excursion.

## 8. Batch alerts (AEFI)

**What it is for:** surveillance of batches with adverse events. A batch goes into alert with **two or more serious AEFIs within
30 days**, or with **one AEFI of product quality deviation (A2)**. The count joins stock doses and doses with a typed batch.

**Who uses it:** immunisation management. Suspending a batch belongs to the network coordination, with access to all units.

- **Suspender na rede** (suspend network-wide), or **Suspender outro lote** (suspend another batch):
  1. enter the **Vacina**, the **Número do lote** and the **Motivo**;
  2. all batches with this number become suspect in every room, and doses with the typed batch are blocked, until the decision.
- **Decidir** (decide): choose **Liberar** (release: the batches return to use) or **Inutilizar** (discard: the balance goes out as
  loss in each room) and write the **Parecer**.

## 9. Vaccination schedule and targets

The **Calendário vacinal** (vaccination schedule) tab (immunisation management) shows the schedule in use and each vaccine's
coverage **Meta** (target). To change the target, click the vaccine, enter the **Meta (%)** and save.

## 10. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Preencha o paciente, a unidade, a vacina e a dose.* — fill in patient, unit, vaccine and dose | the record is incomplete | fill in the four fields |
| *Escolha o lote do estoque de onde a dose saiu.* — choose the stock batch the dose came from | the given dose has no batch | choose the batch |
| *Sem lote aplicável desta vacina no estoque da unidade.* — no applicable batch of this vaccine in the unit's stock | no batch with balance, in date and released | enter the batch or receive a reallocation |
| *Seu acesso não inclui a sala de vacina desta unidade (VACINACAO.REGISTRAR).* — your access does not include this unit's vaccination room | your profile does not vaccinate at this unit | ask for the profile, if your role needs it |
| *Seu acesso não inclui o calendário vacinal (IMUNIZACAO.GERENCIAR).* — your access does not include the schedule | the schedule belongs to immunisation management | ask management |
| *Informe a temperatura da caixa na chegada.* — enter the box temperature on arrival | the receipt has no temperature | measure and enter it |
| *Informe o motivo da recusa.* — enter the reason for refusal | the refusal has no reason | write the reason |
| *Informe o parecer da reavaliação no CRIE.* — enter the CRIE reassessment opinion | the revocation has no opinion | write the opinion |
| *Informe a vacina, o lote e o motivo.* — enter vaccine, batch and reason | the suspension is incomplete | fill in the three fields |
| *A suspensão do lote é da coordenação da rede (acesso a todas as unidades).* — suspension belongs to the network coordination | your access does not cover the whole network | ask the coordination |
