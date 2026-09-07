*** Settings ***
Resource    ../../../pom/estadual/get_all_estadual/get_all_estadual_pom.resource
Resource    ../../estadual/create_estadual/create_estadual_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No 404-on-empty-list scenario here — same reasoning as Federal: no hard-delete endpoint exists anywhere
    in the API, so the list can never be reliably guaranteed empty.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get All Estadual - Status Code 200
    [Documentation]    Seeds an Estadual unit to guarantee a non-empty result, then validates the GET all
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Estadual Unit

    ${response}=    Call GET Get All Estadual
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${ESTADUAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get All Estadual
    [Documentation]    Calls the GET Get All Estadual keyword from the POM.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Estadual

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get All Estadual - Status Code 400 - URL Malformada
    [Documentation]    Sends a raw null byte inside the /estadual path itself, expecting the standard error
    ...    contract instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Estadual - Malformed Path
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estadual - Get All - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get All Estadual test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get All Estadual - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get All Estadual - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get All Estadual test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
