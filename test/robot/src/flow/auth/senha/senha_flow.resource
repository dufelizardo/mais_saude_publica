*** Settings ***
Resource    ../../../pom/auth/senha/senha_pom.resource
Resource    ../../../resource/config/schema/schema.resource
Resource    ../../common/schema_validation_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A suíte não tem credenciais de login (ver login_flow): cobre a troca sem token, que é 401 com o toggle de
    segurança ligado ou desligado. Os casos 200/400/422 e a senha provisória são cobertos pelo AuthControllerTest.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Auth Senha - Status Code 401
    [Documentation]    Tries to change the password without a token, expecting HTTP 401.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    senhaAtual=SenhaAtual123    novaSenha=SenhaNova123
    ${response}=    POST Auth Senha    ${payload}
    Should Be Equal As Integers    ${response.status_code}    401
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Auth - Senha - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Auth Senha test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '401'    POST Auth Senha - Status Code 401
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Auth Senha test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

