# Sectors and administrative model

> **Version:** 1.15.0 · [Português](../pt-BR/24-setores-e-modelo-administrativo.md)

Menu: **Administrativo › Setores** (sectors), **Administrativo › Modelo administrativo** (administrative model) and
**Administrativo › Necessidades de pessoal** (staffing needs).

- **Setores:** each unit's sectors, with the person in charge, and each professional's administrative responsibilities.
- **Modelo administrativo:** what each unit type does administratively: capabilities, processes and profiles.
- **Necessidades de pessoal:** what staff each unit needs, sent to HR for recruitment.

**Who uses it:** administrative management (manager). Sectors can be viewed by anyone signed in. Those who read administration can
view the model.

## 1. Sectors

**The cards at the top:**

- **Setores ativos** (active sectors) and **Unidades com setor** (units with sectors);
- sectors **Por tipo** (by type): administrative, care, support or technical;
- **Sem responsável** (no one in charge): active sectors without a person in charge.

**Registering a sector:**

1. Choose the **Unidade de saúde** (health unit).
2. Enter:
   - the **Nome** (name, for example *Farmácia* — pharmacy);
   - the **Código** (code, for example *FARM*);
   - the type;
   - the **Responsável (matrícula)** (staff number of the person in charge), if any.
3. Leave **Ativo** (active) ticked. An inactive sector no longer appears for selection in encounters and staffing needs.
4. Save.

The sector is used in encounters, the professional's posting, beds (the care sector) and cost centres.

## 2. Administrative responsibilities

These are roles such as **contract supervisor** or **committee member**, separate from the posting. A professional can have
several at the same time.

1. Use **+ Atribuir responsabilidade** (assign responsibility) and choose the **Profissional** (by name or staff number).
2. Enter:
   - the **Responsabilidade** (for example, *Fiscal de contrato* — contract supervisor);
   - the **Setor**;
   - the **Descrição** (for example, *contrato de limpeza nº 12/2026* — cleaning contract no. 12/2026);
   - the **Início** (start).
3. Save. To end it, use **Encerrar** (end) with the date. The responsibility stays in the professional's history.

In the sector list, **Responsáveis** (people in charge) shows the sector's responsibilities. **Trocar profissional** (change
professional) looks up another professional.

## 3. Administrative model

**The cards:**

- **Capacidades ativas** (active capabilities) and **Processos ativos** (active processes);
- **Perfis ativos** (active profiles), which are administrative structure templates;
- **Tipos de unidade com perfil** (unit types with a profile: primary care unit, emergency unit, hospital, CAPS…).

**The sections:**

- **Capacidades administrativas** (administrative capabilities): what administrative structure a unit can have (for example,
  procurement, stock, billing). Each has a code, name and description.
- **Processos administrativos** (administrative processes): the optional breakdown of each capability. The capability is valid
  even without them.
- **Perfis administrativos** (administrative profiles): structure templates, assigned to unit types.
- **Perfil por tipo de unidade** (profile by unit type): each health unit type uses one profile. Management levels (federal to
  regional) are not included. Use **Definir** (set) or **Trocar** (change).
- **Tipos de documento** (document types): what can be attached to a patient (module 01). An inactive type does not enter new
  documents.

An inactive item stays in the catalogue but is not offered for selection.

## 4. Staffing needs

**The cards:**

- **Necessidades registradas** (needs recorded) and **Posições pedidas** (positions requested);
- **Sem vaga vinculada** (without a linked vacancy);
- **Com vaga vinculada** (with a linked vacancy), in recruitment by HR.

**Recording a need:**

1. **Onde** (where): the **Unidade de saúde** and, optionally, the **Setor** (only those of the chosen unit).
2. **Quem** (who):
   - the **Cargo** (job), **Quantidade** (quantity) and **Jornada semanal (h)** (weekly hours);
   - the **Competências necessárias** (competencies needed).
3. Write the **Justificativa** (justification) and save.

**Vincular vaga** (link vacancy): when HR opens the vacancy in **Recrutamento** (module 22), link the need to it. The need becomes
**Vinculada** (linked).

Search by job, unit, sector or competency, and filter by unit or **Sem vaga** (no vacancy).

## 5. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| **Sem responsável** | the active sector has no one in charge | enter the staff number of the person in charge |
| *Nenhuma responsabilidade registrada* — no responsibility recorded | the professional or sector has no responsibility | assign one, if appropriate |
| **Sem perfil** on the unit type | the unit type has no administrative profile | set the profile |
| **Sem vaga** on the need | HR has not opened the vacancy yet | ask HR and link it later |
