# Network and agreements

> **Version:** 1.15.0 · [Português](../pt-BR/35-rede-e-pactuacao.md)

Menu: **Administrativo › Rede e pactuação** (network and agreements). It covers:

- the **PPI** (agreed and integrated programming): how much each performing unit offers per procedure per month and how the offer
  is shared among units and government levels;
- the network's **referrals**;
- **coverage**;
- **thematic networks**.

Regulation uses up the quota when authorising (module 05).

**Who uses it:** managers maintain it. The regulating physician views it.

## 1. Offers and quotas (PPI)

Choose the accounting month. **The cards at the top:**

- **Ofertado** (offered);
- **Pactuado** (agreed) in PPI quotas;
- **Utilizado** (used): authorisations in the month;
- **Extracotas** (over-quota): authorisations without balance.

**Registering the offer:**

1. Click **Nova oferta** (new offer).
2. Choose the **Procedimento** and the **Unidade executante** (performing unit), and enter the **Vagas no mês** (slots in the month).
3. Save. **Copiar do mês anterior** (copy from the previous month) repeats the previous month's offers.

**Agreeing the quota:**

1. On the offer, use **Pactuar cota** (agree quota).
2. Choose the beneficiary **Unidade ou esfera** (unit or government level) and enter the **Cota no mês** (monthly quota). A
   municipality's or region's quota applies to the units below it that have no quota of their own.
3. Save. The offer shows the beneficiary, quota, used and balance, with the **Esgotada** (used up) mark when it runs out.
   **Ajustar** (adjust) and **Remover** (remove) change the quota.

> **Without a registered offer, Regulation authorises without quota control.** With the quota used up, authorisation requires
> over-quota with a justification.

## 2. Network referrals

**What it is for:** stating where each origin sends each procedure.

1. Click **Nova referência** (new referral).
2. Choose the procedure, the **Origem (unidade ou esfera)** (origin: unit or level), the **Executante** (performing unit) and the
   **Início** (start).
3. Save.

**The rules:**

- **the most specific referral applies:** the unit's and, without it, the municipality's or region's;
- if the origin already has a referral for the procedure, the new one starts after it and ends it the day before;
- **Encerrar** (end) finishes the referral;
- without a referral, Regulation chooses the performing unit with no suggestion.

The list shows **Autorizações com referência** (authorisations with a referral), the validity and the status.

## 3. Coverage

The **Cobertura** (coverage) tab shows, per procedure with a PPI offer:

- the agreed offer, the quota used and what was **Fora da referência** (outside the referral);
- how many are **Na fila** (in the queue) and the **Espera (dias)** (waiting days).

**By origin:**

- enrolled population and the quota **Por mil** (per thousand) enrolled;
- requested, authorised and waiting;
- the **sem referência** (no referral) mark.

**Alertas** (alerts) point to what needs attention.

## 4. Thematic networks

The **Redes temáticas** tab shows, for each network (for example, the Psychosocial Care Network, RAPS), its components and the
points of each one. The points come from the units' Operating model (module 25). **Lacunas** (gaps) point to components without a
point in your access.

## 5. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| *Nenhuma oferta nesta competência* — no offer in this month | the month has no registered offer | register the offers or copy the previous month |
| Quota **Esgotada** | the month's quota has run out | Regulation uses over-quota with a justification, or adjust the quota |
| **Sem pactuar** (not agreed) on the offer | the offer has not been split into quotas | agree the quotas |
| **sem referência** on the origin | the origin has no referral for the procedure | register the referral |
