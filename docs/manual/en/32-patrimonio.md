# Assets

> **Version:** 1.15.0 · [Português](../pt-BR/32-patrimonio.md)

Menu: **Administrativo › Patrimônio** (Assets). It looks after the network's permanent assets:

- asset tagging (*tombamento*) and the person responsible;
- transfers between units;
- maintenance (plans, requests and work orders);
- inventory and write-off.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Asset office and managers | tag, transfer, open inventories and request write-offs |
| Maintenance and managers | carry out work orders |
| Manager | confirms write-offs above R$ 10,000.00 |
| Everyone responsible for an asset | sees their assets through the **Meus bens** (my assets) menu item and opens requests |
| Committee members | check the inventory through the **Meus inventários** (my inventories) menu item |

The screen has the tabs:

- assets, **A tombar** (to tag) and transfers;
- **Manutenção** (maintenance), **Inventários** (inventories) and **Painel** (dashboard);
- **Meus bens** (my assets).

## 1. Tagging and registering assets

**A tombar:** permanent assets accepted on Procurement's final receipt, waiting for the tag number and the person responsible.

1. Click **Tombar** (tag) on the asset.
2. Enter the tag number and the **Matrícula do responsável** (staff number of the person responsible).

**Cadastrar bem** (register asset): for an asset that did not come from a purchase in the system (donation, loan, transfer from
another government body or older holdings). It is registered already tagged.

1. Click **Cadastrar bem**.
2. Enter:
   - the **Descrição** (description) and the **Classe** (class): medical-hospital equipment, IT, furniture, vehicle, appliance or
     other;
   - the **Origem** (origin): purchase, donation, loan, transfer from another government body;
   - the **Marca** (brand), **Modelo** (model) and **Número de série** (serial number);
   - the **Data de aquisição** (acquisition date), **Valor (R$)** (value) and **Documento** (document: invoice, donation deed…);
   - the **Matrícula do responsável** and an optional note.
3. Click **Cadastrar**.

**The asset:** its record shows:

- identification, the person responsible and the **Valor contábil** (book value), estimated by the class's straight-line
  depreciation;
- maintenance, **Responsáveis** (people responsible) and write-off;
- for a vehicle, the linked **Veículo da frota** (fleet vehicle).

## 2. Person responsible and transfer

- **Trocar responsável** (change person responsible): the new person's staff number. The person responsible signs the deed, and the
  asset appears in their **Meus bens**.
- **Transferring to another unit:**
  1. use **Enviar para** (send to), with the unit and the reason;
  2. the destination uses **Aceitar** (accept; with the **Setor no destino** — destination sector — and the **Matrícula do novo
     responsável**) or refuses, with the reason;
  3. the source can **Cancelar envio** (cancel sending) while acceptance is pending.

> While an inventory is open at the unit, its assets cannot be transferred.

## 3. Maintenance

**Opening a request (anyone responsible):** on the asset, use **Abrir chamado: defeito** (open request: fault). Describe the
**Defeito** (fault) and tick **Bem parado** (asset out of service) if it does not work. **Meus chamados** (my requests) shows those
you opened.

**Maintenance plans (preventive and calibration):**

1. On the asset, use **Novo plano** (new plan): the type (preventive, calibration or electrical safety) and **A cada (dias)** (every
   so many days). Corrective maintenance has no plan, and each asset has one active plan per type.
2. Optionally, the **Última execução** (last execution).
3. Click **Criar plano** (create plan). When the plan falls due, the work order appears by itself, one per due date. **Desativar**
   (deactivate) stops the plan, and orders already open continue.

**Work orders (maintenance):**

1. **Triagem e início** (triage and start):
   - the priority (low, normal, high or urgent) and **Salvar prioridade** (save priority);
   - who carries it out: the **Equipe interna** (in-house team) or the **Contrato de serviço** (service contract, with the
     contract and the technician);
   - **Iniciar** (start).
2. **Conclusão** (completion):
   - the **Serviço realizado** (work done) and **Mão de obra (R$)** (labour cost);
   - for calibration, the **Certificado** (certificate) and **Válida até** (valid until);
   - parts: the **Material do Estoque** (stock material, which is withdrawn) or the part brought by the contractor, with
     description, quantity and cost;
   - **Concluir** (complete).
3. **Cancelar ordem** (cancel order): with the reason.

**Notices:**

- **Planos vencendo em 30 dias** (plans due within 30 days);
- **calibrações vencidas** (overdue calibrations);
- orders open, in progress and with the asset out of service.

## 4. Inventory

**Opening (asset office):**

1. Use **Abrir inventário** (open inventory), choose the **Unidade** and the **Comissão** (committee).
2. The unit's tagged assets enter the list. While the inventory is open, they cannot be transferred.

**Checking (committee, through Meus inventários):**

1. On each asset, click **Conferir** (check) and choose the **Resultado** (result): found, not found or elsewhere.
2. Choose the **Conservação** (condition: good, fair, poor or unserviceable) and, if it was elsewhere, the **Unidade onde estava**
   (unit where it was) and **Setor onde estava** (sector where it was).
3. Click **Registrar** (record).
4. **Achados a mais** (extra findings): the **Bem achado fora da lista** (asset found outside the list), with the place, through
   **Registrar achado** (record finding).

**Fechar inventário** (close inventory): closing proposes the follow-up actions:

- sector corrected;
- transfer opened, for an asset at another unit;
- write-off suggested, for assets not found or unserviceable.

## 5. Write-off

1. On the asset, use **Pedir baixa** (request write-off) and choose the reason:
   - unserviceable, lost or theft/robbery;
   - donation to a third party, sale or return of a loan.
2. Enter the **Processo** (process), the **Data** (date) and, for theft or robbery, the **Boletim de ocorrência** (police report).
3. **Above R$ 10,000.00, the write-off waits for management's confirmation.**
   - Under **Baixas à espera da confirmação** (write-offs awaiting confirmation), the manager uses **Confirmar** (confirm) or
     **Recusar** (refuse), with the reason.

## 6. Dashboard

The dashboard shows:

- tagged assets, acquisition value and estimated book value;
- idle assets, those to tag, open inventories and write-offs this year;
- **Por unidade** (by unit): assets, acquisition, book value and last inventory (with a **há mais de 12 meses** — more than 12
  months ago — warning);
- **Por classe** (by class);
- **Manutenção** (maintenance): the assets that cost the most.

## 7. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| Transfer refused | there is an open inventory at the unit | close the inventory first |
| **calibração vencida** on the asset | the certificate is past its validity | open the calibration order |
| Write-off **À espera da confirmação** (awaiting confirmation) | the value exceeds R$ 10,000.00 | wait for management's confirmation |
| *Nenhum bem sob a sua responsabilidade.* — no asset under your responsibility | you are not responsible for any asset | nothing to do |
| Last inventory **há mais de 12 meses** | the unit has no recent inventory | open an inventory |
