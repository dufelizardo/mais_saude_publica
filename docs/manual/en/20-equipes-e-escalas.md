# Teams and rosters

> **Version:** 1.15.0 · [Português](../pt-BR/20-equipes-e-escalas.md)

Menu: **Recursos Humanos › Equipes** (teams) and **Recursos Humanos › Escalas** (rosters).

- **Equipes:** the units' health teams, with their members and each type's minimum composition.
- **Escalas:** weekly hours, on-call shifts and HR holidays and leave, by unit.

**Who uses it:** HR management and nursing coordination maintain both. Those who read HR or administration can view them.

## 1. Teams

**The cards at the top:**

- **Equipes ativas** (active teams) and **Equipes eSF** (Family Health teams);
- **Profissionais vinculados** (linked professionals) in active teams;
- **Equipes incompletas** (incomplete teams), without the type's minimum composition.

Search by team, INE (national team identifier) or member, and filter by unit and status: active, incomplete, inactive or all.

**Registering a team:**

1. Click to create a team.
2. Choose the **Unidade** and the **Tipo** (type).
3. Enter the **Nome** (name, for example *SF-12*) and the **INE** (10 digits, optional).
4. Choose the **Coordenação** (coordinator), or **Sem coordenação** (no coordinator).
5. Save.

**Members:**

1. Use **Adicionar membro** (add member), with professionals posted at the unit.
2. Choose the **Função** (role) and the **Vínculo** (relationship).
3. To remove someone, use **Saída** (exit). The team history is kept.

**What the team shows:**

- the **Microáreas · território adscrito** (micro-areas, enrolled territory), from the team's area in the Territory;
- **Apoio matricial** (matrix support): whom the team supports and by whom it is supported;
- the **Reunião de equipe** (team meeting), for example weekly and recurring;
- the **Programas vinculados** (linked programmes; participation is registered in Health programmes);
- **Cobertura vacinal** (vaccination coverage) and homogeneity across vaccines, for those who vaccinate or view indicators at the
  unit.

**Ver escala** (view roster) opens the team's roster.

## 2. Rosters

**To start:** choose the **Unidade** and, optionally, the **Equipe**. The roster lists those with a current posting at the unit,
or the members of the chosen team. Move through the week.

**The cards at the top:**

- **Horas previstas · semana** (planned hours this week);
- **Cobertura dos turnos** (shift coverage);
- **Plantões e sobreavisos** (on-call and standby shifts);
- **Em férias / licença** (on holiday / leave) this week, from HR.

**The tabs:**

- **Jornada semanal** (weekly hours);
- **Plantões** (on-call shifts);
- **Férias & licenças** (holidays and leave);
- **Banco de horas** (hour bank).

**Shifts on the grid:** morning, afternoon, day, night, on-call, standby, training, vacancy and day off. Holidays and leave come
from HR.

**Adding a shift:**

1. Click the professional's cell for the day.
2. Choose the professional or **Vaga aberta (designar depois)** (open vacancy, assign later).
3. Choose the **Tipo** (type). Night and on-call only at 24-hour units.
4. Enter:
   - the **Data** (date), **Início** (start) and **Fim** (end; before the start means it ends the next day);
   - the **Intervalo** (break) for rest and meals, which does not count towards hours. Brazilian labour law (CLT) requires 15
     minutes above 4 hours and 1 hour above 6 hours;
   - the **Equipe** and **Atividade ou local** (activity or place, for example *Imunização, Sala de PA*).
5. Save. Posting, leave and overlaps are checked on saving.

> **Warnings that do not block:** hours above the contracted ones and less than 11 hours of rest between shifts.

**On-call shifts:**

- **Designar** (assign) puts a professional in the open vacancy.
- **Trocar** (swap) changes the person on call, with the **Motivo da troca** (reason, for example *troca combinada entre
  plantonistas* — swap agreed between staff).
- **Não coberto** (not covered) marks a vacancy with nobody.

**Building the week faster:**

- **Aplicar modelo** (apply template): choose the **Profissional** and the **Modelo de jornada** (working pattern), suggested from
  the contracted hours in the posting. 12x36 only applies at 24-hour units.
- **Copiar semana** (copy week): choose the **Destino** (destination) and the source week. Any day counts for the whole week,
  Monday to Sunday.

In both cases, days already past and days with leave, without posting, outside the unit's hours or with another shift are left
out.

**Exportar** (export) downloads the week's roster. **Mobilização** (mobilisation) appears when the professional is in an
emergency (module 15).

## 3. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| **Equipes incompletas** (incomplete teams) | the team lacks the type's minimum composition | add the missing members |
| *Inclua profissionais com lotação na unidade.* — add professionals posted at the unit | the team has no members | add members posted at the unit |
| **Não coberto** (not covered) | the on-call shift has no professional | assign someone |
| Shift refused on saving | the professional has no posting, is on leave or has another shift at that time | choose another professional or time |
| Hours or rest warning | hours exceed the contract or rest is under 11 hours | check it; the warning does not block saving |
