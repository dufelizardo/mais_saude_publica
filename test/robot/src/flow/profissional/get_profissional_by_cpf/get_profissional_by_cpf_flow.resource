*** Settings ***
Resource    ../../../pom/profissional/get_profissional_by_cpf/get_profissional_by_cpf_pom.resource
Resource    ../../profissional/create_profissional/create_profissional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Profissional By Cpf - Status Code 200
    [Arguments]    ${cpf}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${cpf}' == '${EMPTY}'
        ${cpf}=    Seed A Profissional Unit
    END
    ${response}=    Call GET Get Profissional By Cpf    ${cpf}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PROFISSIONAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Profissional By Cpf - Status Code 404
    [Arguments]    ${cpf}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${cpf}' == '${EMPTY}'
        ${cpf}=    Set Variable    00000000000
    END
    ${response}=    Call GET Get Profissional By Cpf    ${cpf}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Profissional By Cpf - Status Code 400 - URL Malformada
    [Documentation]    Sends a cpf containing a raw null byte (%00), expecting the standard error contract
    ...    instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    Call GET Get Profissional By Cpf    teste%00
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call GET Get Profissional By Cpf
    [Documentation]    Calls the GET Get Profissional By Cpf keyword from the POM.
    [Arguments]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Profissional By Cpf    ${cpf}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Profissional - Get By Cpf - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Profissional By Cpf test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Profissional By Cpf - Status Code 200    ${meuCpf}
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Profissional By Cpf - Status Code 404    ${meuCpf}
    ...    ELSE IF    '${meuHTTP}' == '400'    GET Get Profissional By Cpf - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Profissional By Cpf test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
