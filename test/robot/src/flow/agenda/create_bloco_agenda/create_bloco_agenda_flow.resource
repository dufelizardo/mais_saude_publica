*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/agenda/create_bloco_agenda/create_bloco_agenda_pom.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Bloco recorrente por profissional e unidade; o profissional não fica em dois lugares ao mesmo tempo (ADR-0091).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Bloco Agenda Payload
    [Documentation]    Builds a Monday block payload.
    [Arguments]    ${matricula}    ${unidadeId}    ${inicio}=08:00    ${fim}=10:00
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    diaSemana=MONDAY    horaInicio=${inicio}    horaFim=${fim}    duracaoMinutos=${30}    tipo=CONSULTA
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Bloco Agenda
    [Documentation]    Seeds a professional, a unit and a Monday 08:00-10:00 block of 30-minute slots; returns matrícula, unidadeId and next Monday.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}=    Build Bloco Agenda Payload    ${matricula}    ${unidadeId}
    ${criado}=    POST Create Bloco Agenda    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${segunda}=    Evaluate    str(datetime.date.today() + datetime.timedelta(days=(7 - datetime.date.today().weekday()) or 7))    modules=datetime
    RETURN    ${matricula}    ${unidadeId}    ${segunda}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Bloco Agenda - Status Code 201
    [Documentation]    Creates the block and checks the four free slots on next Monday.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${vagas}=    GET Vagas Agenda    ${matricula}    ${unidadeId}    ${segunda}
    Should Be Equal As Integers    ${vagas.status_code}    200
    Validate Array Schema    ${vagas.json()}    ${ITEM_AGENDA_SCHEMA}
    Length Should Be    ${vagas.json()}    4
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Bloco Agenda - Status Code 400 - Fim Antes Do Inicio
    [Documentation]    Sends a block that ends before it starts, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}=    Build Bloco Agenda Payload    ${matricula}    ${unidadeId}    10:00    09:00
    ${response}=    POST Create Bloco Agenda    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Bloco Agenda - Status Code 409 - Bloco Cruzado
    [Documentation]    Creates an overlapping block for the same professional at another unit, expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${outra}=    Seed A Unidade De Saude Uuid
    ${payload}=    Build Bloco Agenda Payload    ${matricula}    ${outra}    09:30    11:00
    ${response}=    POST Create Bloco Agenda    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Bloco Agenda - Create - POST - By HTTP Status Code
    [Documentation]    Executes the Bloco Agenda - Create - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Bloco Agenda - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Bloco Agenda - Status Code 400 - Fim Antes Do Inicio
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Bloco Agenda - Status Code 409 - Bloco Cruzado
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Bloco Agenda - Create - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
