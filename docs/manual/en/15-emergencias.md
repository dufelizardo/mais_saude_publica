# Emergencies and disasters

> **Version:** 1.15.0 · [Português](../pt-BR/15-emergencias.md)

Menu: **Vigilância › Emergências** (Surveillance › Emergencies). It covers the cycle of a public health emergency or a disaster,
such as a flood, an epidemic or a mass gathering:

1. the recorded **event**;
2. the active **emergency**, with its response level;
3. the impact on the network, actions, mobilisations and affected people;
4. demobilisation, closure and the after-action report.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Managers, nursing coordination, epidemiological, environmental and health inspection surveillance | record events |
| Manager | converts the event into an emergency, changes the level, activates plans and closes |
| Managers and nursing coordination | coordinate: actions, mobilisations, capacity and people |
| Manager | keeps contingency plans, emergency capacity and catalogues |

The screen has the tabs:

- **Emergências ativas** (active emergencies), **Eventos** (events) and **Encerradas** (closed);
- **Planos de contingência** (contingency plans), **Capacidade emergencial** (emergency capacity) and **Catálogos**
  (catalogues).

## 1. Recording an event

1. Click **Registrar evento** (record event).
2. Choose the **Tipo** (type) and the **Fonte** (source): surveillance, health unit, Civil Defence, manager, professional or
   external system.
3. Enter the **Local** (place, for example *Bairro Ribeirinho*) and **Ocorrido em** (occurred at; default: now).
4. Write the **Descrição** (description).
5. Optionally, draw the event area: the polygon on the map.
6. Save.

An event that came from a Surveillance outbreak shows **Ligado ao surto da Vigilância** (linked to the Surveillance outbreak).

**On the Eventos tab**, each event has:

- **Monitorar** (monitor);
- **Descartar** (discard), with the reason;
- **Converter em emergência** (convert into an emergency).

## 2. Converting into an emergency (manager)

1. On the event, click **Converter em emergência**.
2. Enter the **Nome da emergência** (name, for example *Enchente Região Norte* — North Region flood), the **Nível inicial**
   (initial level), the **Matrícula do responsável** (staff number of the person in charge) and the **Justificativa**.
3. For an event that has not yet started (for example, a mass gathering), tick **Planejar** (plan). The emergency stays planned:
   fill in the details and structures, and use **Iniciar operação** (start operation) at the time.
4. Confirm.

The affected territory starts from the event area and the micro-areas it covers.

## 3. The active emergency

Open the emergency to see the level, start and latest impact. The main actions are:

| Action | What it does |
|--------|--------------|
| **Nova avaliação** (new assessment) | records the impact: affected population, impact level, affected units, unavailable beds and staff, critical medicines, equipment, transport and infrastructure |
| **Território** (territory) | draws or adjusts the polygon; the micro-areas it covers join the affected territory |
| **Mudar nível** (change level) | the new response level, with the justification; it is kept in the **Histórico de nível** (level history) |
| **ESPIN e decreto** | records the number and date of the public health emergency of national concern (ESPIN) and of the published decree |
| **Ativar plano** (activate plan) | the current contingency plan, with the authority (for example, *Secretário de Saúde* — Health Secretary) and the justification |

> **ESPIN and decree:** the system records them; declaring is an act of the public authority and Civil Defence.

An activated plan that does not cover the current level shows the warning **Não cobre o nível atual** (does not cover the current
level).

## 4. Coordinating the response

**Actions:**

- **Gerar ações do plano** (generate plan actions) brings the actions planned for the level;
- **Nova ação** (new action) creates a one-off action, with priority and deadline.

Each action has **Iniciar** (start), **Concluir** (complete) and **Cancelar**. The **Atrasada** (overdue) mark warns about those
past the deadline.

**Mobilisation:**

1. Use **Mobilizar** (mobilise) to assign professionals, teams or vehicles to the emergency.
2. A mobilised professional gets the shift authorised in the Roster.
3. A patient transfer can be linked to the emergency (**Ligar transferência** — link transfer).

**Emergency capacity:** **Ativar capacidade** (activate capacity) activates the extra beds or resources the unit can open (item 7).

**Stock:** reservations are made through each unit's stock (Administrativo › Estoque). Consumption marked with the emergency
appears here.

## 5. Affected people

- **Vulneráveis da área afetada** (vulnerable people in the affected area):
  - people in the affected territory, with the markers: older person (60+), child (under 5), pregnant woman, in a programme,
    dialysis or serial therapy and oxygen;
  - filter by marker or tick **Incluir quem não tem marcador** (include those without a marker);
  - **Baixar CSV** generates the list for field teams;
  - **Deslocar** (displace) records that the person left home.
- **Deslocados** (displaced people):
  - **Registrar deslocado** (record displaced person) keeps who is away from home, with the place: home, health unit or shelter;
  - **Realocar** (relocate) changes the place;
  - returning ends the displacement.
- **Compromissos nas unidades afetadas** (appointments at affected units): bookings and regulations for the next 30 days at units
  out of operation. Rerouting is done by the Schedule or by Regulation.
- **Regulações priorizadas** (prioritised regulations): a displaced person's regulation can be prioritised. **Retirar** (remove)
  takes the priority away.

## 6. Demobilising, closing and the after-action report

- **Iniciar desmobilização** (start demobilisation):
  - completes open mobilisations, and the Roster stops accepting new shifts without a posting;
  - deactivates emergency capacity and plans;
  - afterwards, only what was left open is completed and the report is written.
- **Encerrar** (close, directly): completes mobilisations and deactivates capacity and plans, with the **Motivo do encerramento**
  (reason). To control the return step by step, use **Iniciar desmobilização**.
- **Cancelar evento** (cancel event): a planned emergency that did not start is cancelled, and the planned structures are
  deactivated.

**After-action report** (closed emergency):

1. The report shows the figures:
   - duration and time from event to activation;
   - actions completed on time and capacity activated;
   - the event's estimated attendance.
2. Write the **Resumo** (summary), the **Problemas** (problems) and the **Lições aprendidas** (lessons learned).
3. Use **Adicionar recomendação** (add recommendation), linked or not to a plan (**Revisar plano** — review plan).
4. Use **Salvar rascunho** (save draft) and, at the end, **Finalizar** (finish). **Exportar números (CSV)** downloads the figures.

## 7. Plans, capacity and catalogues (manager)

**Contingency plans:**

1. **Novo plano** (new plan): the **Código**, the **Nome**, the **Tipo de emergência**, validity, objective and units.
2. For each level, the **Critérios e ações** (criteria and actions), the **Recursos necessários** (resources needed) and the
   **Contatos** (contacts).
3. The plan goes through draft, **Publicar** (publish), **Revisar** (review: new version) and **Revogar** (revoke, with the
   reason).

The **Em uso** (in use) / **Todas as versões** (all versions) filter shows the versions.

**Emergency capacity:** the capacity each unit can activate in an emergency.

1. **Nova capacidade** (new capacity): the **Unidade**, the **Recurso** (resource), the **Capacidade normal** (normal capacity)
   and how much is **Ativável a mais** (activatable on top).
2. An inactive capacity cannot be activated in an emergency.

**Activation happens in the emergency, by the coordination.**

**Catalogues:**

- **Tipos de emergência** (emergency types): code, name, category (disaster or health), the COBRADE disaster code and the profile.
  A mass gathering is planned before it happens and has event details.
- **Níveis de resposta** (response levels): the level, description and order.

Inactive items do not enter new events or level changes.

## 8. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Informe o tipo, a fonte e a descrição.* — enter type, source and description | the event is incomplete | fill in the three fields |
| *Informe o motivo do descarte.* — enter the reason for discarding | the discard has no reason | write the reason |
| *Informe o nome, o nível e a justificativa.* — enter name, level and justification | the conversion is incomplete | fill in the three fields |
| *Escolha o nível e justifique.* — choose the level and justify | the level change is incomplete | choose the level and justify |
| *Justifique o início da operação.* — justify starting the operation | the planned emergency has no justification | write the justification |
| *Escolha o plano e informe a autoridade e a justificativa.* — choose the plan, authority and justification | the plan activation is incomplete | fill in the three fields |
| *Escolha o nível de impacto.* — choose the impact level | the assessment has no level | choose the impact level |
| *Informe a unidade, a capacidade normal e quanto se pode ativar.* — enter unit, normal capacity and activatable amount | the capacity is incomplete | fill in the fields |
| *Encerre a emergência antes* — close the emergency first | the report only closes after closure | close the emergency |
| *Seu acesso não inclui esta operação.* — your access does not include this operation | your profile does not allow the action | ask for the profile, if your role needs it |
