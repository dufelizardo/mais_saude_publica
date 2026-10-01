*** Settings ***
Resource    ../../../pom/auth/recuperacao/recuperacao_pom.resource
Resource    ../../../resource/config/schema/schema.resource
Resource    ../../common/schema_validation_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A suíte de API roda sem login e sem SMTP: a recuperação fica indisponível (422) e a validação do corpo vem antes (400). O fluxo completo, com e-mail, está no teste de interface (tag SEGURANCA) e no RecuperacaoSenhaControllerTest.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Auth Senha Recuperacao - Status Code 400
    [Documentation]    Asks for the link with a blank CPF, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    cpf=${EMPTY}
    ${response}=    POST Auth Senha Recuperacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Auth Senha Recuperacao - Status Code 422
    [Documentation]    Asks for the link where recovery is not available (no login/SMTP), expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    cpf=52998224725
    ${response}=    POST Auth Senha Recuperacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Auth Senha Redefinicao - Status Code 400
    [Documentation]    Sets a too short new password, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    token=qualquer    novaSenha=curta
    ${response}=    POST Auth Senha Redefinicao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Auth Senha Redefinicao - Status Code 422
    [Documentation]    Uses a link where recovery is not available, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    token=qualquer-token-de-teste    novaSenha=SenhaNova123
    ${response}=    POST Auth Senha Redefinicao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Auth - Recuperacao - POST - By HTTP Status Code
    [Documentation]    Executes the Auth Senha Recuperacao scenario for the status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '400'    POST Auth Senha Recuperacao - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Auth Senha Recuperacao - Status Code 422
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Auth Senha Recuperacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Auth - Redefinicao - POST - By HTTP Status Code
    [Documentation]    Executes the Auth Senha Redefinicao scenario for the status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '400'    POST Auth Senha Redefinicao - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Auth Senha Redefinicao - Status Code 422
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Auth Senha Redefinicao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
