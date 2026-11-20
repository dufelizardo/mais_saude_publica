# Laboratory and imaging

> **Version:** 1.15.0 · [Português](../pt-BR/07-laboratorio-e-imagem.md)

Menu: **Assistência › Laboratório e imagem** (Care › Laboratory and imaging). It covers both test flows:

- **Laboratory:** order, collection, analysis, release and acknowledgement by the requester.
- **Imaging:** order, examination, report and acknowledgement by the requester.

Both end in a PDF report with a verification code.

**Who uses it and what each sees:** tabs appear according to the profile.

| Tab | Who uses it |
|-----|-------------|
| **Para coletar** (to collect) | nurses, nursing technicians, laboratory technicians and the technical lead |
| **Em análise** (in analysis) | laboratory technicians and the technical lead |
| **Para liberar** (to release) | the laboratory technical lead |
| **Para realizar** (to perform) | radiology technicians |
| **Para laudar** (to report) | radiologists |
| **Críticos a comunicar** (critical values to communicate) | the technical lead and radiologists |
| **Para ciência** (for acknowledgement) | those who order tests: physicians and nurses |
| **Pedidos** (orders) and **Catálogo de exames** (test catalogue) | everyone who opens the screen; the catalogue is kept by the technical lead |

**The cards at the top:** **Para coletar**, **Em análise**, **Para liberar** and **Urgentes** (urgent, at any stage).

## 1. Ordering tests

**Who uses it:** physicians and nurses. The **Pedir exames** button inside the encounter (module 03) opens an order linked to that
encounter.

1. Click **Pedido de exames** (test order).
2. Under **Paciente**, type part of the name or CPF and choose the person.
3. Choose the **Unidade que pede** (requesting unit) and enter the **Matrícula de quem pede** (requester's staff number).
4. Under **Exames** (tests), tick the tests. Use **Filtrar exames…** to find them in the catalogue.
5. If there is an imaging test, choose the **Serviço de imagem** (imaging service): the unit that performs and reports, which
   must have diagnostic imaging. A regulated test goes to the unit that Regulation authorised.
6. Write the **Indicação clínica** (clinical indication: why the tests are ordered) and, optionally, the **CID-10** (ICD-10).
7. Tick **Urgente** (urgent) when appropriate and save.

## 2. Collecting (laboratory)

The **Para coletar** tab shows one order per row, urgent first, with each test's **Preparo** (preparation).

1. Click **Coletar** (collect). If it says **Recoleta** (recollection), the previous sample was rejected: check the reason before
   collecting again.
2. Under **O que foi coletado** (what was collected), tick the tests. Whatever is left unticked keeps waiting for collection.
3. Enter **Onde foi coletado** (where it was collected) and the **Laboratório que analisa** (analysing laboratory), or **A própria
   unidade da coleta** (the collecting unit itself). That laboratory will see the sample in its analysis list.
4. Enter the **Matrícula de quem coletou** (collector's staff number) and save.

**Collection creates one sample per material**, with the label code.

## 3. Analysing and releasing (laboratory)

**Em análise** (laboratory technician): the list is per test, urgent first.

1. Click **Resultado** (result).
2. Enter the **Resultado** (number or text, depending on the test) and, optionally, an **Observação** (note).
3. Enter the **Matrícula de quem analisou** (analyst's staff number) and save. The result goes to **Para liberar**.

**Rejeitar amostra** (reject sample): a problem sample is rejected, and its tests not yet released go back to collection.

1. Choose the **Motivo** (reason): haemolysed, insufficient volume, clotted, wrong identification or other.
2. Enter the **Matrícula de quem rejeita** (staff number of the person rejecting).

**Para liberar** (technical lead): results recorded and awaiting review.

1. Click **Revisar** (review) and check the result and the analyst.
2. Enter the **Matrícula de quem libera** (releaser's staff number) and release (*Resultado liberado*). If something is wrong
   before release, use **Registrar de novo** (record again).

> **Once released, the result goes to the order and can only change through amendment.**

## 4. Performing and reporting (imaging)

**Para realizar** (radiology technician): tests ordered from your service, urgent first. Check the preparation before calling the
patient.

1. Click **Realizar** (perform).
2. Enter the **Equipamento** (equipment; for example, *Aparelho da sala 1* — room 1 machine).
3. If contrast was used, tick **Com contraste** (with contrast) and enter the **Contraste usado** (contrast used).
4. Enter the **Matrícula de quem realizou** (staff number of who performed it) and save.

**Para laudar** (radiologist): performed tests awaiting a report, urgent first.

1. Click **Laudar** (report).
2. Fill in the **Técnica** (technique), the **Achados** (findings) and the **Conclusão** (conclusion).
3. Choose the **Classificação**: no abnormalities, abnormal or critical finding.
4. If available, paste the **Link das imagens** (image link): the study's address in the image viewer (PACS).
5. Enter the **Matrícula do radiologista** and save.

> **The signed report is released at once.** Corrections are made by **adendo** (addendum), which stores a new report; the
> previous one is kept and linked to it.
>
> **Critical finding:** the requester gets a notice, and the test enters **Críticos a comunicar** until you record whom you spoke
> to.

## 5. Critical value: communicating

The **Críticos a comunicar** tab shows released results with a critical (panic) value that the physician has not yet seen.

1. Talk to whoever is responsible for the patient: the requester, the doctor on duty or the ward's nursing team.
2. Click **Comunicar** (communicate) and record:
   - **Quem recebeu a informação** (who received the information): name and role, for example *Dra. Ana, plantonista* (Dr Ana,
     on duty);
   - the **Meio** (means): phone, in person or system message;
   - **Quando** (when; empty means now);
   - the **Matrícula de quem comunicou** (communicator's staff number).

## 6. Acknowledgement by the requester

The **Para ciência** tab shows released results of the tests **you** ordered and have not yet seen, critical ones first.

1. Enter **Sua matrícula** (your staff number) and click **Ver resultados** (see results).
2. Open the order and see the value. **Retificado** (amended) warns that the value was corrected.
3. Click **Registrar ciência** (record acknowledgement) (*Ciente*).

## 7. Following the order, amending and handing over the result

The **Pedidos** tab shows the progress of each order, **without the values**: awaiting collection, in progress, completed or
cancelled. Search by patient, test or unit and click **Ver** (view). The details record that the results were read and show:

- the **Amostras** (samples, including rejected ones);
- the **Laudos de imagem** (imaging reports: technique, findings, conclusion and **Ver as imagens** — view images);
- the **Realização** (performance), the **Comunicação de críticos** and the **Entregas ao paciente** (hand-overs to the
  patient);
- the **Laudos emitidos** (issued reports) and the **Histórico**.

**Amending a released result** (technical lead):

1. In the details, click **Retificar** (amend).
2. Enter the **Valor correto** (correct value) or the **Resultado correto**, the **Motivo da retificação** (reason) and the
   **Matrícula de quem retifica**.
3. Save. The amendment stores a new result, already released by you. The previous one is kept and linked to it.

**Cancelling a test in the order:** it leaves the work lists, and the order's other tests continue.

**PDF report:**

- **Baixar laudo (PDF)** (download report) issues the signed report, with a verification code and a QR code in the footer.
- Downloading again without a new result returns the same file.
- With a new result, a new version is issued, and the previous one shows as **Substituída** (replaced).
- **Prévia para imprimir** (print preview) opens a simple page for printing.

**Registrar entrega** (record hand-over; those who collect or order):

1. Under **Quem retirou** (who collected it), choose **O próprio paciente** (the patient) or **Outra pessoa** (someone else),
   with the name, document and relationship.
2. Choose the **Forma** (form): printed and collected at the unit, or handed over at the consultation.
3. Enter the **Unidade da entrega** (hand-over unit) and the **Matrícula de quem entrega**.

## 8. Checking whether a report is authentic

Anyone, **without signing in**, can check a report on the **Verificar laudo** (verify report) page, through the QR code or the
system address.

1. Type the **Código de verificação** (verification code) from the report footer.
2. The page answers **Laudo autêntico e vigente** (authentic and current) or **Laudo autêntico, mas substituído** (authentic but
   replaced), with the date, unit, patient, tests and the **Hash do PDF (SHA-256)**.
3. To check the file, compute the SHA-256 of the PDF received: it must match the hash shown.

## 9. Test catalogue (technical lead)

1. Click **+ Exame** (add test).
2. Enter the **Nome** (name, for example *Glicemia de jejum* — fasting glucose) and the **Categoria** (category):
   - **Laboratorial** (laboratory): collection and release;
   - **Imagem** (imaging): performance and report. The **Modalidade** (modality) can be X-ray, ultrasound, CT, MRI,
     mammography, bone densitometry or other.

   The category decides the flow and **cannot change after creation**.
3. Enter the **Prazo em dias** (turnaround in days) and, for laboratory tests:
   - the **Material** and the **Tipo de resultado** (result type: numeric or text);
   - for numeric tests, the unit (for example, *mg/dL*), the **Referência mínima** and **máxima** (reference range) and the
     critical below and above values;
   - for text tests, the reference (for example, *Não reagente* — non-reactive).
4. Write the **Preparo do paciente** (patient preparation; for example, *Jejum de 8 horas* — 8-hour fast).
5. **Faixas por sexo e idade** (ranges by sex and age, optional):
   - under **+ Faixa**, enter the sex, a description (for example, *Recém-nascido* — newborn), age from and up to, and the values;
   - the range for the patient's sex covering the age on the collection date applies; otherwise the one without sex; otherwise
     the general range.
6. **Insumos** (supplies, optional): the stock materials and the quantity each test uses. Tick **Só com contraste** (only with
   contrast) when the supply is used only if the examination uses contrast.
7. Leave **Em uso** (in use) ticked. When not in use, the test does not appear in orders, and old orders continue.

> The reference range is copied into each result: **changing the catalogue does not change results already given.**

## 10. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Marque pelo menos um exame.* / *Informe a indicação clínica.* — tick at least one test / enter the indication | the order is incomplete | tick the tests and write the indication |
| *Selecione o serviço de imagem.* — select the imaging service | there is an imaging test without the performing service | choose the imaging unit |
| *Marque o que foi coletado.* — tick what was collected | the collection has no test ticked | tick what was collected |
| *Informe o valor numérico.* / *Informe o resultado.* — enter the value / result | the result is empty | record the value |
| *Informe qual contraste foi usado.* — enter which contrast was used | contrast ticked without saying which | enter the contrast |
| *Descreva os achados.* / *Informe a conclusão.* — describe the findings / enter the conclusion | the imaging report is incomplete | fill in findings and conclusion |
| *Informe quem recebeu a informação.* — enter who received the information | the critical communication has no recipient | write the name and role |
| *Informe a sua matrícula para ver os resultados dos seus pedidos.* — enter your staff number to see your orders' results | the Para ciência tab needs to know who you are | enter your staff number |
| *Informe o nome de quem retirou.* / *Informe o documento de quem retirou.* — enter the collector's name / document | hand-over to someone else is incomplete | fill in name and document |
| *Não foi possível emitir o laudo em PDF.* — the PDF report could not be issued | the report was not issued | try again; the order must have a released result |
| *Informe ao menos um limite de referência.* — enter at least one reference limit | the numeric test has no reference | enter the minimum, the maximum or both |
| **Recoleta** on the order | the previous sample was rejected | check the reason and collect again |
