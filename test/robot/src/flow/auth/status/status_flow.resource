*** Settings ***
Resource    ../../../pom/auth/status/status_pom.resource
Resource    ../../../resource/config/schema/schema.resource
Resource    ../../common/schema_validation_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O valor de securityEnabled não é fixado nesta suíte (varia por ambiente — sempre false no
    profile de teste/CI hoje, ver ADR-0055) — só valida a forma da resposta.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Auth Status - Status Code 200
    [Documentation]    Fetches the security toggle status, expecting HTTP 200 with the expected shape.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Auth Status
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${AUTH_STATUS_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Auth Status - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Auth Status test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Auth Status - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Auth Status test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
