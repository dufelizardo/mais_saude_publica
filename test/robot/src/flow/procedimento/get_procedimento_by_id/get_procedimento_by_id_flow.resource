*** Settings ***
Resource    ../../../pom/procedimento/get_procedimento_by_id/get_procedimento_by_id_pom.resource
Resource    ../create_procedimento/create_procedimento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Procedimento By Id
    [Documentation]    Calls the GET Get Procedimento By Id keyword from the POM.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Procedimento By Id    ${uuid}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Procedimento By Id - Status Code 200
    [Documentation]    Seeds a fresh Procedimento (unless ${uuid} is informed) and fetches it by id,
    ...    expecting HTTP 200.
    [Arguments]    ${uuid}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${uuid}' == '${EMPTY}'
        ${uuid}    ${consultaId}    ${matricula}=    Seed Um Procedimento
    END
    ${response}=    Call GET Get Procedimento By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PROCEDIMENTO_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Procedimento By Id - Status Code 404
    [Documentation]    Fetches a Procedimento by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    Call GET Get Procedimento By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Procedimento By Id test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Procedimento By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Procedimento By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Procedimento By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
