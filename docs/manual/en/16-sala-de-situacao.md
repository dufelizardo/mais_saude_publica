# Situation Room

> **Version:** 1.15.0 · [Português](../pt-BR/16-sala-de-situacao.md)

Menu: **Sala de Situação** (Situation Room, at the top of the menu). It is the network management dashboard, calculated on the fly
from the records:

- care production and the operational situation;
- programmes, surveillance, vaccination and cost per unit.

**Aggregates only, no patients.**

**Who uses it:** those who view indicators: managers, nursing coordination and the Patient Safety Office.

## 1. Choosing the scope

1. Under **Unidade**, choose a unit or **Todas do meu acesso** (all I can access).
2. Under **Até** (until), choose the end of the period. The figures are for the period ending on that date.
3. **Exportar** (export) downloads care production as CSV. The export is recorded in the audit trail.

At the top, the current **notices** lead to the source screen, for example:

- **Ver emergência** and **Ver evento** (view emergency / event): emergencies with overdue actions and events;
- **Ver índices** and **Ver amostras** (view indices / samples): *Aedes* indices and water quality, in Zoonoses and vectors;
- **Ver medidas** (view measures): health inspection closures nearing their end;
- **Ver na vigilância** (view in surveillance): worker health alerts;
- **Ver na manutenção** and **Ver no painel** (view in maintenance / on the panel): Assets and Procurement.

## 2. Production

The production cards show:

- **Atendimentos concluídos** (completed encounters) and **Média diária** (daily average);
- **Marcações na agenda** (bookings) and **Faltas registradas** (recorded no-shows);
- **Absenteísmo** (absenteeism: no-shows over those done plus no-shows);
- **Encaixes realizados** (fit-ins done).

The charts show:

- encounters by **Dia** (day) or **Mês** (month) and **Atendimentos por hora** (encounters by hour);
- encounters by age group (age on the day of the encounter);
- encounters by type and by the professional's council;
- the eight specialties with most encounters, by sex.

## 3. Now and waiting time

- **Agora** (now): encounters in progress in the last 24 hours:
  - how many are being seen;
  - how many are waiting for triage and how many for consultation;
  - the longest wait.
- **Tempo de espera** (waiting time): the mean, median and 90th percentile from recorded arrival to first contact and to
  consultation.

## 4. Roster and schedules

- **Ocorrências da escala** (roster issues): over the next 7 days, shifts without a professional or with the professional on
  leave. **Ver escala completa →** (see full roster) opens the Roster.
- **Ocupação das agendas** (schedule occupancy): slots offered and taken over the next 7 days.

## 5. Programmes, surveillance and vaccination

- **Programas de saúde** (health programmes):
  - patients followed up and those up to date, by programme;
  - click the programme to see it by unit;
  - **Gerenciar →** (manage) opens Health programmes.
- **Vigilância** (surveillance): reports in the period, those closed on time and the breakdown by territory. **Abrir →** (open)
  opens Surveillance.
- **Vacinação** (vaccination):
  - doses given in the period and today's coverage from the register;
  - vaccines with high dropout and vaccination campaigns;
  - **Ver sala de vacina** (view vaccination room) opens the Vaccination room.

## 6. Productivity and cost

- **Produtividade por unidade** (productivity by unit): encounters completed in the period and the change over the previous one.
- **Custo por unidade** (cost per unit):
  - the cost per encounter of the previous accounting month and, when available, the cost per patient-day;
  - the **acima da média** (above average) mark flags a unit 30% or more above the average for its unit type.

## 7. Common messages

| Message | What it means |
|---------|---------------|
| *Sem atendimentos no período.* — no encounters in the period | there are no completed encounters in the chosen scope |
| *Nenhuma ocorrência na escala dos próximos 7 dias.* — no roster issues in the next 7 days | the roster is complete |
| *Nenhum programa oferecido pelas unidades do seu acesso.* — no programme offered by your units | the units in scope offer no programme |
| *Nenhum custo apurado nas unidades do seu acesso.* — no cost calculated for your units | the previous month has not yet been calculated in Finance |
