# Emergency 192

> **Version:** 1.15.0 · [Português](../pt-BR/06-urgencia-192.md)

Menu: **Assistência › Urgência 192** (Care › Emergency 192). This is the 192 centre (Brazil's emergency medical number;
emergency regulation): calls, medical regulation, sending the ambulance, the destination (including *vaga zero*, sending without
a confirmed bed), the pre-hospital care form (APH) and response times.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Regulation call taker (TARM) | takes the call and opens the incident |
| Emergency regulating physician | classifies severity, decides the conduct and the destination |
| SAMU intervention team (ambulance crew) | fills in the APH form at the scene |
| Destination unit (reception, physicians, nursing) | receives the patient |
| Manager | follows up |

> For the centre to work, a unit must have the **Regulação médica de urgência** (emergency medical regulation) capability in the
> Operating Model (Administrativo › Modelo operacional). Without it, the screen warns.

**The cards at the top:**

- **Aguardando regulação** (awaiting regulation, no severity yet);
- **Vermelho · amarelo** (red · yellow) open;
- **Recurso em campo** (resource in the field);
- the average time **Até a regulação** (until regulation).

The **Todas as centrais** (all centres) filter chooses the centre.

## 1. Opening an incident (TARM)

1. Click **Novo chamado** (new call).
2. Choose the **Central** (centre) and the **Origem** (origin):
   - **Chamado 192** (192 call);
   - **Pedido de unidade** (request from a unit): choose the **Unidade que pede** (requesting unit);
   - **Crise em saúde mental** (mental health crisis).
3. Enter the call-back phone number and describe the **Queixa** (complaint): what is happening and since when.
4. **Patient:**
   - enter the **CPF do paciente**, if known;
   - if not, tick **Sem identificação** (unidentified) and describe the person (sex and approximate age).
5. Choose the **Tipo de local** (type of place: home, public road, event venue, school, unit point or other) and enter the
   **Endereço** (address) and **Ponto de referência** (landmark).
6. Save (*Ocorrência aberta*, incident opened). It goes into **Ocorrências abertas** (open incidents) as *Aguardando regulação*.

## 2. The incident board

The **Ocorrências abertas** show first those awaiting regulation, then by severity and by call time. Each one shows:

- the severity, the protocol number and the status;
- the complaint and the place.

Those without access to clinical data see the row with *sem acesso ao dado clínico* (no access to clinical data).

**Statuses:** awaiting regulation, regulated, resource in the field, completed or cancelled.

When you open an incident, the panel shows:

- the complaint, the caller, the place and the requesting unit;
- the decision, the conduct, the destination and *vaga zero*;
- the outcome and the **Ficha do APH** (APH form);
- the times **Até o envio** (until dispatch), **Envio até o local** (dispatch to scene) and **Total**;
- the **Linha do tempo** (timeline): call received, regulation, resource sent, arrival at the scene, departure, arrival at the
  destination, received at the destination, closure.

Alongside are:

- the **Viaturas disponíveis** (available ambulances) of SAMU, on duty now and with no open incident;
- the **Destinos de urgência** (emergency destinations): units with the Emergency profile, with free beds and incidents on the
  way.

## 3. Regulating (regulating physician)

1. On the incident, click **Regular** (regulate).
2. Classify the **Gravidade** (severity): red, yellow, green or blue.
3. Choose the **Decisão** (decision) and describe the **Conduta** (conduct):

   | Decision | What it asks for |
   |----------|------------------|
   | Orientação por telefone (phone advice) | only the conduct |
   | Envio de recurso (send resource) | the fleet ambulance or the resource type (USA advanced support, USB basic support, motorcycle ambulance, air ambulance or other); the destination can come later |
   | Encaminhamento (meios próprios) (referral, own means) | the destination |
   | Transferência entre unidades (transfer between units) | the destination |

4. Under **Viatura da frota** (fleet ambulance), choose one of those available on duty. Without a registered ambulance, record the
   type and a text (for example, *USA-01*); the ambulance can be set with the crew on site.
5. Choose the **Destino** (destination; required for referral and transfer). **Without a free bed now:** to send anyway, tick
   **vaga zero** (send without a confirmed bed) and write the **Justificativa da vaga zero**.
6. Enter the **Matrícula do médico regulador** (regulating physician's staff number) and confirm: *Ocorrência regulada* or
   *Orientação registrada*.

**Reclassificar** (reclassify): choose the **Nova gravidade** (new severity) and the **Motivo** (*Gravidade reclassificada*).

**Definir destino** (set destination) or **Trocar destino** (change destination): once regulated, and until arrival at the
destination, the regulator chooses or changes the destination unit, also with *vaga zero* when there is no bed.

## 4. Care at the scene: the APH form (SAMU crew)

The **Atendimento na cena** (care at the scene) section lists incidents with a resource sent.

1. Click the incident to open the form. Enter **your staff number** at the top.
2. Fill in:
   - the **Cena** (scene): the nature (clinical, trauma, obstetric, psychiatric, paediatric…), the **Mecanismo do trauma**
     (mechanism: fall, car collision, motorcycle, pedestrian hit, firearm, stab wound, assault, burn, drowning…) and the
     situation found;
   - the **Avaliação primária (XABCDE)** (primary survey):
     - X, exsanguinating haemorrhage;
     - A, airway;
     - B, breathing;
     - C, circulation;
     - D, Glasgow eye (1–4), verbal (1–5) and motor (1–6), with the total calculated, and the pupils;
     - E, exposure;
   - the **Procedimentos** (procedures):
     - oxygen, intubation, venous access, cervical collar, board and immobilisation, splint;
     - dressing or compression, tourniquet, CPR, defibrillation, monitoring, childbirth;
     - or other, described;
   - the **Impressão diagnóstica** (diagnostic impression) and the **CID-10** (ICD-10), and the **Desfecho na cena** (outcome at
     the scene; with who refused, the time of death or who received at the destination and the hand-over time, as the case may
     be).
3. Save the form (*Ficha salva*). Once saved, record:
   - the **Sinais vitais** (vital signs: systolic and diastolic BP, HR, SpO2, temperature, glucose and pain from 0 to 10), as
     many times as needed;
   - the **Medicamentos** (medicine, dose and route).
4. At the end, click **Finalizar** (finish) (*Ficha finalizada*). **Once finished, the form only accepts addenda:** use **Novo
   adendo** (new addendum).

**PDF:** the **Ficha do APH em PDF** button generates the form for printing or attaching.

## 5. Receiving at the destination, identifying and closing

- **Receber no destino** (receive at destination; the receiving unit): records the patient's arrival (*Paciente recebido*).
- **Identificar paciente** (identify patient): when the patient arrived unidentified, enter the 11-digit CPF to link the incident
  to the record (*Paciente identificado*).
- **Encerrar** (close; centre):
  1. choose the **Desfecho** (outcome): advised by phone, treated at the scene, received at the destination, death at the scene,
     refusal, no incident (mistake or hoax) or withdrawal;
  2. describe the closure.

  Receipt at the destination is recorded by the receiving unit, and phone advice by the regulation.
- **Cancelar** (cancel; centre): with the reason (for example, *chamado duplicado* — duplicate call).

## 6. Common messages

| Message | What it means | What to do |
|---------|---------------|------------|
| *Informe o telefone de retorno.* — enter the call-back phone | the call has no phone number | note the caller's number |
| *Descreva a queixa.* — describe the complaint | the call has no complaint | describe what is happening |
| *Nenhum paciente com esse CPF; deixe em branco e descreva a pessoa.* — no patient with this CPF | the CPF is not registered | tick unidentified and describe the person |
| *Classifique a gravidade.* / *Descreva a conduta.* — classify severity / describe conduct | regulation is incomplete | choose the colour and describe the conduct |
| *Escolha a viatura ou o tipo de recurso.* — choose the ambulance or resource type | sending a resource without one | choose the ambulance or the type |
| *Escolha o destino.* — choose the destination | referral or transfer without a destination | choose the destination unit |
| *Justifique a vaga zero.* — justify vaga zero | *vaga zero* needs the why | write the justification |
| *Informe a sua matrícula no topo da ficha.* — enter your staff number at the top of the form | the APH form has no professional | enter your staff number |
| *Descreva o outro procedimento.* — describe the other procedure | the *Outro* procedure has no description | describe what was done |
| *Escolha o desfecho.* / *Descreva o encerramento.* — choose the outcome / describe the closure | the closure is incomplete | choose the outcome and describe it |
| *Informe o CPF com 11 dígitos.* — enter the CPF with 11 digits | the identification CPF is incomplete | check the digits |
