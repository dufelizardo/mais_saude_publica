*** Settings ***
Resource    ../../../pom/profissional/get_all_profissional/get_all_profissional_pom.resource
Resource    ../../profissional/create_profissional/create_profissional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No 404-on-empty-list scenario here — same reasoning as every hierarchy level: no hard-delete endpoint
    exists anywhere in the API, so the list can never be reliably guaranteed empty.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get All Profissional - Status Code 200
    [Documentation]    Seeds a Profissional to guarantee a non-empty result, then validates the GET all
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Profissional Unit

    ${response}=    Call GET Get All Profissional
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PROFISSIONAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get All Profissional
    [Documentation]    Calls the GET Get All Profissional keyword from the POM.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Profissional

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get All Profissional - Status Code 400 - URL Malformada
    [Documentation]    Sends a raw null byte inside the /profissional path itself, expecting the standard error
    ...    contract instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Profissional - Malformed Path
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Profissional - Get All - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get All Profissional test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get All Profissional - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get All Profissional - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get All Profissional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
