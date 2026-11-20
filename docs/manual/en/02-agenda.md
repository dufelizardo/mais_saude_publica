# Schedule

> **Version:** 1.15.0 · [Português](../pt-BR/02-agenda.md)

Menu: **Assistência › Agenda** (Care › Schedule). A schedule always belongs to **one professional at one unit**: the recurring
blocks create the slots, and bookings take those slots.

## 1. Opening a professional's schedule

**What it is for:** seeing the slots, bookings and blocks of a professional at a unit.

**Who uses it:** those who book (reception, physicians and dentists) and those who only look: nursing and the regulating
physician.

**Step by step:**

1. Under **Profissional**, choose the professional; under **Unidade**, the unit. Without both, the screen asks *Escolha o
   profissional e a unidade* (choose the professional and the unit).
2. If you want, filter by **Especialidade** (specialty) and **Tipo de marcação** (booking type).
3. Choose the **Dia** (day) or **Semana** (week) view. The arrows change the period, and **Hoje** (today) goes back to today. In
   the week view, clicking a day opens that day.
4. The mini calendar in the side panel also takes you to any date. Days with bookings are highlighted.

**The cards at the top:**

| Card | What it shows |
|------|---------------|
| Bookings | how many there are on the day or in the week |
| **Ocupação** (occupancy) | slots taken over slots offered, as a percentage; the target is 85% |
| **Encaixes** (fit-ins) | bookings made outside the slots |
| **Faltas** (no-shows) | the no-shows recorded in the period |

**Colour legend:** booked consultation, procedure or test, fit-in, free slot, recorded no-show, and block, holiday or leave.

The side panel has **Próximas marcações · hoje** (next bookings today), **Agenda recorrente** (the recurring blocks) and
**Bloqueios no período** (blocks in the period).

## 2. Booking a patient

**What it is for:** reserving a time for the patient.

**Who uses it:** reception, physicians and dentists.

**Step by step:**

1. Click a **free slot** in the grid, or **Nova marcação** (new booking). The slot fills in the date, time and type.
2. Under **Paciente** (patient), type part of the name or CPF to filter and choose the person.
3. Check **Data e hora** (date and time) and the **Tipo** (type): consultation, procedure or follow-up visit.
4. Tick **Encaixe** (fit-in) only if the time is not a slot in the schedule. Without fit-in, the time must be a free slot.
5. Under **Programa** (programme, optional), choose the health programme the patient is followed up in. With the programme, the
   booking fulfils the patient's programmed action (the scheduled consultation).
6. Write an **Observação** (note) if needed, and save.

The confirmation reads *Marcação feita* (booking made) or *Encaixe marcado* (fit-in booked).

> Slots in the past cannot be booked.

## 3. Cancelling, recording a no-show or requesting transport

Click the booking in the grid. The drawer shows the **Paciente**, **Quando** (when), the **Tipo** and the **Situação** (status:
booked, confirmed, done, cancelled or no-show). While it is booked or confirmed:

- **Cancelar marcação** (cancel booking): frees the slot (*Marcação cancelada*).
- **Registrar falta** (record no-show): the patient did not come (*Falta registrada*). It counts in the **Faltas** card.
- **Pedir transporte** (request transport): opens the health transport request for the visit, already with the patient and the
  booking. It appears for those who request transport (reception, nursing coordination and social work).

## 4. Setting up the recurring schedule (blocks)

**What it is for:** defining when the professional sees patients every week. **Without a block there are no slots: only
fit-ins.**

**Who uses it:** reception, physicians and dentists.

**Step by step:**

1. In the **Agenda recorrente** panel, click **+ Bloco** (add block).
2. Choose the **Dia da semana** (weekday) and the **Tipo de atendimento** (type of care, or none).
3. Enter the **Início** (start), **Fim** (end) and **Duração da vaga (min)** (slot length), from 5 to 240 minutes.
4. Under **Vale a partir de** (valid from), the date when the block starts creating slots.
5. Save (*Bloco criado*, block created).

**Changing a block:** a block cannot be edited. End it and create another, and the schedule history is kept.

1. On the block, click **Encerrar** (end).
2. Enter the **Último dia do bloco** (last day of the block). After that date the block creates no slots, and existing bookings
   stay.

**Rules the system checks:**

- the end must be after the start, and the block cannot be shorter than one slot;
- the block must fit within the unit's opening hours;
- the professional cannot have two overlapping blocks on the same day.

## 5. Blocking the schedule

**What it is for:** closing times for a day off, meeting, training or unit closure.

**Who uses it:** reception, physicians and dentists.

**Step by step:**

1. In the **Bloqueios no período** panel, click **+ Bloqueio** (add block-out).
2. Under **Para quem** (for whom), choose the professional at this unit or **A unidade inteira** (the whole unit).
3. Enter the **Início** and **Fim**.
4. Choose the **Motivo** (reason): day off, meeting, training, unit closed or other. Optionally, a **Descrição** (for example,
   *reunião de equipe*, team meeting).
5. Click **Bloquear** (block) (*Agenda bloqueada*).

To undo it, click **Remover** (remove) on the block-out. The slots of the period come back to the schedule.

> **Holidays and leave approved in HR already close the schedule;** they do not need a block-out.

## 6. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Selecione o paciente.* / *Informe a data e a hora.* — select the patient / enter the date and time | the patient or the time is missing | fill in the field |
| *Esse horário não é uma vaga da agenda do profissional nessa unidade. Escolha uma vaga livre ou marque como encaixe.* — this time is not a slot | the time is not in any block | choose a free slot or tick **Encaixe** |
| *Essa vaga já está ocupada…* — this slot is already taken | someone booked the time first | choose another slot or book as a fit-in |
| *A agenda está bloqueada nesse horário (…)* — the schedule is blocked at this time | there is a block-out in the period | choose another time, or remove the block-out if it no longer applies |
| *O profissional está afastado (…)* — the professional is on leave | there is HR holiday or leave in the period | book with another professional or on another date |
| *A … não funciona nesse horário.* — the unit is not open at this time | the unit is closed at that time | choose a time within opening hours |
| *O fim precisa ser depois do início.* — the end must be after the start | in the block or block-out, the end came first | correct the times |
| *Use de 5 a 240 minutos.* — use 5 to 240 minutes | the slot length is out of range | use a length between 5 and 240 minutes |
| *O bloco é menor que uma vaga.* — the block is shorter than one slot | the block cannot hold a single slot | make the block longer or the slot shorter |
| *O profissional já tem um bloco na …* — the professional already has a block on … | the block overlaps another | end the old block or change the time |
| *Esta unidade está fora do seu acesso.* — this unit is outside your access | your profile does not cover the unit | ask for the profile at that unit, if your role needs it |
