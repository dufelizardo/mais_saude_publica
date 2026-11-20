# Health inspection

> **Version:** 1.15.0 · [Português](../pt-BR/14-vigilancia-sanitaria.md)

Menu: **Vigilância › Vigilância sanitária** (Surveillance › Health inspection, VISA). It covers:

- regulated establishments and licensing;
- inspections and complaints;
- the administrative health process: infraction notice, defence, decision and appeal;
- precautionary measures: seizure and closure.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Health inspector | registers establishments, inspects, records complaints and issues infraction notices |
| Health inspection coordination | everything the inspector does, plus licences, precautionary measures and judgement (decision and appeal) |
| Manager | follows up and supports the coordination |

**The cards at the top:**

- **Licenças pendentes** (pending licences): without a licence, expired or expiring;
- **Prazos de correção vencidos** (correction deadlines passed): open irregularities;
- **Denúncias paradas** (stalled complaints) for more than 30 days;
- **Processos com pendência** (processes with pending steps): acknowledgement, default, decision or closure.

The screen has the tabs:

- **Estabelecimentos** (establishments), **Inspeções** (inspections), **Denúncias** (complaints) and **Licenças** (licences);
- **Processos** (processes) and **Medidas** (measures);
- **Pendências** (pending items).

## 1. Registering an establishment

1. Click **Novo estabelecimento** (new establishment).
2. Enter:
   - the **CNPJ ou CPF** (company or personal taxpayer number), the **Razão social ou nome** (legal name), the **Nome fantasia**
     (trade name), the **Endereço** (address) and the phone;
   - the **Responsável legal** (legal representative) and the **Responsável técnico** (technical lead), with the **Registro no
     conselho** (professional council registration).
3. Under **Atividades (CNAE) e grau de risco** (activities and risk level), use **Adicionar atividade** (add activity) for each
   one: the **CNAE** activity code (*0000-0/00*), the description and the risk level.

   | Level | Licensing |
   |-------|-----------|
   | I · low | licensing waiver, with no expiry (ANVISA RDC 153/2017; Law 13.874/2019) |
   | II · medium | health licence |
   | III · high | licence requiring a satisfactory inspection in the last 12 months |

4. Leave **Ativo** (active) ticked and save.

The open establishment shows:

- irregularities, inspections, licences and processes;
- **Documentos** (documents): type, title and a PDF, JPG or PNG file up to 10 MB;
- the actions **Alterar cadastro** (edit record), **Inspecionar** (inspect), **Lavrar auto de infração** (issue infraction notice)
  and **Abrir não conformidade** (open non-conformity).

## 2. Licensing

1. On the establishment row, use **Licença** (licence; coordination).
2. Depending on the risk:
   - **Dispensa de licenciamento** (licensing waiver; risk I);
   - **Conceder licença sanitária** (grant health licence), with the validity. Empty means one year. For risk III, the licence
     requires a satisfactory inspection in the last 12 months.
3. Save. The licence gets a number.

The **Licenças** tab lists the number, issue date and validity. In **Pendências**, **Licenciamento** (licensing) shows what is
unlicensed, expired or expiring.

## 3. Inspecting

1. Click **Registrar inspeção** (record inspection), or **Inspecionar** on the establishment or the complaint.
2. Choose the **Estabelecimento**, the reason and, if any, the **Denúncia** (complaint) or the **Inspeção de origem** (source
   inspection: the follow-up to check corrections).
3. Optionally, choose a **Roteiro** (checklist) and answer the **Itens do roteiro** (checklist items).
4. Record the **Irregularidades** (irregularities) with **Adicionar irregularidade**: severity, legal framing and the **Corrigir
   até** (correct by) deadline. On a follow-up, tick the **Irregularidades corrigidas** (corrected irregularities).
5. Enter the **Matrículas dos fiscais** (inspectors' staff numbers), separated by commas, including yours.
6. Write the **Relatório** (report) and save.

The inspection record comes out as a PDF (**Termo**), and the row shows **assinada** (signed).

## 4. Complaints

1. Click **Registrar denúncia** (record complaint) and choose the **Canal** (channel).
2. Choose the **Estabelecimento cadastrado** (registered establishment) or, if it is not registered, describe the
   **Estabelecimento denunciado** (establishment reported: name and address).
3. Write the complaint. Tick **Anônima** (anonymous), or enter the **Denunciante** (complainant) and the **Contato** (contact).

   > The complainant's details only appear to the health inspection coordination.

4. Save. The complaint gets a **Protocolo** (protocol number).

Then use **Inspecionar** and, at the end, **Concluir** (conclude): **Procedente** (upheld) or **Improcedente** (not upheld), with
the justification.

## 5. Administrative health process

**Issuing an infraction notice:**

1. Use **Lavrar auto de infração** on the establishment or on the **Processos** tab.
2. Tick the **Irregularidades abertas a autuar** (open irregularities to cite).
3. Enter the **Dispositivo legal infringido** (legal provision breached, for example *Lei 6.437/1977, art. 10, IV*) and the
   **Descrição da infração** (description).
4. Choose the severity. Empty means that of the most serious irregularity.
5. Enter the **Matrículas dos fiscais** and save.

**The course of the process:**

1. **Ciência do auto** (acknowledgement of the notice): form and date.
2. **Defesa** (defence): filing date and summary. With no defence on time, the process proceeds by **revelia** (default).
3. **Decisão** (decision; coordination):
   - the **Resultado**: **Aplicar penalidades** (apply penalties) or **Arquivar** (dismiss);
   - the **Fundamentação** (grounds), with mitigating (art. 7) and aggravating (art. 8) circumstances;
   - the **Penalidades (art. 2º)** (penalties), with the amount (R$) or the suspension period.
4. **Ciência da decisão** (acknowledgement of the decision).
5. **Recurso** (appeal: filing date and summary) and the **Julgamento do recurso** (appeal judgement): **Improvido** (dismissed),
   **Parcialmente provido** (partly upheld) or **Provido** (upheld), with the penalties that remain.
6. **Encerrar processo** (close process).

Each step's **Termos** (records) come out as PDFs (**Baixar** — download).

> *A próxima etapa não está no seu acesso* (the next step is not in your access): decision and judgement belong to the
> coordination.

## 6. Precautionary measures

On the **Medidas** tab (filter **Vigentes** — current, or **Encerradas** — closed):

- **Apreender produto** (seize product):
  1. enter the **Produto**, batch, **Quantidade** and **Local de guarda** (storage place), and the reason for seizure;
  2. later, record the **Destinação do produto** (product disposal).
- **Interdição cautelar** (precautionary closure):
  1. choose the establishment and describe what is closed and why;
  2. tick **Interdição total** (full closure) if applicable, and enter the **Início** (start) and **Até** (until; empty means 90
     days);
  3. **Desinterditar** (lift closure), with the reason, ends the closure.

In **Pendências**, **Interdições cautelares no fim** (closures ending) warns about those expiring soon.

## 7. Checking a health inspection document

Anyone, **without signing in**, can check on the **Verificar documento da vigilância sanitária** (verify health inspection
document) page:

- a **record** (infraction notice, decision, closure, seizure or lifting), by the footer code. The page shows the type, date,
  establishment and the **Hash do PDF (SHA-256)**: the SHA-256 of the PDF received must match;
- a **health licence**, by its number. The page shows the issue date and validity.

## 8. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Informe o CNPJ ou CPF, a razão social e o endereço.* — enter taxpayer number, legal name and address | the record is incomplete | fill in the three fields |
| *Informe o CNAE e a descrição de cada atividade.* — enter the code and description of each activity | an activity has no code or description | complete the activities |
| *Informe a matrícula de ao menos um fiscal.* — enter at least one inspector's staff number | the inspection or notice has no inspector | enter the staff numbers |
| *Escreva o relatório da inspeção.* — write the inspection report | the inspection has no report | write the report |
| *Marque ao menos uma irregularidade aberta.* — tick at least one open irregularity | the notice has no irregularity | tick the irregularities to cite |
| *Informe o dispositivo legal e a descrição.* — enter the legal provision and description | the notice is incomplete | enter the law and describe the infraction |
| *Informe a fundamentação.* / *Informe ao menos uma penalidade.* — enter the grounds / at least one penalty | the decision is incomplete | write the grounds and penalties |
| *Informe o produto e a quantidade.* — enter product and quantity | the seizure is incomplete | fill in product and quantity |
| *A próxima etapa não está no seu acesso.* — the next step is not in your access | the step belongs to the coordination | ask the coordination |
