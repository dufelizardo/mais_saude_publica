# Encounters

> **Version:** 1.15.0 · [Português](../pt-BR/03-atendimentos.md)

Menu: **Assistência › Atendimentos** (Care › Encounters). This is where care happens, from the booking to the health record. The
encounter brings together triage, consultation, procedures, medication checks and nursing progress notes. The screen has four
tabs:

- **Atendimentos** (encounters);
- **Agendamentos** (bookings);
- **Medicação** (medication);
- **Prontuário** (health record).

## 1. The Atendimentos tab

**What it is for:** seeing the day's encounters and opening one to record care.

**Who uses it:** reception, physicians, nursing and dentists. Those who only read the health record also see the list.

**The cards at the top:**

| Card | What it shows |
|------|---------------|
| **Atendimentos hoje** (encounters today) | today's encounters and how many are in progress |
| **Aguardando atendimento** (waiting) | encounters not yet started |
| **Agendamentos hoje** (bookings today) | today's bookings |
| **Risco laranja ou vermelho** (orange or red risk) | severe patients in open encounters |

**How to read the list:**

- each row has the date and time, the patient, the type, the unit and sector, the triage risk, the records made and the status:
  waiting, in progress or completed;
- **via agendamento** means the encounter came from a booking;
- **sem triagem** (no triage) means nobody has classified the risk yet.

Use **Buscar paciente…** (search patient) and the status filter to find an encounter, and **Abrir** (open) to go into it.

## 2. Starting an encounter

There are three ways, through the buttons at the top:

| Button | When to use it |
|--------|----------------|
| **Acolhimento** (reception) | **walk-in:** the patient arrived without a booking. You find the patient, start the encounter and go straight to triage. |
| **Iniciar atendimento** (start encounter) | a new encounter, with or without a booking |
| **Agendar** (book) | booking for later (see the Agendamentos tab) |

**Who uses it:** reception, physicians, nursing and dentists.

**Walk-in step by step:**

1. Under **Buscar paciente por** (search patient by), choose **CPF** or **Cartão SUS** and type the number. If the person has no
   record, use **Cadastrar paciente** (register patient).
2. Check the patient and the age.
3. Choose the **Unidade** (unit) and, optionally, the **Setor** (sector).
4. Enter the **Matrícula de quem acolhe** (staff number of the person receiving) and the **Tipo** (type): consultation,
   emergency or admission.
5. Confirm. The encounter opens, ready for triage.

**In Iniciar atendimento,** in addition:

- **Agendamento de origem** (source booking): choose the patient's booking or **Sem agendamento (demanda espontânea)** (no
  booking, walk-in). The chosen booking becomes *Realizado* (done) by itself.
- **Especialidade** (specialty, optional): without it, the booking's specialty applies.
- **Local de realização** (place, optional): without it, the encounter is **at the unit**. It can be at a care **Ponto**
  (point), at the **Domicílio** (home) or at **Outro endereço** (another address), with a description of the place (for
  example, *Praça da Sé, em frente à igreja*) and, optionally, latitude and longitude.

## 3. Inside the encounter

The encounter opens in a wide drawer. At the top is the **encounter flow**: booking, encounter, triage, consultation, procedure
and progress note. Below are the details (type, date and time, professional, unit, sector and place) and the actions:

| Action | What it does | Who sees it |
|--------|--------------|-------------|
| **Editar atendimento** (edit encounter) | corrects the encounter details | reception, physicians, nursing, dentists |
| **Concluir atendimento** (complete encounter) | closes the encounter | the same |
| **Ver prontuário** (view record) | opens the patient's Prontuário tab | those who read the health record |
| **Encaminhar** (refer) | opens the request in **Regulação** (regulation) | physicians and dentists |
| **Pedir exames** (order tests) | opens the order in **Laboratório e imagem** (laboratory and imaging) | physicians and nurses |
| **Internar** (admit) / **Pedir vaga** (request a bed) | goes to **Leitos** (beds), to admit at the unit or request a bed elsewhere | physicians, nursing and the bed management office / physicians |

> Once there is a clinical record, the encounter's patient can no longer be changed.

Each record has its own section with a **+** button, and records already made show a **Retificar** (amend) button (see item 9).

## 4. Triage and risk classification

**Who uses it:** nurses and nursing coordination.

1. In the encounter, under **Triagem** (triage), click **+ Triagem**.
2. Choose the **Classificação de risco** (risk classification):

   | Colour | Meaning |
   |--------|---------|
   | Blue | not urgent |
   | Green | less urgent |
   | Yellow | urgent |
   | Orange | very urgent |
   | Red | emergency |

3. If available, record the **Sinais vitais** (vital signs): BP (mmHg, for example 120/80), temperature (°C), SpO₂ (%), HR
   (bpm) and weight (kg).
4. Write the **Observações** (notes) and save (*Triagem registrada*).

The risk shows in the list. Orange and red count in the card at the top.

## 5. Consultation

**Who uses it:** physicians.

1. Under **Consultas**, click **+ Consulta**.
2. Choose the **Tipo de consulta**: first consultation, follow-up or emergency.
3. Fill in:
   - the **Queixa principal** (chief complaint);
   - the **Diagnóstico** (diagnosis; include the ICD code when there is one);
   - the **Receituário** (prescription);
   - the **Exames solicitados** (tests ordered);
   - the **Retorno** (follow-up).
4. Save (*Consulta registrada*).

The consultation's prescription is what nursing checks on the Medicação tab.

## 6. Procedure

**Who uses it:** physicians, nursing and oral health.

1. Under **Procedimentos do atendimento**, click **+ Procedimento**.
2. Choose the **Procedimento** from the catalogue: those of the encounter's specialty and those of general use. If it is not
   there, use **Outro (texto livre)** (other, free text) and write the type (for example, *Curativo* — dressing, *Glicemia
   capilar* — capillary glucose).
3. Enter the **Data** (date): today, or the planned date if the procedure is scheduled. Write the **Descrição** (description)
   and save.

**Outcome:** a planned procedure gets its outcome **only once**, through the **Registrar desfecho** (record outcome) button:

- **Realizado** (done), with the date it was done;
- **Cancelado** (cancelled), with the reason.

The planned date is kept.

## 7. Medication check

**What it is for:** recording whether the prescribed medicine was given. **What was administered is taken out of the unit's
stock.**

**Who uses it:** nurses, nursing technicians and nursing coordination.

1. In the encounter, under **Medicação**, click **+ Checagem** (add check). It can also be done from the **Medicação** tab (item
   10).
2. Choose the **Prescrição** (the consultation prescription) and the **Medicamento** (medicine).
3. Under **Checagem**, choose:
   - **Administrado** (administered):
     - the **Lote** (batch), from the unit's stock, with balance and within its expiry date;
     - the **Dose** (for example, *1 g*, *500 mg*, *10 gotas* — drops) and the **Via** (route): oral, sublingual,
       intramuscular, intravenous, subcutaneous, topical, inhaled, rectal or other;
     - the **Quantidade retirada do lote** (quantity taken from the batch), in batch units (ampoules, tablets, bottles).
   - **Não administrado** (not administered): the **Motivo** (reason): patient refusal, patient absent, medicine out of stock,
     stopped by the physician or other. With *Outro* (other), describe it in the note.
4. Enter the **Matrícula de quem checou** (staff number of the person checking) and save.

> Without a consultation with a prescription there is nothing to check: *Registre a consulta com a prescrição para checar a
> medicação.*

## 8. Nursing progress note

**Who uses it:** nurses and nursing coordination.

1. Under **Evolução de enfermagem**, click **+ Evolução**.
2. Write the **Descrição** and save (*Evolução registrada*).

## 9. Correcting a record: amendment

**Clinical records are not edited.** Triage, consultation, procedure, progress note, medication check and dental assessment are
corrected by **retificação** (amendment):

1. On the record, click **Retificar**.
2. Correct what is needed and write the **Motivo da retificação** (reason): what is being corrected and why.
3. Save. The amendment stores a **new version**. The previous one stays in the health record, marked as amended.

In a medication check, if the previous check took stock, the quantity goes back to the batch before the new withdrawal.

> Only the current version can be amended. An amended version shows which one replaced it.

## 10. The Medicação tab

**What it is for:** the list of encounters with medicines to check, so nursing can work through a queue.

1. Filter by unit and choose **Em aberto** (open) or **Todos** (all).
2. In each encounter, **Registrar checagem** (record check) opens the same drawer as item 7, and **Abrir atendimento** opens the
   encounter.

The check appears in the list once the encounter's consultation is recorded.

## 11. The Agendamentos tab

**What it is for:** the list of bookings. **Booking is optional:** an encounter can start directly.

- Filter by patient. On each booking, **Iniciar** (start) opens the encounter from it. The pencil edits the booking.
- **Agendar** creates a booking with the **Paciente**, the **Data e hora**, the **Matrícula do profissional** (professional's
  staff number), the **Status** and the **Observação**.

> To book within the professional's slots, use the **Agenda** screen (module 02). It checks blocks, block-outs and leave.

## 12. The Prontuário tab

**What it is for:** the consolidated reading of the patient: the encounters with their current triages, consultations,
procedures, progress notes and medication.

**Who uses it:** those who read the health record: physicians, nursing, pharmacy, oral health, psychology, social work,
epidemiological surveillance and the regulation and diagnostic physicians.

1. Choose the patient from the list, or search by **CPF** or **Cartão SUS** and click **Buscar** (search).
2. The record shows the patient header (with **Abrir em Pacientes**, **Agendar** and **Iniciar atendimento**) and, in sections,
   the **Internações** (admissions), the **Ocorrências de urgência** (emergency calls, with the **Ficha do APH** — pre-hospital
   care form), the **Exames laboratoriais** (lab tests) and each encounter with its records.

**Quem acessou os dados deste paciente** (who accessed this patient's data): for those with the audit permission, the
encounter shows who read the record and when, including denied access. **Ver tudo na auditoria** opens the full audit trail.

## 13. Oral health in the encounter

For dentists and oral health technicians, the encounter also has:

- **Avaliação odontológica** (dental assessment, dentist):
  - the history and the clinical examination and findings;
  - the diagnosis and oral conditions;
  - the **Risco em saúde bucal** (oral health risk) and the **Necessidade de tratamento** (treatment need);
  - the initial plan.
- **Procedimentos odontológicos** (dental procedures), with the **Dente (FDI)** (tooth) and the **Faces** (surfaces). Permanent
  teeth go from 11 to 48 and primary teeth from 51 to 85. A procedure done on a tooth or surface updates the **Odontograma**
  (dental chart).
- **Plano de tratamento** (treatment plan) and **Prótese** (prosthesis), by the dentist.
- **Odontograma:** the patient's dental chart, with its history.

## 14. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Busque o paciente pelo CPF ou pelo Cartão SUS.* — search the patient by CPF or SUS card | the walk-in has no patient yet | search and choose the patient |
| *Selecione a unidade.* / *Informe a matrícula.* — select the unit / enter the staff number | the unit or staff number is missing | fill in the field |
| *Selecione a classificação de risco.* — select the risk classification | the triage has no colour | choose the classification |
| *Selecione o tipo de consulta.* — select the consultation type | the consultation has no type | choose first, follow-up or emergency |
| *Escolha o procedimento do catálogo ou informe o tipo.* — choose from the catalogue or enter the type | the procedure is empty | choose from the catalogue or use **Outro (texto livre)** |
| *Informe o dente (FDI).* / *Marque a face (ou as faces).* — enter the tooth / tick the surface(s) | the dental procedure needs tooth and surface | fill in the tooth and tick the surfaces |
| *Informe quando foi realizado.* / *Informe por que foi cancelado.* — enter when it was done / why it was cancelled | the outcome is incomplete | enter the date or the reason |
| *Selecione o lote.* / *Informe a dose.* / *Selecione a via.* — select the batch / dose / route | the administered check is incomplete | fill in batch, dose and route |
| *A quantidade precisa ser maior que zero.* — the quantity must be greater than zero | the quantity taken is zero | enter how many units left the batch |
| *Com motivo “Outro”, descreva na observação.* — with "other", describe it in the note | not administered with *Outro* needs an explanation | describe the reason |
| *Informe o motivo da retificação.* — enter the amendment reason | the amendment needs the why | write what was corrected and why |
| *Esta … já foi retificada pela versão …* — this … was already amended by version … | you tried to amend an old version | amend the current version |
| *O agendamento informado é de outro paciente.* — the booking belongs to another patient | the chosen booking is not this patient's | choose the right booking or **Sem agendamento** |
