# Zoonoses and vectors

> **Version:** 1.15.0 · [Português](../pt-BR/13-zoonoses-e-vetores.md)

Menu: **Vigilância › Zoonoses e vetores** (Surveillance › Zoonoses and vectors). This is environmental surveillance field work.
The screen has seven tabs:

- **Focos** (breeding sites) and **Índices do ciclo** (cycle indices): property inspections against *Aedes*, breeding sites and
  indices;
- **Animais** (animals) and **Observação de agressores** (observation of biting animals);
- **Vacinação antirrábica** (rabies vaccination);
- **Qualidade da água** (water quality);
- **Riscos ambientais** (environmental risks).

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Endemic disease agent | inspects properties, records breeding sites and samples |
| Environmental surveillance and managers | plan control actions, vaccination posts, water points and risks |
| Investigators and indicator readers | read the indices |

The **Área** filter chooses the territory area (or **Todas** — all).

## 1. Recording an inspection

1. Click **Registrar vistoria** (record inspection).
2. Choose the **Área** and the **Imóvel** (property; use **Buscar endereço** — search address), and enter the **Matrícula do
   agente** (agent's staff number).
3. Choose the **Tipo de visita** (visit type): routine, index survey (LIRAa), blockade, strategic point or complaint. If the visit
   is part of a planned action, choose the **Ação de controle** (control action).
4. Choose the **Resultado** (result): inspected, closed or refused.
5. For an inspected property, enter:
   - the **Depósitos inspecionados por tipo** (containers inspected by type), such as small movable container, fixed container or
     natural container;
   - the **Depósitos eliminados** (containers eliminated) and **Depósitos tratados** (containers treated), with the
     **Larvicida** (larvicide) and the **Quantidade (g)** (quantity in grams).
6. Under **Focos (depósitos com larva ou pupa)** (breeding sites: containers with larvae or pupae), use **Adicionar foco** (add
   site): the container, stage and species (*Aedes aegypti*, *Aedes albopictus*, other or unidentified).
7. Save.

**Pendências de revisita do ciclo** (cycle revisits pending): closed or refused properties appear for **Revisitar** (revisit).

## 2. Breeding sites

The **Focos** tab lists breeding sites with the container, species, laboratory and status.

- **Resultado do laboratório** (laboratory result): *Aedes aegypti*, *Aedes albopictus*, another species or negative. **A
  negative result stops counting in the indices.**
- **Foco tratado** (site treated) and **Foco eliminado** (site eliminated) record what was done.

## 3. Cycle indices and control actions

The **Índices do ciclo** tab shows, by area and for the municipality:

- properties visited, inspected, pending and positive;
- the **IIP** (property infestation index): properties with breeding sites ÷ properties inspected × 100;
- the **Breteau index:** positive containers ÷ properties inspected × 100.

**The Ministry of Health classification:**

| Index | Status |
|-------|--------|
| below 1% | satisfactory |
| 1% to 3.9% | alert |
| 4% or more | risk |

**Control actions (environmental surveillance):**

- **Planejar bloqueio** (plan blockade): transmission blockade around a case;
- **Planejar ação** (plan action): ultra-low-volume spraying (UBV), clean-up drive, educational action or other.

Enter the **Data prevista** (planned date), the **Microáreas** (micro-areas) and the **Responsável** (person responsible). Then
**Registrar a realização** (record completion) with the properties worked, or **Cancelar a ação** (cancel the action) with the
reason.

## 4. Animals and observation of biting animals

**Animals:**

1. Use **Registrar animal** (register animal): species, owner and place.
2. Use **Registrar coleta** (record sample) for the animal's test. The result is entered later, under **Exames** (tests).

**Observação de agressores:** a dog or cat that bit someone stays **under observation for 10 days** from the bite.

1. Use **Abrir observação** (open observation), or **Observar** on the animal, linked to the bite.
2. At the end, record the **Desfecho** (outcome). If the animal fell ill or died, use **Coleta para raiva** (rabies sample).

The outcome appears in the reported rabies post-exposure care, so the investigator can decide the person's treatment schedule.
An observation without an outcome by the deadline is marked **vencida** (overdue).

## 5. Rabies vaccination

1. Choose the **Ano** (year).
2. Use **Registrar posto** (record post): the post, date and doses given to **Cães** (dogs) and **Gatos** (cats).

## 6. Water quality

**Sampling points:** **Novo ponto** (new point), with the name, place, type of **Abastecimento** (supply) and, optionally,
latitude and longitude.

**Samples:**

1. Use **Registrar amostra** (record sample): the collection date and the **Parâmetros** (parameters).
2. The result comes out as **Conforme** (compliant) or **Fora do padrão** (non-compliant). For a non-compliant sample, record the
   **Medida** (measure).

Compliance follows Brazilian Ministry of Health Ordinance GM/MS 888/2021:

- *E. coli* absent;
- in treated water, total coliforms absent and free residual chlorine from 0.2 to 5 mg/L;
- turbidity up to 5 NTU;
- fluoride up to 1.5 mg/L.

## 7. Environmental risks

The territory's **permanent risks**: risk type, place and people exposed. Exposure from a disaster stays in the emergency (module
15).

1. Use **Registrar risco** (record risk).
2. When the risk is resolved, mark it **Controlado** (controlled).

## 8. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Escolha o imóvel e informe a matrícula do agente.* — choose the property and enter the agent's staff number | the inspection is incomplete | choose the property and enter the number |
| *Escolha o resultado.* — choose the result | the inspection has no result | choose the visit result |
| *Informe a data, as microáreas e o responsável.* — enter date, micro-areas and person responsible | the control action is incomplete | fill in the three fields |
| *Informe o motivo.* — enter the reason | the cancellation has no reason | write the reason |
| *Confira os campos.* — check the fields | the drawer has fields with errors | correct what is marked in red |
| *Seu acesso não inclui esta operação.* — your access does not include this operation | your profile does not allow the action | ask for the profile, if your role needs it |
