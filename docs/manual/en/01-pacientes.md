# Patients

> **Version:** 1.15.0 · [Português](../pt-BR/01-pacientes.md)

Menu: **Assistência › Pacientes** (Care › Patients). This is the single record of everyone cared for in the network: personal
data, address and, in a side panel, the clinical summary, programmes, vaccination and documents.

## 1. Finding a patient

**What it is for:** finding the person before booking, seeing them or checking the record.

**Who uses it:** anyone who sees or follows up patients: reception, physicians, nursing, pharmacy, laboratory, regulation, oral
health, mental health, social work, epidemiological surveillance and managers.

**Step by step:**

1. Type the name or CPF in **Buscar por nome ou CPF…** (search by name or CPF). The list filters as you type.
2. If needed, use the filters next to it:
   - **Programa** (programme): only people followed up in that programme, in the units you have access to;
   - **Faixa etária** (age group): 0–11, 12–17, 18–59 or 60+ years;
   - **Status:** active (*Ativo*) or inactive (*Inativo*).
3. Choose how to see the result: **Lista** (a table with name, CPF, age, sex, SUS card and status) or **Cartões** (cards).
4. Click the patient to open the detail panel on the right.

The cards at the top show **active patients**, **new records this month** and how many are **in health programmes**.

**Export:** the **Exportar** button downloads the filtered list as CSV (spreadsheet). The file has the name, CPF, SUS card, date
of birth, age, sex, phone, e-mail, city, state, status and registration date.

> The exported file contains personal data. Keep it only as long as you need it and do not send it through open channels.

## 2. Registering or editing a patient

**What it is for:** recording a new person or correcting the data of someone already registered.

**Who uses it:** reception and community health workers. Without this permission, the **Novo paciente** (new patient) button and
the edit pencil do not appear.

**Step by step:**

1. Click **Novo paciente** or, with the patient open in the panel, the pencil (**Editar**, edit).
2. Fill in the fields. Those marked **\*** are required:
   - **Nome completo \*** (full name);
   - **CPF \*** (the dots and dash are added automatically);
   - **Cartão Nacional de Saúde (CNS)** (national health card), optional;
   - **Data de nascimento \*** (date of birth);
   - **Sexo \*** (sex): male, female or unknown;
   - **Telefone \*** (phone) and **E-mail** (optional).
3. Under **Endereço** (address), type the **CEP \*** (postcode). The system looks up the street, district, city and state. Check
   them and fill in the **Número \*** (number) and **Complemento** (address line 2).
4. Leave **Ativo** (active) ticked. Untick it only when the record should no longer appear as an active patient of the network.
5. Click **Salvar** (save). **Cancelar** (cancel) closes the window without saving.

**Tips:**

- Before registering, search by CPF so as not to create a duplicate record.
- If the CEP is not found, fill in the address by hand.

## 3. The patient panel

When you click a patient, the right-hand panel shows the name, age, sex, CPF and CNS, and five tabs.

### Resumo (summary)

- **Sinais vitais · última triagem** (vital signs · last triage): blood pressure, heart rate, temperature and weight from the last
  triage.
- **Equipe de referência** (reference team): the family health team responsible for the patient (see item 4).
- **Programas vinculados** (linked programmes): the programmes the patient is in, with the unit, team, start date and status.
  **Atrasado** (overdue) means the follow-up is late.
- **Próximos atendimentos** (upcoming appointments): future bookings. The **Agenda →** link opens the patient's bookings on the
  **Atendimentos** (encounters) screen.
- **Histórico recente** (recent history): the latest clinical records. **Ver histórico completo** opens the History tab.

### Histórico (history)

The timeline of clinical records (triages, consultations, progress notes and procedures), newest first. **Ver prontuário
completo** opens the full health record on the **Atendimentos** screen.

### Programas (programmes)

Current programmes and the history of closed ones, care pathways and programmed actions (for example, a planned consultation or
test, with its date). People who follow up programmes (physicians, nursing, oral health, psychology and social work) have three
actions for each programme:

| Action | What it asks for | What it is for |
|--------|------------------|----------------|
| **Marcar faltoso** (mark as defaulter) | the reason | the patient stopped attending |
| **Voltar a ativo** (back to active) | the reason | the defaulter came back to follow-up |
| **Saída** (exit) | the exit reason, a description and, optionally, the date (not in the future) | ending the follow-up; **discharge** (*alta*) completes it, the other reasons close it |

The exit stays in the history; nothing is deleted. The **Inscrever em um programa** button (enrol in a programme) goes to the
**Programas de Saúde** screen. In the mental health programme, the **Abrir em Saúde Mental** link goes to its own screen.

**Sensitive** programmes (marked in purple) from another unit are not shown; the panel only says how many there are.

### Vacinação (vaccination)

The patient's vaccination card. People who vaccinate (nursing) record the dose here. Those who neither read the health record nor
vaccinate see a notice that the card is health data.

### Anexos (attachments)

The patient's documents, such as external test results, reports and signed forms (see item 5).

## 4. Reference team (enrolment)

**What it is for:** knowing which team is responsible for the patient and, when needed, linking them to a team by hand.

**How it works:**

- **Pelo domicílio** (by household): someone living in a registered household of an area with a team already shows that team.
  The **ver no Território** link shows where.
- **Sem equipe de referência** (no reference team): the patient does not live in a registered household of an area with a team.
- **Manual:** for people who do not live in a household of the territory (for example, people experiencing homelessness) or who
  chose another team.

**Who does manual enrolment:** nursing and community health workers.

**Step by step:**

1. In the Summary, under **Equipe de referência**, click **Adscrever manualmente** (enrol manually).
2. Choose the **Equipe** (team) and write the **Motivo** (reason).
3. Save. To undo it, use **Encerrar adscrição manual** (end manual enrolment), also with a reason. The patient goes back to the
   household's team, if there is one.

The **Histórico de adscrição** (enrolment history) keeps the previous teams.

## 5. Attaching documents

**What it is for:** keeping a patient document in the record, such as a test from another service, a report or a signed form.

**Who uses it:** people who attach documents (reception, physicians, nursing, psychology, social work and managers) and people
who read the health record, who can open the files.

> Attaching does not grant the right to open: those who only attach see the documents they attached themselves. Opening the files
> is for those who read the health record.

**Step by step:**

1. On the **Anexos** tab, click **Anexar documento** (attach document).
2. Fill in the **Tipo** (type), **Título** (title), **Unidade** (unit) and, optionally, the document **Data** (date).
3. Under **Arquivo** (file), choose a PDF, JPG or PNG of up to **10 MB**.
4. Click **Enviar** (send).

In the document list:

- **Baixar** (download): opens the file.
- **Nova versão** (new version): replaces the file and keeps the previous versions, which can still be downloaded.
- **Retirar** (withdraw): removes the document from the list, with a reason. Nothing is deleted: the document moves to
  **Retirados** (withdrawn).

## 6. Patient without a care relationship: access with justification

When the care-relationship requirement is turned on in your environment, the health record only opens for those with a care
relationship with the patient: an encounter at your unit, or being the professional of the encounter or of the booking. Without
it, the panel shows **Sem vínculo assistencial com este paciente** (no care relationship with this patient).

When you need to see it anyway:

1. Click **Acessar com justificativa** (access with justification).
2. Choose the **Motivo** (reason): **Emergência** (emergency), **Continuidade do cuidado** (continuity of care) or **Regulação ou
   encaminhamento** (regulation or referral).
3. Write the **Justificativa** (justification), with at least 20 characters.
4. Confirm. Access lasts **4 hours**, and the panel shows *Acesso justificado até…* (justified access until…).

> Justified access is recorded in the audit trail, with the reason and the text. Use it only when care requires it.

## 7. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Nome é obrigatório.* — name is required (and the other fields with **\***) | a required field is missing | fill in the field marked in red |
| *CEP não encontrado — preencha o endereço manualmente.* — postcode not found, fill in the address by hand | the CEP is not in the address database | type the street, district, city and state |
| *Não foi possível salvar o paciente. Tente novamente.* — the patient could not be saved | the record was not stored | check the data (for example, whether the CPF is already registered) and save again |
| *Sem acesso ao prontuário deste paciente.* — no access to this patient's record | your profile or care relationship does not allow seeing clinical data | if needed, use access with justification (item 6) |
| *Seu acesso não inclui os programas do paciente.* — your access does not include the patient's programmes | your profile does not see programmes | ask for the profile, if your role needs it |
| *O arquivo vai até 10 MB.* — the file can be up to 10 MB | the file is too large | reduce the file or scan at a lower resolution |
| *Descreva a justificativa com pelo menos 20 caracteres.* — describe the justification with at least 20 characters | the justification is too short | explain the reason for access better |
| *A saída não pode ser no futuro.* — the exit cannot be in the future | the programme exit date is after today | use today's date or an earlier one |
