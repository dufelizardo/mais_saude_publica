*** Settings ***
Resource    ../../../pom/federal/get_all_federal/get_all_federal_pom.resource
Resource    ../../federal/create_federal/create_federal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No 404-on-empty-list scenario here: the endpoint does return 404 when there are zero Federal units
    (AbstractHierarquicoService/getAllFederal), but the DB is shared across the whole suite run, so an
    empty state can never be reliably guaranteed without wiping data other tests depend on. Worse: there is
    no hard-delete endpoint anywhere in the API (des-habilitar only flips "ativo", which getAllFederal()
    doesn't even filter on) — every Federal unit ever created in this database stays forever, so the list
    can never be empty again without a destructive out-of-band DB operation.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get All Federal - Status Code 200
    [Documentation]    Seeds a Federal unit to guarantee a non-empty result, then validates the GET all response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Federal Unit

    ${response}=    Call GET Get All Federal
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${FEDERAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get All Federal
    [Documentation]    Calls the GET Get All Federal keyword from the POM.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Federal

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get All Federal - Status Code 400 - URL Malformada
    [Documentation]    Sends a raw null byte inside the /federal path itself, expecting the standard error
    ...    contract instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Federal - Malformed Path
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Federal - Get All - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get All Federal test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get All Federal - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get All Federal - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get All Federal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
