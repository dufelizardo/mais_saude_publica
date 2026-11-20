# Fleet and transport

> **Version:** 1.15.0 · [Português](../pt-BR/34-frota-e-transporte.md)

Menu: **Administrativo › Frota e transporte** (Fleet and transport). It covers:

- elective health transport and Out-of-Area Treatment (TFD);
- trips and the fleet;
- ambulance duty rosters.

SAMU and trips put the vehicle in service and return it.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Reception, nursing coordination and social work | request transport |
| Fleet coordinator and managers | schedule trips, look after the fleet and duty rosters |
| TFD committee and managers | authorise TFD and grant per diems |
| 192 centre, bed regulation and assets | view |

**The cards at the top:**

- vehicles **Disponíveis** (available) and **Em serviço** (in service);
- **Na fila** (in the queue): transports to schedule;
- **TFD na comissão** (TFD with the committee), awaiting authorisation.

## 1. Requesting transport

The request can also be opened from the Schedule (module 02) and from the admission (module 08), already with the patient.

1. Click **Pedir transporte** (request transport).
2. Under **Paciente**, search by name or CPF.
3. Choose the **Unidade que pede** (requesting unit) and the reason:
   - consultation, test, haemodialysis or serial therapy;
   - hospital discharge, TFD or other.
4. Enter the **Compromisso** (appointment): the time of the consultation, test or session; for a discharge, the time to leave.
   - Linked to a booking, the date, time and destination come from it.
   - For discharge transport, pick-up is the hospital and the destination the patient's home, unless another is given.
5. Enter the **Trecho** (leg):
   - the **Unidade de destino** (destination unit) or **Outro destino** (other destination, for example *Hospital de Clínicas*)
     and the **Município de destino** (destination municipality);
   - the **Endereço de embarque** (pick-up address; empty means the one in the record).
6. Tick **O que o paciente precisa** (what the patient needs): stretcher, wheelchair, oxygen or **Acompanhante** (companion). A
   companion is required for under-18s and stretcher patients; enter their document.
7. **TFD:** TFD goes to another municipality and passes through the TFD committee before joining a trip (Ministry of Health
   Ordinance SAS 55/1999). Enter the **CID** (ICD code), **Quem assina o laudo** (who signs the report) and the **Justificativa**.
8. **Sessions** (haemodialysis, therapy): use **Repetir nos dias da semana** (repeat on weekdays), with the days and **Até**
   (until). Up to 90 days, one request per session.
9. Send.

## 2. Requests and TFD

The **Solicitações de transporte** (transport requests) tab shows from 7 days ago to the next 60 days, with filters by status and
reason.

- **Autorizar** (authorise) or **Negar** (deny) TFD (TFD committee).
- **Diárias** (per diems; authorised TFD): **Conceder diária** (grant per diem):
  - the **CPF do beneficiário** (beneficiary's CPF: the patient or companion), the **Dias** (days) and the **Valor da diária (R$)**
    (daily rate);
  - one per diem per beneficiary;
  - the amount is the days (1 to 30) times the daily rate.
- **Cancelar** (cancel), with the reason. For sessions, tick **Cancelar também as próximas sessões** (also cancel the next
  sessions), if appropriate.

## 3. Scheduling trips

1. Click **Programar viagem** (schedule trip).
2. Choose the **Veículo** (vehicle): transport ambulance, van, minibus or car. **SAMU ambulances do not do elective transport.**
3. Choose the **Condutor** (driver) and enter the **Saída prevista** (planned departure) and **Retorno previsto** (planned return).
4. Add requests from the queue (**Nenhuma solicitação na fila** — no requests in the queue — when there are none).
5. Save.

**During the trip:**

- **Saída com** (departure with) the **Quilometragem do painel** (odometer reading);
- each patient's **Embarque** (boarding) and **Chegada** (arrival), or the no-show (with an optional reason);
- the final arrival, with the odometer reading.

**Agenda de viagens** (trip schedule) shows seven days from the date. **Viagens concluídas** (completed trips) shows those that
arrived in the period.

## 4. Vehicles

1. Click **Novo veículo** (new vehicle) and enter:
   - the **Placa** (plate, for example *ABC1D23*), the **Prefixo** (call sign, for example *USB-01*) and the type: advanced life
     support (USA), basic life support (USB), motorcycle ambulance, air ambulance, transport ambulance, health van, minibus or
     administrative car;
   - the **Unidade de lotação** (base unit). A SAMU ambulance is based at a unit with the Mobile care profile;
   - model, year, seats, **Maca** (stretcher) and **Acesso para cadeira de rodas** (wheelchair access).
2. **Mudar situação** (change status): available, under maintenance or inactive, with the **Motivo** (for example, *revisão dos 50
   mil km* — 50,000 km service). The **Histórico de situações** (status history) keeps everything.

## 5. Ambulance duty rosters

1. Click **Novo plantão** (new duty) and choose the **Viatura** (ambulance), the **Início** and the **Fim**.
2. Build the **Tripulação** (crew) with **Mais um tripulante** (add crew member): the professional and the **Função** (role:
   driver, nursing technician, nurse or physician).
3. Save.

**Minimum crew by type:**

| Ambulance | Crew |
|-----------|------|
| USB | driver and nursing technician |
| USA | driver, nurse and physician |

The list shows from the last 24 hours to the next 7 days. Ambulances on duty **Agora** (now) appear as available to the 192 centre.

## 6. Vehicles lent to emergencies

Under **Pedidos das emergências** (requests from emergencies), the emergency coordination asks for the vehicle through
mobilisation.

1. Use **Ceder veículo** (lend vehicle): the **Veículo disponível** (available vehicle) and a note (for example, *com condutor da
   garagem* — with a garage driver).
2. Once lent, the vehicle stays in service until it is returned: it does not join new elective trips or the SAMU duty roster. Its
   already scheduled trips come back as warnings to reschedule.
3. **Devolver** (return), with the odometer reading.

## 7. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| Companion required | the patient is under 18 or travels on a stretcher | enter the companion and their document |
| TFD not in a trip | TFD has not been authorised yet | wait for the TFD committee |
| SAMU ambulance refused for the trip | SAMU ambulances do not do elective transport | choose another vehicle |
| *Nenhuma diária concedida.* — no per diem granted | the authorised TFD has no per diem | grant one, if the patient is entitled |
| Trip with a reschedule warning | the vehicle was lent to an emergency | schedule the trip on another vehicle |
