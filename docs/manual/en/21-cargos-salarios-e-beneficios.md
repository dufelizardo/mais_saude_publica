# Jobs, salaries and benefits

> **Version:** 1.15.0 · [Português](../pt-BR/21-cargos-salarios-e-beneficios.md)

Menu: **Recursos Humanos › Cargos & salários** (jobs and salaries) and **Recursos Humanos › Benefícios** (benefits). This is the
network's pay structure:

- salary categories and jobs, with the salary table;
- length-of-service bonus rules (*anuênio*);
- benefit types, with their current value.

**Who uses it:** HR management (manager).

> **Values have validity dates.** A pay rise does not erase the previous value: it is added as a new dated value, and payroll uses
> the value valid in the accounting month.

## 1. Salary categories

A salary category is the level at which a collective agreement applies.

1. On the **Categorias salariais** tab, create the category with the **Nome** (name, for example *Profissionais de enfermagem* —
   nursing staff).
2. If any, enter the **Convenção coletiva** (collective agreement, for example *SINDSAUDE-SP 2026*).

## 2. Jobs and salary table

1. On the **Cargos** (jobs) tab, create the job:
   - the **Categoria salarial** (salary category);
   - the **Nome do cargo** (job name, for example *Enfermeiro* — nurse);
   - the **Descrição** (description): the job's duties, which also appear in recruitment vacancies.
2. Click the job to open the **Tabela salarial** (salary table). It shows the **Valor base vigente** (current base value) and the
   **Histórico** (history), with each value's validity, amount and reason.
3. **Registrar novo valor** (record new value):
   - the **Valor base (R$)** (base value, for example *4.850,00*);
   - **Vigência a partir de** (valid from);
   - the **Motivo** (reason).

   The previous value stays in the history.

> Without a current base value, the job warns: *Este cargo ainda não tem valor base vigente.*

## 3. Length-of-service bonus rules

The *anuênio* is the length-of-service bonus, **one rule per salary category**.

1. On the **Regras de anuênio** tab, use **+ Nova regra** (new rule).
2. Choose the **Categoria salarial**. A rule's category cannot change later.
3. Enter the **% por ano** (% per year, for example *1*) and, optionally, the **Teto em anos** (cap in years, for example *35*).
   Without a cap, the bonus grows as long as there is service time.

The **Categorias sem anuênio** (categories without bonus) card shows those without a length-of-service bonus.

## 4. Benefits

The screen shows the network catalogue's benefit types, who pays and the current value. **Each professional's enrolment is on
their profile** (module 19, Benefits tab).

1. Create the type with the **Nome** (for example, *Vale-alimentação* — meal voucher) and **Quem custeia** (who pays):
   - **Empresa** (employer): the network pays everything;
   - **Compartilhado** (shared): the network and the professional split it;
   - **Profissional** (professional): the professional pays, and the network only intermediates.
2. After registering, the drawer opens on the values so you can record the current value.
3. **Registrar novo valor:** the **Valor (R$)** (for example, *800,00*), **Vigência a partir de** and, optionally, the reason
   (for example, *reajuste anual* — annual increase).

Search by name and filter by who pays.

## 5. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Este cargo ainda não tem valor base vigente.* — this job has no current base value | the salary table is empty | record the base value |
| *Este benefício ainda não tem valor vigente.* — this benefit has no current value | the benefit has no value | record the value |
| *Nenhuma regra de anuênio cadastrada* — no bonus rule registered | no category has a length-of-service bonus | create the rules, if the category has one |
