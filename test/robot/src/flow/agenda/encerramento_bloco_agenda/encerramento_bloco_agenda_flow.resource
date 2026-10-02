*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/agenda/encerramento_bloco_agenda/encerramento_bloco_agenda_pom.resource
Resource    ../create_bloco_agenda/create_bloco_agenda_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Encerrar o bloco tira as vagas depois da data; as marcações ficam (ADR-0091).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Encerrar Bloco Agenda - Status Code 200
    [Documentation]    Ends the block the day before next Monday and checks there are no slots left.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${blocos}=    GET Blocos Do Profissional    ${matricula}
    Validate Array Schema    ${blocos.json()}    ${BLOCO_AGENDA_SCHEMA}
    ${uuid}=    Set Variable    ${blocos.json()}[0][uuid]
    ${vespera}=    Evaluate    str(datetime.date.fromisoformat($segunda) - datetime.timedelta(days=1))    modules=datetime
    ${payload}=    Create Dictionary    vigenteAte=${vespera}
    ${response}=    POST Encerrar Bloco Agenda    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${vagas}=    GET Vagas Agenda    ${matricula}    ${unidadeId}    ${segunda}
    Length Should Be    ${vagas.json()}    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encerrar Bloco Agenda - Status Code 404
    [Documentation]    Ends a random block, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    vigenteAte=2030-01-01
    ${response}=    POST Encerrar Bloco Agenda    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Bloco Agenda - Encerramento - POST - By HTTP Status Code
    [Documentation]    Executes the Bloco Agenda - Encerramento - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Encerrar Bloco Agenda - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Encerrar Bloco Agenda - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Bloco Agenda - Encerramento - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
