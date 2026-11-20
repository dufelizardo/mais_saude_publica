# Beds

> **Version:** 1.15.0 · [Português](../pt-BR/08-leitos.md)

Menu: **Assistência › Leitos** (Care › Beds). It covers the life of the bed and of the admission:

- the bed map;
- admitting, changing beds, discharging and cleaning;
- the hospital prescription and the nursing station's times;
- elective admissions;
- the daily census;
- the AIH (hospital admission authorisation) form and its authorisation;
- the bed centre between units and the bed register.

**Who uses it and what each sees:** tabs appear according to the profile.

| Tab | Who uses it |
|-----|-------------|
| **Mapa de leitos** (bed map) and **Internações** (admissions) | those who view admissions: reception, physicians, nursing, nutrition, the bed management office (NIR), bed regulation and the authorising physician |
| **Horários** (times) | nursing (checks the prescription), the nutritionist (checks the diet) and physicians |
| **Eletivas** (electives) and **A receber** (to receive) | physicians, nursing and the NIR (bed manager) |
| **Censo diário** (daily census) | those who view admissions; nursing coordination and the NIR close it |
| **Autorização de AIH** (AIH authorisation) | the authorising physician; number ranges belong to the manager |
| **Central de leitos** (bed centre) | the bed regulator |
| **Cadastro de leitos** (bed register) | nursing, nursing coordination and the NIR |

**The cards at the top:**

- the **Taxa de ocupação** (occupancy rate);
- the **Livres** (free beds), ready to admit;
- **Higienização e bloqueio** (cleaning and blocked): being cleaned and blocked;
- the **Média de permanência** (average length of stay).

**Notices at the top:**

- **Comunicação ao Ministério Público pendente** (communication to the Public Prosecutor's Office pending): an involuntary
  psychiatric admission must be communicated within **72 hours** of admission and of discharge (Brazilian Law 10.216/2001).
- **Internações de urgência sem laudo de AIH há mais de 48 horas** (emergency admissions without an AIH form for more than 48
  hours).

## 1. The bed map

The map shows the beds by unit and sector, with each one's status: free, occupied, being cleaned or blocked.

| On a bed that is… | Action |
|-------------------|--------|
| free | **Internar** (admit) or **Bloquear** (block; with a reason, for example *manutenção da cama* — bed maintenance; the bed leaves the vacancy map until unblocked) |
| occupied | **Abrir** (open) the admission |
| being cleaned | **Liberar** (release), when cleaning is done |
| blocked | **Desbloquear** (unblock) |

## 2. Admitting at your own unit

**Who uses it:** physicians, nursing and the NIR. The **Internar** button inside the encounter (module 03) brings the patient
and links the admission to the encounter.

1. Click **Internar** on the free bed, or use the button at the top.
2. Under **Paciente**, type part of the name or CPF and choose the person.
3. Choose the **Leito livre** (free bed). Only free beds compatible with the patient's sex appear.
4. Enter the **CID-10 principal** (main ICD-10 code, for example *J18.9*) and the **Matrícula do médico responsável** (staff number
   of the physician in charge).
5. Write the **Motivo da internação** (reason for admission): the clinical picture and why the patient needs to stay.
6. Choose the nature (**Urgência** — emergency, or **Eletiva** — elective) and, optionally, the **Previsão de alta** (expected
   discharge).
7. If the bed is for another age group, write the **Justificativa de faixa etária** (age group justification). The groups are:
   neonatal up to 28 days, paediatric up to under 13 years, adult from 13.
8. Save.

## 3. The open admission

On the **Internações** tab (or by opening an occupied bed), click **Ver** (view). The list does not show the reason or the
summary: they open in the details, with the reading recorded.

The details show the physician in charge, the ICD-10 code, the reason, the **Movimentos** (movements) and the actions:

- **Trocar de leito** (change bed):
  1. choose the **Leito de destino** (destination bed) and write the **Motivo** (for example, *isolamento de contato* — contact
     isolation);
  2. enter the **Matrícula de quem faz a troca**.

  The current bed goes to cleaning and is only free once cleaning is recorded.
- **Pedir transporte** (request transport): opens the health transport request.
- **Dar alta** (discharge; physician):
  1. choose the **Tipo de alta** (discharge type): improved, at own request, transfer to another unit, absconded or death;
  2. enter the **Data e hora** (empty means now; it cannot be in the future);
  3. write the **Sumário de alta** (discharge summary): diagnosis, treatment, progress and instructions for whoever continues the
     care;
  4. enter the **Matrícula do médico que dá a alta**.

  **Discharge closes the admission, which no longer changes.** The bed goes to cleaning.
- **Internação psiquiátrica** (psychiatric admission):
  1. choose the **Modalidade**: voluntary, involuntary or compulsory (by court order, with its number);
  2. for involuntary admissions, use **Registrar comunicação** (record communication) for the communication to the Public
     Prosecutor's Office, with the protocol number. Once the deadline passes, the details show *prazo vencido* (deadline
     passed).

## 4. Hospital prescription and times

**Prescribing (physician):**

1. In the admission details, under **Prescrição** (prescription), click **Nova prescrição** (new prescription), or **Renovar**
   (renew) to copy the previous one (the items come in **Itens copiados** — copied items).
2. For each item, choose:
   - **Medicamento** (medicine): the dose (for example, *1 g*), the route and the **Intervalo (horas)** (interval in hours); with
     **Se necessário** (as needed), the condition of use (for example, *febre acima de 37,8* — fever above 37.8);
   - **Dieta** (diet): nil by mouth, free, soft, pureed, liquid, liquid-pureed, low-sodium, diabetic, enteral or parenteral;
   - **Cuidado** (care): the description.
3. Enter the **Matrícula do médico** and save. The prescription lasts 24 hours.

**Suspender** (suspend) an item cancels its pending times.

**Horários (nursing station):** the **Horários** tab shows the scheduled times of the last and next 6 hours, by sector. **Pending
for more than 1 hour becomes overdue.**

- **Checar** (check):
  - for a medicine, record the dose. With the **Lote** (batch), it is taken from stock; choose **Sem baixa** (no stock
    withdrawal) when the dose was already dispensed;
  - for a diet (nutritionist), the **Aceitação** (acceptance);
  - if it was not done, the **Motivo**.
- **Reaprazar** (reschedule): the **Novo horário** (new time).
- **Retificar** (amend): the check, with the **Motivo da retificação**.

## 5. Elective admissions

The **Eletivas** tab shows surgeries and planned admissions for the next 30 days, and past ones still without an outcome.

**Agendar internação eletiva** (book an elective admission):

1. Use the **Agendar internação eletiva** button at the top.
2. Enter:
   - the patient, the **Unidade** and the **Data prevista** (planned date; cannot be in the past);
   - the **CID-10** and the **Procedimento ou cirurgia** (procedure or surgery);
   - the **Previsão de permanência (dias)** (expected length of stay in days).

**The bed:** **Reservar leito** (reserve bed) holds a bed from two days before. The reservation lasts until the end of the
planned day.

**On the day:**

- **Admitir** (admit): the patient arrived;
- **Mais › O que registrar** (more › what to record):
  - **Remarcar** (reschedule), with the new date;
  - **Falta do paciente** (patient no-show);
  - **Cancelar** (cancel).

The **Laudo AIH** (AIH form) can also be filled in from here.

## 6. AIH form and authorisation

**Filling in the form (physician):**

1. In the admission details or the elective, use **Preencher laudo de AIH** (fill in AIH form).
2. Fill in:
   - the **Principais sinais e sintomas clínicos** (main clinical signs and symptoms);
   - the **Condições que justificam a internação** (conditions justifying admission);
   - the **Principais resultados de provas diagnósticas** (main diagnostic test results);
   - the **Diagnóstico inicial** (initial diagnosis) and the ICD-10 codes (main, secondary and associated cause);
   - the **Procedimento solicitado** (requested procedure): search by name or by SIGTAP code (the SUS procedure table);
   - the **Clínica** (specialty) and, if any, the **Causa externa** (external cause): road accident, typical or commuting work
     accident, or other;
   - the **Médico solicitante (matrícula)** (requesting physician's staff number).
3. The form can be **Corrigido** (corrected), **Impresso** (printed) or **Cancelado** (cancelled) while not yet authorised.

**Authorising (authorising physician):** the **Autorização de AIH** tab shows the forms waiting, emergencies first.

- **Autorizar** (authorise): the form gets the next number from the year's current AIH range.
- **Devolver** (return, with what is missing) or **Negar** (deny).

> **The requester cannot authorise their own form.**

**Number ranges (manager):**

1. Under **Cadastrar faixa** (register range), enter the **Ano** (year) and the **first 12 digits** of the start and end of the
   range received from the health authority, without the check digit. The system calculates the digit (modulo 11).
2. Authorisation uses the next free number of the year's active range. **Without a range, it cannot authorise.**

## 7. Daily census

**What it is for:** each unit's movement for the day:

- operational beds and inpatients at 0h;
- admissions, discharges, absconding and deaths, and internal transfers;
- inpatients at 24h, patient-days and occupancy.

1. Choose the unit and the **Dia** (day) and click **Ver censo** (view census). The **Censos não fechados** (census not closed)
   list shows what is still to be closed.
2. Check the **Censo nominal (às 24h)** (named census at 24h): who was admitted, in which bed and sector, and for how many days.
3. Close the census (nursing coordination or NIR).

**Once closed, the census is kept.** To correct it, use **Reabrir censo** (reopen census) with the **Motivo da reabertura**
(reason) and close it again.

## 8. Bed centre (between units)

**Requesting a bed at another unit (physician):**

1. Use the **Pedir vaga em outra unidade** (request a bed at another unit) button at the top, or **Pedir vaga** inside the
   encounter.
2. Enter:
   - the patient and the **Unidade de origem** (source unit);
   - the type of bed requested and the priority;
   - the **CID-10** and the **Quadro clínico** (clinical picture): vital signs, severity and why a bed is needed;
   - the **Matrícula de quem pede**.
3. The request goes to the bed centre, which reserves a compatible bed at another unit.

> To admit at your own unit, use **Internar** (item 2).

**Regulating (bed regulator):** the **Central de leitos** tab has the queue by priority and request time. Under **Regular**:

- **Reservar** (reserve) one of the **Leitos compatíveis** (compatible beds), for a time (**Reservar por (horas)** — reserve for
  hours);
- **Vaga zero:** the patient goes without a confirmed free bed, and the unit chooses the bed at admission. Choose the **Unidade
  que recebe** (receiving unit) and write the **Justificativa**;
- **Negar** (deny), with the reason.

Also enter the **Transporte** (for example, *USA 02 do SAMU* — advanced ambulance 02) and the **Matrícula do regulador**.

**Receiving (destination unit):** the **A receber** tab shows patients the centre sent to your unit, with a reserved bed or as
*vaga zero*.

- **Admitir** on arrival: choose the **Leito livre da unidade** (unit's free bed) and enter the **Médico responsável
  (matrícula)**.
- **Recusar** (refuse), with the reason. The request goes back to the centre's queue in its original position, and the bed
  becomes free again.

The source unit can **Cancelar pedido** (cancel request) while it is in the queue. An unused reservation expires on time and
shows as *Reserva vencida* (reservation expired).

## 9. Bed register

**Who uses it:** nursing, nursing coordination and the NIR.

1. Click **+ Leito** (add bed).
2. Choose the **Unidade** and the **Setor assistencial** (care sector). The bed belongs to a care sector of the unit.
3. Enter the **Identificação** (identification, for example *Enf. 2 · Leito 03*) and the **Tipo** (type): clinical, surgical,
   paediatric, obstetric, adult ICU, paediatric ICU, neonatal ICU, observation or isolation.
4. Choose the sex (male, female or mixed) and the **Faixa etária** (age group).
5. Save.

**Beds are not deleted:** they go out of use, and the history remains. Out of use, the bed leaves the map. **An occupied bed
cannot go out of use.**

> The number of beds authorised per capability is set by management. Above it, registration is blocked.

## 10. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Escolha um leito livre.* — choose a free bed | the admission has no bed | choose a free, compatible bed |
| *Informe o CID-10 principal (ex.: J18.9).* — enter the main ICD-10 code | the diagnosis is missing | enter the ICD code |
| *Informe o motivo da internação.* — enter the reason for admission | the reason is missing | describe the picture and why admit |
| *A alta não pode ser no futuro.* — discharge cannot be in the future | the discharge time is after now | use now or an earlier time |
| *Informe o motivo do bloqueio.* — enter the reason for blocking | the block has no reason | write the reason |
| *Descreva o quadro clínico.* — describe the clinical picture | the bed request has no picture | describe the signs and severity |
| *Escolha um dos leitos compatíveis.* — choose a compatible bed | the reservation has no bed | choose a bed from the list or consider *vaga zero* |
| *Informe o motivo da recusa.* — enter the reason for refusal | refusal at the destination has no reason | write the reason |
| *A data não pode ser no passado.* — the date cannot be in the past | the elective date has passed | choose today or a future date |
| *Informe a condição de uso.* — enter the condition of use | the *Se necessário* item has no condition | describe when to give it |
| *Informe os 12 primeiros dígitos, sem o verificador.* — enter the first 12 digits, without the check digit | the AIH range is incomplete | type the first 12 digits |
| *O fim não pode ser menor que o início.* — the end cannot be lower than the start | the AIH range is reversed | correct start and end |
| *Informe o motivo da reabertura.* — enter the reason for reopening | the census reopening has no reason | write what will be corrected |
