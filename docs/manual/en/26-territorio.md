# Territory

> **Version:** 1.15.0 · [Português](../pt-BR/26-territorio.md)

Menu: **Administrativo › Território** (Territory). It is primary care's geographic division:

- **segments**, **territories**, **areas** and **micro-areas**;
- the team responsible for the area and the community health worker (ACS) responsible for the micro-area, with history;
- the register of **properties**, **households** and **families**.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Anyone signed in | views the territory division |
| Managers and nursing coordination | register segments, territories, areas and micro-areas, and assign the team and ACS |
| Nurses, nursing coordination and ACS | register properties, households and families |

**The cards at the top:**

- **Áreas** (areas) and **Microáreas** (micro-areas);
- **Microáreas sem ACS** (micro-areas without a community health worker);
- **Sem desenho** (not drawn): areas and micro-areas without a polygon on the map.

## 1. Building the division (management)

The order is **segment → territory → area → micro-area**.

1. **Novo segmento** (new segment): a health district, for example. Enter the **Código** (code) and the **Nome** (name).
2. **Novo território** (new territory): choose the segment and enter the code and name.
3. **+ Área** (add area):
   - choose the territory and the **Unidade** (unit), with the CNES;
   - choose the **Equipe responsável** (responsible team). Without a team, micro-areas cannot get an ACS.
4. **Nova microárea** (new micro-area): in the area, enter the code and choose the **ACS**.

**Drawing on the map:**

- draw the polygon of the area and of each micro-area;
- view **Esta área** (this area) or the **Território inteiro** (whole territory);
- without a polygon, saving deletes the drawing.

**Changing the team or ACS:** choose the new team or ACS and write the **Motivo da troca** (reason). The **Histórico** (history)
keeps who was responsible before.

Use **Buscar no território** (search the territory) to find an area, micro-area, team or ACS.

## 2. Properties and families

Household and family registration belongs to those who register or manage the area's territory.

**The property:**

1. In the area, open **Imóveis e famílias** (properties and families) and click **Novo imóvel** (new property).
2. Choose the **Tipo** (type) and the **Microárea**, or use **Pelo ponto no mapa** (by the point on the map). With the point
   marked, it must fall inside the drawn micro-area.
3. Enter the address: **Logradouro** (street), **Número** (number; *S/N* if none), **Bairro** (district), **CEP** (postcode),
   **Cidade** (city) and **UF** (state).
4. Optionally, click on the map where the property is.
5. Leave **Ativo** (active) ticked. An inactive property (demolished, permanently vacant) stays in the history.

**The household (inside the property):**

1. Use **Novo domicílio** (new household) and enter the **Identificação no imóvel** (identification within the property, for
   example *apto 12, casa dos fundos* — flat 12, back house).
2. Optionally, enter:
   - the status and location;
   - the number of rooms;
   - water supply, sewage and waste disposal;
   - electricity.
3. An inactive household cannot receive a family.

**The family:**

1. Use **Nova família** (new family): the head of household (a registered patient), the **Renda familiar** (family income) and
   **Desde** (since).
2. The other people are added later through **Incluir pessoa** (add person), with the relationship. Search by name, CPF or SUS
   card. If not found, register the patient (module 01) and come back.
3. If the person has a different address in the patient record, use **Usar o endereço do imóvel** (use the property address).

**Changes:**

- **Saída** (exit): the person leaves the family, with the **Motivo** (reason, for example *saiu de casa, óbito, mudou de cidade* —
  left home, death, moved city) and the date. If they are the last person, the family no longer lives there.
- **Trocar responsável** (change head of household): choose the new head.
- **Mudança** (move):
  - the whole family moves to another household (**Área de destino** — destination area, **Imóvel** and **Domicílio**);
  - it stops living here on the date and starts at the destination with the same people;
  - for a single person, use the exit and add them to the other family.

> Living in a household in an area with a team gives the patient a **reference team** through the household (module 01).

## 3. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Escolha a microárea ou marque o ponto no mapa.* — choose the micro-area or mark the point | the property has no micro-area | choose the micro-area or mark the point |
| *Informe a latitude e a longitude juntas, ou nenhuma.* — enter both latitude and longitude, or neither | only one coordinate was entered | enter both or neither |
| *Escolha o segmento.* / *Escolha o território.* — choose the segment / territory | the record has no parent level | choose the segment or territory |
| *Escolha a equipe.* / *Escolha o ACS.* — choose the team / ACS | the area or micro-area has no one responsible | choose the team or ACS |
| *Informe o motivo da troca.* — enter the reason for the change | the change has no reason | write the reason |
| *Escolha o domicílio de destino.* — choose the destination household | the move has no destination | choose area, property and household |
| **Área sem equipe responsável.** (area without a responsible team) | micro-areas cannot get an ACS | choose the area's team |
