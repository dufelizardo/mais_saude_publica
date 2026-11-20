# Quality

> **Version:** 1.15.0 · [Português](../pt-BR/18-qualidade.md)

At the top of the menu are three service quality screens:

- **Segurança do paciente** (patient safety): incident reporting and analysis by the Patient Safety Office (ANVISA RDC
  36/2013);
- **Não conformidades** (non-conformities): 5W2H action plans and effectiveness checks (PDCA);
- **Indicadores de qualidade** (quality indicators): targets by unit, a 12-month series and protocol adherence.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Every professional in the network | reports incidents; carries out the non-conformity actions they are responsible for |
| Patient Safety Office | classifies and analyses incidents, reports to Notivisa and audits protocols |
| Quality management (managers and the office) | records and handles non-conformities, sets targets and keeps checklists |
| Indicator readers | see the dashboards |

## 1. Reporting an incident

> **Reporting is for learning and prevention, not for punishment.** You can report without identifying yourself.

1. In **Segurança do paciente**, click **Notificar** (report).
2. Enter:
   - the **Unidade**, **Quando ocorreu** (when it happened) and the **Local** (place, for example *enfermaria 2, leito 4* — ward 2,
     bed 4);
   - the **Tipo** (type): patient identification, communication, medication, surgery or procedure, healthcare-associated
     infection, fall, pressure injury, diet, equipment, documentation or other;
   - the **Dano percebido** (perceived harm; optional, or *Não sei* — don't know) and the **Paciente** (optional).
3. Describe **O que aconteceu** (what happened) and **O que foi feito na hora** (what was done at the time).
4. Optionally, tick **Notificar sem me identificar** (report anonymously).
5. Click **Notificar incidente** (report incident). The incident gets a protocol number.

## 2. Analysing an incident (Patient Safety Office)

The list shows the protocol, occurrence, type, classification and status: received, under analysis, completed or discarded. **a
classificar** (to classify) marks what has not been classified yet.

1. Open the incident.
2. **Classificar** (classify):
   - the **Classificação (OMS)** (WHO classification);
   - the **Natureza** (nature): risk circumstance, near miss, no-harm incident or adverse event;
   - the **Dano** (harm): none, mild, moderate, severe or death;
   - **Never event**, for an event that should never happen.
3. **Análise** (analysis):
   - tick the **Fatores contribuintes** (contributing factors): patient, professional, team, task or technology, environment,
     organisation or external;
   - write the **Causa raiz** (root cause) and **Lições aprendidas** (lessons learned);
   - click **Concluir análise** (complete analysis).
4. **Notificação ao Notivisa** (report to Notivisa): adverse events have a deadline to be reported to Notivisa (ANVISA). Record
   the **Número no Notivisa** (Notivisa number) and click **Registrar**. The **Vencido** (overdue) mark warns of delays.
5. If needed, **Abrir não conformidade** (open non-conformity) from the incident.
6. **Descartar** (discard): the reason (duplicate, out of scope).

The **Painel** (dashboard) shows reports, **Eventos adversos** (adverse events) with harm to the patient, **Notivisa pendente**
(Notivisa pending) and counts by type, nature and harm.

## 3. Non-conformities

**Recording (quality management):**

1. Click **Registrar não conformidade** (record non-conformity).
2. Choose the **Unidade** and the origin: safety incident, health inspection, internal audit, complaint, indicator off target or
   other. For an indicator, enter the **Indicador** and the **Competência** (month); for adherence, the **Protocolo**.
3. Describe the **Não conformidade** and, if any, the **Correção imediata** (immediate correction).
4. Choose the severity (minor, major or critical) and enter the **Matrícula do responsável** (staff number of the person
   responsible) and the date it was **Identificada em** (identified on).
5. Click **Registrar**.

The **suggestions** bring what calls for a non-conformity (for example, an indicator off target): **Abrir não conformidade** on
each one.

**Handling it:**

1. **Análise de causa** (cause analysis):
   - tick the **Categorias (6M)**: method, machine, manpower, material, measurement and environment;
   - write the **Causa** (cause);
   - click **Salvar análise** (save analysis).
2. **Plano de ação (5W2H)** (action plan): use **Nova ação** (new action) for each action:
   - **O quê** (what), **Por quê** (why) and **Quem (matrícula)** (who, staff number);
   - **Quando (prazo)** (when, deadline), **Onde** (where) and **Como** (how);
   - **Quanto custa (R$)** (how much).
3. Each action has **Iniciar** (start) and **Concluir** (complete), with the **Evidência do que foi feito** (evidence of what was
   done).
4. **Verificação de eficácia** (effectiveness check):
   - **Eficaz: encerrar** (effective: close) closes the non-conformity;
   - **Não eficaz** (not effective) requires a note, and the action plan continues.

**My actions:** each professional sees the actions they are responsible for, with **Iniciar** and **Concluir**. With none, the list
says *Nenhuma ação sob sua responsabilidade*.

**Statuses:** open, being handled, under verification, closed or cancelled. Cancelling belongs to quality management, with a
reason. The **Painel** shows **Ações atrasadas** (overdue actions), **Eficácia na 1ª verificação** (effectiveness at the first
check) and counts by origin, severity and status.

## 4. Quality indicators

**Dashboard:**

1. Choose the **Unidade** and the **Competência** (month). Targets apply per unit: choose one to compare.
2. Each indicator shows the **Valor** (value), the **Meta** (target), the **Situação** (status) and the **12 meses** (12-month)
   series.
3. For an indicator off target, use **Abrir não conformidade**.

**Targets (quality management):**

1. Choose the unit at the top of the page.
2. Use **Definir meta** (set target): the indicator, the target, the protocol (for adherence) and **Vigente desde** (valid from).

**Protocol audits (Patient Safety Office):**

1. Use **Registrar auditoria** (record audit): the protocol, the date and the answer to each checklist item, with an optional
   note.
2. The protocol's **Adesão** (adherence) comes from the audits of the last three months.

**Checklists (quality management):**

1. Use **Novo checklist** (new checklist): the **Código**, the **Nome** and the items, one per line, in the format *code; text*.
2. To change it, use **Nova versão** (new version) and **Publicar versão** (publish version).

## 5. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Informe a unidade, quando ocorreu, o local, o tipo e o que aconteceu.* — enter unit, when, place, type and what happened | the report is incomplete | fill in the fields |
| *Marque ao menos um fator contribuinte.* — tick at least one contributing factor | the analysis has no factor | tick the factors |
| *Informe o número da notificação no Notivisa.* — enter the Notivisa number | the Notivisa record is empty | enter the number |
| *Informe a unidade, a não conformidade e a matrícula do responsável.* — enter unit, non-conformity and staff number | the record is incomplete | fill in the three fields |
| *Informe o quê, por quê, quem e o prazo.* — enter what, why, who and deadline | the 5W2H action is incomplete | fill in the required fields |
| *Informe a evidência do que foi feito.* — enter the evidence | completing the action needs evidence | describe what was done |
| *A verificação não eficaz pede a observação.* — a not-effective check needs a note | the note is missing | write why it was not effective |
| *Escolha a unidade no alto da página.* — choose the unit at the top of the page | the target or audit depends on the unit | choose the unit |
| *Responda todos os itens do checklist.* — answer all checklist items | the audit is incomplete | answer the items |
| *Escreva os itens como "código; texto", um por linha.* — write items as "code; text", one per line | the checklist format is wrong | correct the items |
