*** Settings ***
Resource    ../../../pom/auth/eu/eu_pom.resource
Resource    ../../../resource/config/schema/schema.resource
Resource    ../../common/schema_validation_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A suíte não tem credenciais de login (ver login_flow): cobre a resposta sem token, que é 401 com o
    toggle de segurança ligado ou desligado. O caso 200 é coberto pelo AuthControllerTest (JUnit).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Auth Eu - Status Code 401
    [Documentation]    Asks who is logged in without a token, expecting HTTP 401.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Auth Eu
    Should Be Equal As Integers    ${response.status_code}    401
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Auth - Eu - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Auth Eu test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '401'    GET Auth Eu - Status Code 401
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Auth Eu test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
