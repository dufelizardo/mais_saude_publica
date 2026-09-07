*** Settings ***
Resource    ../../../pom/federal/desabilitar_federal/desabilitar_federal_pom.resource
Resource    ../../federal/create_federal/create_federal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DELETE Desabilitar Federal - Status Code 200
    [Documentation]    Seeds a Federal unit and disables it (ativo=false), expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Federal Unit

    ${response}=    DELETE Desabilitar Federal    ${nome}    ${False}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Desabilitar Federal - Status Code 404
    [Documentation]    Attempts to enable/disable a Federal unit that does not exist, expecting HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END

    ${response}=    DELETE Desabilitar Federal    ${nome}    ${False}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Desabilitar Federal - Status Code 400 - URL Malformada
    [Documentation]    Sends a nome containing a raw null byte (%00), which the embedded Tomcat rejects before
    ...    the request ever reaches Spring MVC — expecting the standard error contract instead of a raw HTML
    ...    error page (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    DELETE Desabilitar Federal    teste%00    ${False}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Federal - Desabilitar - DELETE - By HTTP Status Code
    [Documentation]    Executes the appropriate Desabilitar Federal test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    DELETE Desabilitar Federal - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    DELETE Desabilitar Federal - Status Code 404    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '400'    DELETE Desabilitar Federal - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Desabilitar Federal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
