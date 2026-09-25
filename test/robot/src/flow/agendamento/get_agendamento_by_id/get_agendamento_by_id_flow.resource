*** Settings ***
Resource    ../../../pom/agendamento/get_agendamento_by_id/get_agendamento_by_id_pom.resource
Resource    ../create_agendamento/create_agendamento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Agendamento By Id
    [Documentation]    Calls the GET Get Agendamento By Id keyword from the POM.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Agendamento By Id    ${uuid}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Agendamento By Id - Status Code 200
    [Documentation]    Seeds a fresh Agendamento (unless ${uuid} is informed) and fetches it by id, expecting
    ...    HTTP 200.
    [Arguments]    ${uuid}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${uuid}' == '${EMPTY}'
        ${uuid}    ${pacienteId}    ${matricula}=    Seed Um Agendamento
    END
    ${response}=    Call GET Get Agendamento By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${AGENDAMENTO_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Agendamento By Id - Status Code 404
    [Documentation]    Fetches an Agendamento by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    Call GET Get Agendamento By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Agendamento - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Agendamento By Id test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Agendamento By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Agendamento By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Agendamento By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
