*** Settings ***
Resource    ../../../pom/regional/get_regional_by_nome/get_regional_by_nome_pom.resource
Resource    ../../regional/create_regional/create_regional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Regional By Nome - Status Code 200
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Seed A Regional Unit
    END
    ${response}=    Call GET Get Regional By Nome    ${nome}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${REGIONAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Regional By Nome - Status Code 404
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${response}=    Call GET Get Regional By Nome    ${nome}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Regional By Nome - Status Code 400 - URL Malformada
    [Documentation]    Sends a nome containing a raw null byte (%00), expecting the standard error contract
    ...    instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    Call GET Get Regional By Nome    teste%00
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get Regional By Nome
    [Documentation]    Calls the GET Get Regional By Nome keyword from the POM.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Regional By Nome    ${nome}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regional - Get By Nome - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Regional By Nome test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Regional By Nome - Status Code 200    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Regional By Nome - Status Code 404    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get Regional By Nome - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Regional By Nome test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
