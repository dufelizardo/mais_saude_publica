*** Settings ***
Resource    ../../../pom/estadual/desabilitar_estadual/desabilitar_estadual_pom.resource
Resource    ../../estadual/create_estadual/create_estadual_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DELETE Desabilitar Estadual - Status Code 200
    [Documentation]    Seeds an Estadual unit and disables it (ativo=false), expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Estadual Unit

    ${response}=    DELETE Desabilitar Estadual    ${nome}    ${False}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Desabilitar Estadual - Status Code 404
    [Documentation]    Attempts to enable/disable an Estadual unit that does not exist, expecting HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END

    ${response}=    DELETE Desabilitar Estadual    ${nome}    ${False}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Desabilitar Estadual - Status Code 400 - URL Malformada
    [Documentation]    Sends a nome containing a raw null byte (%00), expecting the standard error contract
    ...    instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    DELETE Desabilitar Estadual    teste%00    ${False}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estadual - Desabilitar - DELETE - By HTTP Status Code
    [Documentation]    Executes the appropriate Desabilitar Estadual test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    DELETE Desabilitar Estadual - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    DELETE Desabilitar Estadual - Status Code 404    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '400'    DELETE Desabilitar Estadual - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Desabilitar Estadual test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
