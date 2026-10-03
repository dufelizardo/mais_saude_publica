*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/agenda/get_agenda/get_agenda_pom.resource
Resource    ../create_bloco_agenda/create_bloco_agenda_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A agenda dia a dia, com o resumo do período (ADR-0091).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Agenda - Status Code 200
    [Documentation]    Reads next Monday: four free slots and 0% occupancy.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${response}=    GET Agenda    ${matricula}    ${unidadeId}    ${segunda}    ${segunda}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${AGENDA_SCHEMA}
    Should Be Equal As Integers    ${response.json()}[vagasOfertadas]    4
    Should Be Equal As Integers    ${response.json()}[ocupacaoPercentual]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Agenda - Status Code 400 - Periodo Longo
    [Documentation]    Asks for more than 62 days, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${ate}=    Evaluate    str(datetime.date.fromisoformat($segunda) + datetime.timedelta(days=90))    modules=datetime
    ${response}=    GET Agenda    ${matricula}    ${unidadeId}    ${segunda}    ${ate}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Agenda - GET - By HTTP Status Code
    [Documentation]    Executes the Agenda - GET scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Agenda - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Agenda - Status Code 400 - Periodo Longo
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Agenda - GET test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
