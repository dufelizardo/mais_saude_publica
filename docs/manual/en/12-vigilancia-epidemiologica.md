# Epidemiological surveillance

> **Version:** 1.15.0 · [Português](../pt-BR/12-vigilancia-epidemiologica.md)

Menu: **Vigilância › Vigilância epidemiológica** (Surveillance › Epidemiological surveillance). It is the reporting,
investigation and closure of notifiable conditions. **The network detects, the professional reports.** It also covers outbreaks,
the map and epidemic curve, negative reporting, worker health and mandatory communication of violence.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Those who provide care (physicians, nursing, oral health, psychology, social work) | report |
| Epidemiological surveillance | investigates, classifies, closes, opens outbreaks and handles negative reporting |
| Managers and surveillance | keep the catalogue of conditions |

**The cards at the top:**

- the **Notificações abertas** (open reports: reported or under investigation);
- those **Em investigação** (under investigation) and open outbreaks;
- **Encerramento atrasado** (late closure);
- **Semanas sem declaração** (weeks without a declaration).

Filters are the **Unidade**, the **Agravo** (condition) and the **Situação** (status: reported, under investigation or closed).

## 1. Reporting

1. Click **Notificar** (report). It can also be done from the **Sugestões** (suggestions) tab (item 6).
2. Under **Paciente**, search by name, CPF or SUS card.
3. Choose the **Agravo** and the **Unidade notificante** (reporting unit).
4. Enter the **Primeiros sintomas** (symptom onset) and the **Gravidade** (severity), and the **Local provável** (probable place:
   address or landmark), if known.
5. If the condition has a **Ficha específica** (specific form), it opens with its own fields.
6. Tick **Relacionado ao trabalho** (work-related) when the case is work-related. The report then asks for the occupation,
   employer and place of the event; for accidents, the **Data do acidente** (accident date).
7. Save.

> **AEFI and immunisation errors start from the dose:** open the patient's vaccination card and use **Notificar ESAVI** (module
> 10).

A condition marked **Imediata** (immediate) must be reported within 24 hours. A **Sensível** (sensitive) condition's details are
only for surveillance and the reporter.

## 2. The report list

The **Notificações** tab shows the patient, condition, report date, deadline and status. The marks **Imediata**, **Sensível**
and **Atrasado** (overdue) help prioritise.

The **Boletim** (bulletin, aggregates only) shows those reported, closed, confirmed and discarded.

## 3. Investigating, classifying and closing (surveillance)

Open the report and use the sections:

- **Investigação** (investigation): exposure and history, the **Local provável de infecção** (probable place of infection) and the
  **Medidas adotadas** (measures taken). Click **Salvar investigação** (save investigation).
- **Contatos** (contacts):
  1. use **Registrar contato** (record contact), linked to the record or **Sem cadastro** (unregistered, with the name);
  2. enter the phone, address, exposure and exposure date;
  3. the contact has a monitoring status and can become a report (**Notificar**).

  The **Monitoramento vencido** (monitoring overdue) mark warns about those past the condition's period.
- **Medidas de controle** (control measures):
  1. use **Registrar medida** (record measure): the measure (for example, vaccination blockade, isolation, chemoprophylaxis,
     vector control, active search or health education), deadline, description and person responsible (blank means you);
  2. each measure has **Iniciar** (start), **Concluir** (complete, with the result) and **Cancelar**.

  **Prazo vencido** (deadline passed) warns about delays.
- **Classificar o caso** (classify the case): the **Classificação** (suspected, probable, confirmed, discarded or inconclusive),
  the **Critério** (criterion: laboratory, clinical-epidemiological or clinical) and the justification.
- **Encerrar a notificação** (close the report): the **Evolução** (outcome: cure, death from the condition, death from another
  cause or unknown).
  - For AEFI, also the **Causalidade (OMS)** (WHO causality) and the **Conduta sobre o esquema** (schedule decision): keep,
    continue with precaution (CRIE) or contraindicate further doses.

The **Linha do tempo** (timeline) keeps every step.

## 4. Outbreaks

The **Surtos** (outbreaks) tab lists outbreaks with the condition, start, cases and status. **Medida vencida** (overdue measure)
warns when a control measure is late.

**Abrir surto** (open outbreak; surveillance):

1. Click **Abrir surto** and choose the condition and the **Unidade responsável** (responsible unit).
2. Enter the **Nome** and the **Tipo** (type):
   - **Comunitário** (community), in the territory;
   - **Institucional** (institutional), in an establishment: enter the **Nome do estabelecimento** (school, nursery, health
     service, long-term care home, prison, shelter, company…) and the address.
3. Write the **Definição de caso** (case definition): who is included in the outbreak.
4. Link the condition's cases and record the outbreak's control measures.

## 5. Map and epidemic curve

The **Mapa** tab shows **Casos por microárea** (cases by micro-area), filtered by condition and classification. An outbreak's
micro-area without cases also appears. From there you can **Abrir surto** with the micro-area's cases. Below is the **Curva por
semana epidemiológica** (curve by epidemiological week).

## 6. Suggestions

The **Sugestões** tab brings:

- admissions and test orders from the last 30 days with a notifiable condition ICD code;
- mental health crises with a pending report.

Use **Notificar** on each one. **Reporting is always done by the professional:** the system only suggests. A report made from a
mental health crisis marks the crisis as reported.

## 7. Negative reporting

**What it is for:** declaring that the unit had no case of a notifiable condition in the epidemiological week (Sunday to
Saturday).

1. On the **Notificação negativa** (negative report) tab, see the **Semanas sem notificação nem declaração** (weeks with no report
   or declaration).
2. Use **Declarar sem casos** (declare no cases) for each week.

**Declaradas** (declared) stay in the history.

## 8. Worker health

The **Saúde do trabalhador** (worker health) tab brings together work-related reports:

- the worker, condition, occupation, employer, event and status;
- the **Acompanhamento sorológico a fazer** (serological follow-up to do) after an accident with biological material: the stages
  at 6 weeks, 3 months and 6 months;
- the **Medidas do trabalho com o prazo vencido** (work measures past their deadline);
- the **Riscos ocupacionais** (occupational risks): the type and the agent, with **Registrar o risco** (record risk) and
  **Controlado** (controlled, with how the risk was controlled);
- the **Acidentes de servidores sem notificação** (staff accidents without a report), with **Notificar**. This list covers the
  whole network: only surveillance with access to all units sees it.

## 9. Mandatory communication of violence

A violence report requires **mandatory communication** depending on the victim:

| Victim | Who to notify |
|--------|---------------|
| under 18, including self-inflicted injury | Guardianship Council (*Conselho Tutelar*) |
| older person | police, Public Prosecutor's Office or the older persons' council |
| woman aged 18 to 59, except self-inflicted | public security, within 24 hours |

Adult men and self-inflicted injury in adults have no mandatory communication. The **Comunicações obrigatórias da violência
pendentes** (pending mandatory communications) notice shows what is missing, with the **Vencida** (overdue) mark.

1. In the report, under **Comunicação obrigatória** (mandatory communication), choose the **Instituição** and click **Comunicar**
   (communicate).
2. To cancel a communication, enter the reason.

Institutions come from the intersectoral network catalogue (**Administrativo › Rede intersetorial**). Without an active
institution of an accepted type, register it first.

## 10. Catalogue of conditions (managers and surveillance)

1. Click **Novo agravo** (new condition) and enter:
   - the **Código**, the **Nome** and the **Periodicidade** (frequency): immediate (within 24 hours) or weekly;
   - the **CID-10** codes, separated by commas (for example, *A90, A91*);
   - **Encerrar em (dias)** (close within days) and **Monitorar contatos por (dias)** (monitor contacts for days; blank means no
     limit);
   - the **Critérios de classificação aceitos** (accepted classification criteria).
2. Tick, depending on the condition:
   - **Notificação compulsória** (mandatory reporting);
   - **Sensível** (sensitive): details are only for surveillance and the reporter;
   - **Trabalho** (work): the report asks for occupation and labour market status.
3. Build the specific **Ficha** (form) with **Adicionar campo** (add field): key, type (yes/no, text, number, date, single option
   or multiple), label, options and whether it is required.
4. Leave **Ativo** (active) ticked.

## 11. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Escolha o paciente na lista.* — choose the patient from the list | the report has no patient | search and choose the patient |
| *Escolha o agravo.* / *Escolha a unidade notificante.* — choose the condition / reporting unit | condition or unit missing | choose both |
| *Escolha a gravidade.* — choose the severity | the report has no severity | choose the severity |
| *Informe a data do acidente.* — enter the accident date | the work case has no date | enter the date |
| *Escolha a classificação.* / *Escolha a evolução.* — choose the classification / outcome | classification or closure incomplete | choose the option |
| *Escolha a causalidade.* / *Escolha a conduta sobre o esquema.* — choose causality / schedule decision | AEFI closure incomplete | choose causality and decision |
| *Sem cadastro, informe o nome do contato.* — unregistered: enter the contact's name | an unregistered contact has no name | write the name |
| *Descreva a medida.* — describe the measure | the control measure is empty | describe the measure |
| *Nenhuma instituição ativa de tipo aceito…* — no active institution of an accepted type | the intersectoral catalogue lacks the institution | register the institution |
| *Escolha pelo menos um critério.* — choose at least one criterion | the condition has no classification criterion | tick the accepted criteria |
