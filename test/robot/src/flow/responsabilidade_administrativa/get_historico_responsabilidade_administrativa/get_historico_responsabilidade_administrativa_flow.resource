*** Settings ***
Resource    ../../../pom/responsabilidade_administrativa/get_historico_responsabilidade_administrativa/get_historico_responsabilidade_administrativa_pom.resource
Resource    ../create_responsabilidade_administrativa/create_responsabilidade_administrativa_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Historico Responsabilidade Administrativa
    [Documentation]    Calls the GET Get Historico Responsabilidade Administrativa keyword from the POM.
    [Arguments]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Historico Responsabilidade Administrativa    ${matricula}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Historico Responsabilidade Administrativa - Status Code 200
    [Documentation]    Seeds a fresh Responsabilidade Administrativa (unless ${matricula} is informed) and
    ...    fetches its histórico, expecting HTTP 200.
    [Arguments]    ${matricula}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${matricula}' == '${EMPTY}'
        ${matricula}    ${setorId}=    Seed A Responsabilidade Administrativa
    END
    ${response}=    Call GET Get Historico Responsabilidade Administrativa    ${matricula}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${RESPONSABILIDADE_ADMINISTRATIVA_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Historico Responsabilidade Administrativa - Status Code 404
    [Documentation]    Fetches the histórico of a Profissional matrícula that doesn't have any Responsabilidade
    ...    Administrativa, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${response}=    Call GET Get Historico Responsabilidade Administrativa    ${matricula}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Responsabilidade Administrativa - Get Historico - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Historico Responsabilidade Administrativa test scenario
    ...    based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Historico Responsabilidade Administrativa - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Historico Responsabilidade Administrativa - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Historico Responsabilidade Administrativa test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
