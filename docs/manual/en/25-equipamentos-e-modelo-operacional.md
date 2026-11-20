# Health facilities and operating model

> **Version:** 1.15.0 · [Português](../pt-BR/25-equipamentos-e-modelo-operacional.md)

Menu: **Administrativo › Equipamentos de Saúde** (health facilities) and **Administrativo › Modelo operacional** (operating
model).

- **Equipamentos de Saúde:** the network's units (primary care unit, emergency care unit, hospital, CAPS…) and management levels,
  with the network map and each unit's record.
- **Modelo operacional:** the catalogue of what units operate and can do: profiles, capabilities and service types.

**Who uses it:** those who maintain the structure (the platform administrator) and those who read administration (managers).

## 1. The network of units

**The cards at the top:**

- **Unidades ativas** (active units) and **UBS** (primary care units);
- **UPA / Hospital / CAPS e especializadas** (emergency units, hospitals, psychosocial centres and specialised units);
- **Em manutenção / obras / inoperantes** (under maintenance, works or out of operation), with no schedule slots.

**Search and filter:** by unit or CNES (national health facility register number), and by **Regional** (region), **Tipo** (type:
care units or management levels), **Situação** (status) and **Perfil operacional** (operating profile).

**View on the Mapa (map) or the Lista (list).** Units without coordinates appear only in the list. Mark the location in the record,
under **Dados gerais** (general data).

**Exportar** (export) downloads the list.

## 2. The unit's record

Click the unit to open its record, with the tabs:

| Tab | What it has |
|-----|-------------|
| **Dados gerais** (general data) | name, CNES (7 digits), type (not changed by editing), address, contact, person in charge (by CPF) and location on the map |
| **Operacional** (operational) | opening hours, capacity (beds and consulting rooms) and status |
| **Capacidades** (capabilities) | the unit's operating profiles and capabilities, and the **Leitos autorizados** (authorised beds) |
| **Serviços** (services) | how the unit is organised: adult ICU, vaccination room, emergency room… |
| **Pontos** (points) | where the unit works outside its address (school, square) and **Rotas programadas** (scheduled routes); for a mobile unit, **Onde está agora** (where it is now) |
| **Equipes** (teams) | posted professionals and teams (registered in Human resources › Teams) |
| **Vinculações** (links) | the hierarchy: parent unit and regional supervision; sectors and the bed map |
| **Histórico** (history) | status changes, with **Ver tudo na auditoria** (see everything in the audit trail) |

**Registering or editing:**

1. Enter the **Nome**, the type, the **CNES** and the **Nível de gestão** (management level). Only the level above the chosen type
   appears; for a care unit, the municipal level.
2. Fill in the address and contact.
3. Under **CPF do responsável** (CPF of the person in charge), enter the CPF. It links to the professional with the same CPF;
   without a record, it stays pending until they exist.
4. Under **Localização** (location), click on the map or type the coordinates (WGS84). Without coordinates, the unit does not
   appear on the network map or the territory map.

**Opening hours:**

1. Use **Editar horário** (edit hours) and, for each day, tick **Aberto** (open) and enter the **1º turno** (first shift) and,
   optionally, the **2º turno** (second shift). For a lunch break, use two shifts that day.
2. The schedule only offers slots within the shifts.
3. **Funciona 24 horas** (open 24 hours) applies to hospitals and emergency units: then opening hours do not close the schedule.

> Without structured hours, the schedule does not close according to the unit's hours.

**Changing status:** use **Mudar situação** (change status), with the **Status operacional** (for example, under maintenance or
under works), the **Motivo** (reason) and the **Previsão de retorno** (expected return). A unit out of operation offers no slots,
and its appointments appear in emergencies.

**The unit's profiles and capabilities:**

1. **Adicionar perfil** (add profile): what the unit operates (primary care, emergency, inpatient…), with the **Desde** (since)
   date. A unit can have several profiles: a hospital is inpatient and emergency.
2. **Adicionar capacidade** (add capability): only cross-cutting ones (reception, walk-in care) and those of the unit's profiles.
   Use the **Observação** (note) for, for example, the accreditation ordinance.
3. **Leitos autorizados** (management): the number of beds authorised per capability. Use **Alterar limite** (change limit). Bed
   registration above it is blocked.

**Points and position:**

- **Adicionar ponto** (add point) and **Adicionar rota** (add route) show where the unit works outside its address.
- For a mobile unit, **Registrar posição** (record position) states where it is. The position is entered by whoever operates it;
  there is no tracking.

## 3. The operating model catalogue

**The cards:**

- **Perfis ativos** (active profiles: primary care, emergency, inpatient…);
- **Capacidades ativas** (active capabilities);
- **Perfis em unidades** (profiles in units): current associations.

**The sections:**

- **Perfis operacionais** (operating profiles): what a unit operates.
- **Capacidades assistenciais** (care capabilities): what a unit can do. A profile's capability only enters units that have that
  profile. Without a profile (group), the capability applies to any care unit. A capability's profile cannot change while it is
  in any unit.
- **Tipos de serviço** (service types): how the capability is organised at the unit. A type may require a capability: then the
  service only enters units with that capability, and it cannot change while there is a current service of that type.

Each item has a **Código** (stored in capitals), a **Nome** and a **Descrição**. An inactive item stays in the catalogue and in units
that already have it, but does not enter new units.

> Capabilities drive several screens: the 192 centre needs *Regulação médica de urgência* (emergency medical regulation), the
> imaging report needs diagnostic imaging, and Regulation checks the performing unit's capability.

## 4. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| *Sem coordenadas: a unidade não aparece no mapa…* — no coordinates: the unit does not appear on the map | the unit has no location | mark it on the map under Dados gerais |
| *Sem horário estruturado…* — no structured hours | the unit has no opening hours | register the hours |
| *Nenhum perfil…* — no profile | the unit has no operating profile | add the profile |
| *Não foi possível carregar as equipes (é preciso ter acesso ao RH ou às equipes).* — teams could not be loaded (HR or team access required) | your profile does not read HR | ask for the profile, if your role needs it |
