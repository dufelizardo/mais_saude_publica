# Procurement

> **Version:** 1.15.0 · [Português](../pt-BR/31-compras.md)

Menu: **Administrativo › Compras** (Procurement). It covers the public procurement path (Brazilian Law 14.133/2021):

1. the **purchase request** and the **price survey**;
2. the **contracting** (electronic auction, direct award, joining a price register…) and the **contracts and price registers**;
3. the **supply order**, the **receipt** and **incidents** with the supplier;
4. the **supplier** register, with certificates and sanctions.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Requesters (managers, storekeepers, pharmacists) | create purchase requests |
| Procurement (procurement office, managers, finance) | analyse, survey prices, contract, manage contracts and orders |
| Contract manager and supervisor | receive deliveries through the **Meus contratos** (my contracts) menu item |

The screen has the tabs:

- **Solicitações** (requests) and **Fornecedores** (suppliers);
- **Contratações** (contracting) and **Contratos** (contracts);
- **Painel** (dashboard).

## 1. Purchase request

1. Click **Nova solicitação** (new request).
2. Choose the **Unidade**, the **Setor** (sector, optional) and the **Prioridade** (normal or urgent).
3. Write the **Justificativa da necessidade** (justification of need).
4. Add items with **Adicionar item** (add item): the **Tipo** (material or medicine), the item, the **Unidade de medida** (unit of
   measure) and the **Quantidade**. **Sugerir pelo estoque** (suggest from stock) adds materials below their minimum.
5. **Salvar rascunho** (save draft) and, when ready, **Enviar para análise** (send for analysis).

**The cycle:** draft → sent → approved (or returned, or rejected) → being quoted → quoted.

**Analysing (Procurement):** **Aprovar** (approve), **Devolver** (return) or **Rejeitar** (reject), with the reason for returning
or rejecting.

## 2. Price survey

On the approved request:

1. Use **Nova cotação** (new quote) for each item:
   - the **Fonte** (source): supplier proposal, similar public contract, website or other;
   - the **Referência** (reference: the contract, website or source);
   - the **Preço unitário (R$)** (unit price) and the **Data** (date).
2. Click **Registrar cotação** (record quote).
3. With fewer than 3 quotes for an item, write the **Justificativa para menos de 3 cotações** (justification for fewer than 3 quotes).
4. **Concluir pesquisa de preços** (complete price survey). The request becomes **Cotada** (quoted) and can join a contracting.

## 3. Contracting

1. On the **Contratações** tab, use **Nova contratação** (new contracting), or **Abrir contratação** (open contracting) from the
   **Solicitações cotadas** (quoted requests).
2. Enter:
   - the **Modalidade** (procedure): electronic auction, competitive tender, direct award, non-competitive award or joining a price
     register;
   - the **Número do processo** (process number), the **Fundamento legal** (legal basis, for example *Art. 75, II*) and the
     **Objeto** (subject);
   - when joining, the **Ata de origem** (source price register: managing body and number).
3. Link the quoted requests.
4. **Resultado** (result): for each item, the supplier and the **Preço unitário**. Above the survey's reference, write the
   **Justificativa do preço acima da referência** (justification of a price above the reference). Click **Registrar resultado**.
5. **Homologação** (award approval):
   - choose the **Instrumento** (instrument: price register or contract) and tick **Serviço contínuo** (continuous service) if it is;
   - enter the **Matrícula do gestor** and **Matrícula do fiscal** (staff numbers of the contract manager and supervisor);
   - click **Homologar** (approve the award).

Approval generates the **Atas e contratos** (price registers and contracts). **Cancelar contratação** (cancel contracting) is also
possible, with the reason.

## 4. Contracts and price registers

The **Contratos** tab shows validity, current value and **Alertas** (alerts): expiry, low balance and incidents.

On the contract:

- the **Gestor e fiscal** (manager and supervisor);
- each item's **Saldo** (balance) and **Preço** (price), with the **baixo** (low) mark below 20%;
- **Aditivos** (amendments):
  - use **Novo aditivo** (new amendment): deadline extension, quantity increase or decrease, or price rebalancing;
  - enter the date it was **Assinado em** (signed on), the **Novo fim** (new end) or the **Novo preço** (new price);
  - click **Registrar aditivo**.

## 5. Supply order and receipt

**Issuing the order (Procurement):**

1. On the contract, use **Nova ordem de fornecimento** (new supply order).
2. Choose the **Unidade de entrega** (delivery unit) and the items with quantities.
3. Choose the Finance **Empenho** (budget commitment), or type the **Número do empenho**.
4. Enter the **Prazo (dias)** (deadline in days) and click **Emitir ordem** (issue order).

**Receiving (contract manager or supervisor, through Meus contratos):**

1. Use **Receber entrega (provisório)** (receive delivery, provisional):
   - the **Nota fiscal** (invoice) and **Data da nota** (invoice date);
   - per item, what arrived and, if any, the **Lote** (batch) and **Validade** (expiry).
2. Click **Registrar recebimento provisório** (record provisional receipt).
3. After checking, use **Confirmar recebimento definitivo** (confirm final receipt), or refuse with the **Motivo da recusa**.

The order shows, per item, the **Pedido** (ordered), **Aceito** (accepted), **Em recebimento** (being received) and **Falta**
(missing).

> Final receipt records the entry in Stock (materials) or Pharmacy (medicines), at the contract price, and enters Finance's
> liquidation queue.

**Ocorrências** (incidents): use **Nova ocorrência** (new incident) for a delay, partial delivery, item out of specification or
other, with the description. **Cancelar ordem** (cancel order), with the reason.

## 6. Suppliers

1. Click **Cadastrar fornecedor** (register supplier): the **CPF ou CNPJ** (personal or company taxpayer number), **Razão social ou
   nome** (legal name), **Nome fantasia** (trade name), **E-mail**, **Telefone** (phone) and **Endereço** (address).
2. **Certidões** (certificates): use **Nova certidão** (new certificate), with the type, the date it was **Emitida em** (issued) and
   **Válida até** (valid until). The **Certidões** column shows **Em dia** (up to date) or what has expired.
3. **Sanções** (sanctions, art. 156 of Law 14.133/2021):
   - use **Nova sanção** (new sanction): the type, **Processo** (process), **Início** (start), **Fim** (end) and **Valor (R$)**
     (amount);
   - a **Vigente** (current) sanction can be **Revogada** (revoked).

## 7. Dashboard

The dashboard cards show:

- **Valor contratado** (contracted value) and **Ordens em aberto** (open orders);
- **Contratos vencendo** (contracts expiring) within 90 days;
- **Itens com saldo baixo** (items with low balance), below 20%;
- **Ocorrências abertas** (open incidents) with suppliers;
- **Solicitações em andamento** (requests in progress);
- **Ordens atrasadas** (late orders).

## 8. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| Item with fewer than 3 quotes | the law requires a justification | write the justification or record more quotes |
| Price above the reference | the result exceeded the survey | write the price justification |
| Item with **baixo** balance | the contract is running out for the item | arrange an amendment or a new contracting |
| *Nenhuma solicitação cotada fora de contratação.* — no quoted request outside a contracting | no request is ready to contract | complete the price survey |
