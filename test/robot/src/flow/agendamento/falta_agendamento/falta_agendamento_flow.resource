*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/agendamento/falta_agendamento/falta_agendamento_pom.resource
Resource    ../../agenda/create_bloco_agenda/create_bloco_agenda_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Marcação com unidade segue a agenda: vaga livre ou encaixe; a falta entra no resumo (ADR-0091).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Agendamento Na Agenda Payload
    [Documentation]    Builds an agendamento payload at the unit.
    [Arguments]    ${pacienteId}    ${matricula}    ${unidadeId}    ${dataHora}    ${encaixe}=${False}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    profissionalMatricula=${matricula}    dataHora=${dataHora}    status=AGENDADO    tipo=CONSULTA    unidadeId=${unidadeId}    encaixe=${encaixe}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Registrar Falta Agendamento - Status Code 200
    [Documentation]    Books the 08:00 slot and records the no-show.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${payload}=    Build Agendamento Na Agenda Payload    ${pacienteId}    ${matricula}    ${unidadeId}    ${segunda}T08:00:00
    ${criado}=    POST Create Agendamento Na Agenda    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET Agendamentos Do Paciente    ${pacienteId}
    Validate Array Schema    ${lista.json()}    ${AGENDAMENTO_SCHEMA}
    ${response}=    POST Registrar Falta Agendamento    ${lista.json()}[0][uuid]
    Should Be Equal As Integers    ${response.status_code}    200
    ${depois}=    GET Agendamentos Do Paciente    ${pacienteId}
    Should Be Equal    ${depois.json()}[0][status]    FALTOU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Registrar Falta Agendamento - Status Code 422 - Fora Da Agenda
    [Documentation]    Books outside the slots without encaixe, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${payload}=    Build Agendamento Na Agenda Payload    ${pacienteId}    ${matricula}    ${unidadeId}    ${segunda}T08:10:00
    ${response}=    POST Create Agendamento Na Agenda    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Agendamento - Falta - POST - By HTTP Status Code
    [Documentation]    Executes the Agendamento - Falta - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Registrar Falta Agendamento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Registrar Falta Agendamento - Status Code 422 - Fora Da Agenda
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Agendamento - Falta - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
