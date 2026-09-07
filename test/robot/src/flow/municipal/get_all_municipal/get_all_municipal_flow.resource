*** Settings ***
Resource    ../../../pom/municipal/get_all_municipal/get_all_municipal_pom.resource
Resource    ../../municipal/create_municipal/create_municipal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No 404-on-empty-list scenario here — same reasoning as Federal: no hard-delete endpoint exists anywhere
    in the API, so the list can never be reliably guaranteed empty.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get All Municipal - Status Code 200
    [Documentation]    Seeds a Municipal unit to guarantee a non-empty result, then validates the GET all
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Municipal Unit

    ${response}=    Call GET Get All Municipal
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${MUNICIPAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get All Municipal
    [Documentation]    Calls the GET Get All Municipal keyword from the POM.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Municipal

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get All Municipal - Status Code 400 - URL Malformada
    [Documentation]    Sends a raw null byte inside the /municipal path itself, expecting the standard error
    ...    contract instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Municipal - Malformed Path
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Municipal - Get All - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get All Municipal test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get All Municipal - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get All Municipal - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get All Municipal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
