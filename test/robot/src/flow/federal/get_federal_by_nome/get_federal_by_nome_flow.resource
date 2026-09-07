*** Settings ***
Resource    ../../../pom/federal/get_federal_by_nome/get_federal_by_nome_pom.resource
Resource    ../../../flow/federal/create_federal/create_federal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Federal By Nome - Status Code 200
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Seed A Federal Unit
    END
    ${response}=    Call GET Get Federal By Nome    ${nome}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${FEDERAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Federal By Nome - Status Code 404
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${response}=    Call GET Get Federal By Nome    ${nome}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get Federal By Nome
    [Documentation]    Calls the GET Get Federal By Nome keyword from the POM.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Federal By Nome    ${nome}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Federal By Nome - Status Code 400 - URL Malformada
    [Documentation]    Sends a nome containing a raw null byte (%00), which the embedded Tomcat rejects before
    ...    the request ever reaches Spring MVC — expecting the standard error contract instead of a raw HTML
    ...    error page (AQUAQE-216). Note: only %00 is reliable here — invalid percent-encoding like %zz gets
    ...    normalized by RequestsLibrary/urllib3 before it ever leaves the client, so it can't be used to
    ...    reproduce this scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    Call GET Get Federal By Nome    teste%00
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Federal - Get By Nome - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Federal By Nome test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Federal By Nome - Status Code 200    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Federal By Nome - Status Code 404    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get Federal By Nome - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Federal By Nome test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
