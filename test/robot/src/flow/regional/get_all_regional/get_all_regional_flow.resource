*** Settings ***
Resource    ../../../pom/regional/get_all_regional/get_all_regional_pom.resource
Resource    ../../regional/create_regional/create_regional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No 404-on-empty-list scenario here — same reasoning as Federal: no hard-delete endpoint exists anywhere
    in the API, so the list can never be reliably guaranteed empty.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get All Regional - Status Code 200
    [Documentation]    Seeds a Regional unit to guarantee a non-empty result, then validates the GET all
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Regional Unit

    ${response}=    Call GET Get All Regional
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${REGIONAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get All Regional
    [Documentation]    Calls the GET Get All Regional keyword from the POM.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Regional

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get All Regional - Status Code 400 - URL Malformada
    [Documentation]    Sends a raw null byte inside the /regional path itself, expecting the standard error
    ...    contract instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Regional - Malformed Path
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regional - Get All - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get All Regional test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get All Regional - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get All Regional - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get All Regional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
