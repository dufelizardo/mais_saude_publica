# Audit

> **Version:** 1.15.0 · [Português](../pt-BR/37-auditoria.md)

Menu: **Administração › Auditoria** (Administration › Audit). It is the trail of what happened in the system:

- **reads** of health or personal data details;
- **changes**;
- **denied access**;
- **sign-ins**.

It also shows **alerts** about suspicious patterns.

**Who uses it:** those with the audit permission. It is **outside all default profiles**: whoever administers the system does not
audit themselves, and the permission is granted separately, to whoever management designates. The number on the menu item shows
open alerts.

> **The event does not store content:** neither submitted data nor clinical text. The record it points to stays in the system,
> with its own history.

## 1. Events

**The cards:**

- **Eventos** (events), newest first;
- **Leituras de dado de saúde** (health data reads): details opened (health record, encounter, patient…);
- **Alterações** (changes): creations, changes, amendments and revocations;
- **Acessos negados** (denied access).

**Filtering:**

- by **CPF de quem fez** (CPF of whoever did it);
- by patient (name, CPF or SUS card);
- by **Ação** (action), **Resultado** (result: allowed or denied) and **Unidade**;
- by period, up to the **Até** (until) date.

**Limpar filtros** (clear filters) starts over.

Each event shows **Quando** (when), **Quem** (who), **O quê** (what), the patient, the unit and the result. When opened, it shows:

- the **Motivo** (reason, for justified access);
- the **Origem (IP)**, the **Resposta** (response) and the **Rota** (route);
- the **Registro** (record) it points to.

Use **Tudo desta pessoa** (everything by this person) or **Tudo deste paciente** (everything about this patient) to see the full
trail of either.

**Exporting:** the current filter downloads as CSV (opens in Excel). Filtered by patient, it serves as an access report for the data
subject (LGPD, Brazil's data protection law). The query itself and the export are recorded in the audit trail.

## 2. Alerts

**What it is for:** suspicious patterns found in the trail. Each alert stays open until someone concludes it, with an opinion.

**The types:**

| Alert | What it indicates |
|-------|-------------------|
| Recusas seguidas (repeated refusals) | many denied accesses (no permission or out of scope) in a short time |
| Login recusado em sequência (repeated failed sign-ins) | many wrong sign-in attempts: someone may be trying to guess the password |
| Leitura em massa (mass reading) | data of many different patients opened in a short time |
| Leitura de madrugada (night-time reading) | health data reads in the early hours, outside 24-hour units |
| Acesso justificado (justified access) | health record opened without a care relationship: supervision reviews the reason |

**The cards:**

- **Abertos** (open), awaiting analysis;
- **Severidade alta** (high severity): open and notified by e-mail;
- those detected in the **Últimos 7 dias** (last 7 days);
- **Procedentes** (upheld), confirmed in analysis.

**Analysing:**

1. Filter by status, type and severity (high or medium), and open the alert.
2. Check the events: the first, the last and how many.
3. Choose the **Conclusão** (conclusion):
   - **Procedente** (upheld): the access is not justified and calls for action;
   - **Improcedente** (not upheld): there was a reason.
4. Write the **Parecer** (opinion: what was checked and the action taken) and click **Concluir análise** (conclude analysis).

**Verificar agora** (check now) looks for new alerts immediately.

## 3. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| *Nenhum evento* — no events | the filters found nothing | adjust the filters |
| *Nada aguardando análise no seu escopo.* — nothing awaiting analysis in your scope | no open alert at your units | nothing to do |
| *Não foi possível exportar a auditoria.* — the audit could not be exported | the export failed | try again |
