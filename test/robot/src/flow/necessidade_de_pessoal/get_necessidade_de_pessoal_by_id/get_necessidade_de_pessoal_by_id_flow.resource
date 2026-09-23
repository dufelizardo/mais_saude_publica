*** Settings ***
Resource    ../../../pom/necessidade_de_pessoal/get_necessidade_de_pessoal_by_id/get_necessidade_de_pessoal_by_id_pom.resource
Resource    ../create_necessidade_de_pessoal/create_necessidade_de_pessoal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Necessidade De Pessoal By Id
    [Documentation]    Calls the GET Get Necessidade De Pessoal By Id keyword from the POM.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Necessidade De Pessoal By Id    ${uuid}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Necessidade De Pessoal By Id - Status Code 200
    [Documentation]    Seeds a fresh Necessidade De Pessoal (unless ${uuid} is informed) and fetches it by id,
    ...    expecting HTTP 200.
    [Arguments]    ${uuid}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${uuid}' == '${EMPTY}'
        ${uuid}    ${unidadeId}    ${cargoId}=    Seed A Necessidade De Pessoal
    END
    ${response}=    Call GET Get Necessidade De Pessoal By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${NECESSIDADE_DE_PESSOAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Necessidade De Pessoal By Id - Status Code 404
    [Documentation]    Fetches a Necessidade De Pessoal by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    Call GET Get Necessidade De Pessoal By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Necessidade De Pessoal - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Necessidade De Pessoal By Id test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Necessidade De Pessoal By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Necessidade De Pessoal By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Necessidade De Pessoal By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
