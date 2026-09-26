*** Settings ***
Library    Collections
Library    String
Resource    ../../../pom/auth/login/login_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Login não tem um endpoint público para provisionar um Usuario (criação acontece só via bootstrap
    do AdministradorPlataforma ou reconciliação interna — ver ADR-0055), então esta suíte cobre os
    cenários alcançáveis puramente por HTTP sem depender de dado pré-existente: validação de campo
    obrigatório (400) e credenciais que certamente não existem (401). O caminho de sucesso (200) já é
    coberto por AuthControllerTest (JUnit), que semeia um Usuario diretamente via repositório.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Login Payload
    [Documentation]    Builds a LoginRequestDto payload.
    [Arguments]    ${tipo}=CPF    ${identificador}=00000000000    ${senha}=SenhaQualquer123    ${manterConectado}=${FALSE}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    tipo=${tipo}
    ...    identificador=${identificador}
    ...    senha=${senha}
    ...    manterConectado=${manterConectado}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Login - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to log in with every field omitted, expecting the field-validation
    ...    HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Login    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Login - Status Code 401 - Credenciais Inexistentes
    [Documentation]    Attempts to log in with a CPF that certainly doesn't exist, expecting the
    ...    generic-message HTTP 401 (never reveals whether the identifier or the password was wrong).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    9    [NUMBERS]
    ${payload}=    Build Login Payload    tipo=CPF    identificador=${sufixo}    senha=SenhaQualquer123

    ${response}=    POST Login    ${payload}
    Should Be Equal As Integers    ${response.status_code}    401
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Login - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Login test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '400'    POST Login - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE IF    '${meuHTTP}' == '401'    POST Login - Status Code 401 - Credenciais Inexistentes
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Login test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
