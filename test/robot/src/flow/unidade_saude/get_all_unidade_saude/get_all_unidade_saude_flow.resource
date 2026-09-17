*** Settings ***
Resource    ../../../pom/unidade_saude/get_all_unidade_saude/get_all_unidade_saude_pom.resource
Resource    ../../unidade_saude/create_unidade_saude/create_unidade_saude_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No 404-on-empty-list scenario here — same reasoning as Federal/Regional: no hard-delete endpoint exists
    anywhere in the API, so the list can never be reliably guaranteed empty.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get All Unidade Saude - Status Code 200
    [Documentation]    Seeds a Unidade de Saúde unit to guarantee a non-empty result, then validates the GET all
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Unidade Saude Unit

    ${response}=    Call GET Get All Unidade Saude
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${UNIDADE_SAUDE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get All Unidade Saude
    [Documentation]    Calls the GET Get All Unidade Saude keyword from the POM.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Unidade Saude

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get All Unidade Saude - Status Code 400 - URL Malformada
    [Documentation]    Sends a raw null byte inside the /unidade-saude path itself, expecting the standard
    ...    error contract instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Unidade Saude - Malformed Path
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Unidade Saude - Get All - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get All Unidade Saude test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get All Unidade Saude - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get All Unidade Saude - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get All Unidade Saude test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
