# Regulation

> **Version:** 1.15.0 · [Português](../pt-BR/05-regulacao.md)

Menu: **Assistência › Regulação** (Care › Regulation). This is the Access Regulation Centre: referrals for specialist
consultations, tests or procedures, the priority queue and the slot at the unit that performs it.

**The path of a request:**

1. the physician or dentist **requests**;
2. the request enters the **queue**;
3. the regulator **authorises** it with a slot, **returns** it for more information or **denies** it;
4. the performing unit **books**;
5. the patient is **seen**, with the counter-referral, or **does not show up**.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Physicians and dentists | request, complete and follow up |
| Regulating physician | regulates the queue and keeps the catalogue |
| Reception | follows progress and books at the performing unit |

The screen has three tabs:

- **Fila** (queue);
- **Solicitações** (requests);
- **Procedimentos regulados** (regulated procedures).

**The cards at the top:**

- **Na fila** (in the queue);
- **Vermelho ou amarelo** (red or yellow) waiting in the queue;
- **Devolvidas** (returned) waiting for more information;
- **Autorizadas** (authorised) with a booked slot.

## 1. Requesting a referral

**Who uses it:** physicians and dentists. The **Encaminhar** (refer) button inside the encounter (module 03) opens the same
drawer, already with the patient and the unit.

1. Click **Nova solicitação** (new request).
2. Under **Paciente**, type part of the name or CPF and choose the person.
3. Choose the **Procedimento** (from the catalogue of regulated procedures) and the **Unidade que solicita** (requesting unit).
4. Enter the **CID-10** (ICD-10 code, for example *I20.9*) and the **Matrícula de quem solicita** (requester's staff number).
5. Choose the **Prioridade proposta** (proposed priority):

   | Colour | Meaning |
   |--------|---------|
   | Red | emergency |
   | Yellow | urgent |
   | Green | not urgent |
   | Blue | elective |

   The regulator can reclassify it while the request is in the queue.
6. Write the **Justificativa clínica** (clinical justification), with at least 10 characters: the history, the findings and what
   is expected from the referral.
7. Save (*Solicitação registrada*).

## 2. The queue

**What it is for:** the Centre's order of service, **by priority and, within it, by the time of the request**. Each regulator
sees only the units in their scope.

Each row shows the **Posição** (position), the **Prioridade**, the patient, the procedure, the requester and the **Espera**
(waiting time). A request linked to an ongoing emergency is marked with its name. Filter by procedure and priority.

## 3. Regulating (regulating physician)

1. In the queue, click **Analisar** (analyse). The drawer shows the ICD-10 code, the requesting unit, who requested it and when,
   the clinical justification and the **Histórico** (history).
2. Under **Decisão da regulação**, choose the **Decisão** (decision):
   - **Autorizar com vaga** (authorise with a slot; see below);
   - **Devolver para complementar** (return for more information): say what is missing to regulate;
   - **Negar** (deny): enter the reason for the denial;
   - **Reclassificar prioridade** (reclassify priority): choose the **Nova prioridade** (new priority, different from the current
     one) and the reason.
3. Enter the **Matrícula de quem regula** (regulator's staff number) and confirm.

> **The requester cannot authorise or deny their own request.**

**Authorising with a slot:**

1. Choose the **Unidade executante** (performing unit). The drawer shows the procedure's **network reference** and warns when the
   choice is **fora da referência** (outside the reference).
2. If the procedure requires a capability (for example, endodontics at a dental specialty centre), only a unit with it is
   accepted.
3. Enter **Quem vai atender** (who will see the patient, optional) and the **Data e hora da vaga** (slot date and time). The
   drawer shows the free slots for the next 30 days.
   - **With the staff number** of who will see the patient, authorising also books.
   - **Without it**, the performing unit books later.
   - Without a free slot, only as a fit-in through the Schedule.
4. **PPI (agreed programming):** the drawer shows the procedure's agreed quota. With no balance left, tick **Autorizar como
   extracota** (authorise over quota) and write the **Justificativa da extracota**.
5. Optionally, an **Orientação ao paciente** (patient instruction; for example, *levar os exames anteriores* — bring previous
   tests).
6. Confirm: *Solicitação autorizada e agendada* (authorised and booked) or *Solicitação autorizada*.

## 4. Completing a returned request

**Who uses it:** the requester (physician or dentist).

1. On the **Solicitações** tab, returned requests show the action to complete them.
2. Read what the regulation asked, write the **Resposta ao regulador** (answer to the regulator) and the **Matrícula de quem
   complementa**.
3. Save. The request goes back to the queue **in its original position**.

## 5. Following up, booking, recording the visit or the no-show

The **Solicitações** tab shows the progress of each referral, **without clinical data**. The details open only for those who
request or regulate.

- **Search and filter:** by patient, procedure or unit, and by status: in the queue, returned, authorised, booked, done, no-show,
  denied or cancelled.
- **Agendar** (book; authorised request with no booking; reception or regulator):
  1. enter the **Matrícula de quem vai atender** and the **Data e hora**;
  2. enter the **Matrícula de quem agenda** (staff number of the person booking);
  3. the booking goes into the schedule of the person who will see the patient, at the performing unit.
- **Atendido** (seen; booked request; those who record consultations or procedures):
  1. write the **Contrarreferência** (counter-referral), with at least 10 characters: what was done, the finding and the
     management plan for whoever continues the care;
  2. enter the **Matrícula de quem atendeu** and save.
- **Faltou** (no-show): describe **O que aconteceu** (what happened) and enter the **Matrícula de quem registra**. The booking is
  closed and the request ends as a no-show. **To see the patient, a new request will be needed.**
- **Cancelar** (cancel): while still possible, with the reason and the staff number of the person cancelling.

## 6. Regulated procedures (catalogue)

**What it is for:** what goes through the Centre, such as specialist consultations and tests. **A direct-access procedure is
booked at the unit itself** and is not included here.

**Who uses it:** the regulating physician.

1. Click **+ Procedimento**.
2. Enter the **Nome** (name, for example *Consulta em cardiologia*) and the **Tipo** (type): specialist consultation, test or
   procedure.
3. Optionally, the **Capacidade exigida da executante** (capability required from the performing unit), from the Operating
   Model. Authorisation then only accepts a unit that has it.
4. Leave **Em uso** (in use) ticked. When not in use, it does not appear in new requests, and old requests continue.

## 7. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Informe o CID-10 (ex.: I10, E11.9).* — enter the ICD-10 code | the request has no ICD code | enter the code |
| *Descreva a justificativa clínica (pelo menos 10 caracteres).* — describe the justification (at least 10 characters) | the justification is too short | describe the history and what is expected |
| *A vaga não pode estar no passado.* — the slot cannot be in the past | the slot date has passed | choose a future slot |
| *Sem saldo na PPI: marque extracota para autorizar.* — no PPI balance: tick over quota | the agreed quota is used up | tick over quota and justify, or choose another performing unit |
| *Escolha uma prioridade diferente da atual.* — choose a different priority | the reclassification repeated the colour | choose another priority |
| *Diga o que falta para regular.* — say what is missing | the return has no request | write what the requester needs to provide |
| *Informe o motivo da negativa.* — enter the reason for denial | the denial has no reason | write the reason |
| *Responda ao que o regulador pediu.* — answer what the regulator asked | the completion is empty | answer the regulation's request |
| *Escreva a contrarreferência (pelo menos 10 caracteres).* — write the counter-referral | the visit has no counter-referral | describe what was done and the plan |
| *Matrícula não encontrada.* — staff number not found | the number is not a professional's | check the number |
| *Esta solicitação é de uma unidade fora do seu acesso.* — this request is from a unit outside your access | your profile does not cover the unit | ask for the profile, if your role needs it |
