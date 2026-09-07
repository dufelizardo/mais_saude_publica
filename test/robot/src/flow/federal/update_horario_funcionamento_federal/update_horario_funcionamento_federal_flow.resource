*** Settings ***
Resource    ../../../pom/federal/update_horario_funcionamento_federal/update_horario_funcionamento_federal_pom.resource
Resource    ../../federal/create_federal/create_federal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Update Horario De Funcionamento Federal Payload
    [Documentation]    Builds a valid UnidadeDeSaudeHorarioDeFuncionamentoRequestDto payload.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${horarioFuncionamento}=    Create Dictionary
    ...    MONDAY=07:00-19:00
    ...    TUESDAY=07:00-19:00

    ${payload}=    Create Dictionary
    ...    horarioFuncionamento=${horarioFuncionamento}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Funcionamento Federal - Status Code 200
    [Documentation]    Seeds a Federal unit and updates its horario de funcionamento, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Federal Unit
    ${payload}=    Build Update Horario De Funcionamento Federal Payload

    ${response}=    PATCH Update Horario De Funcionamento Federal    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Funcionamento Federal - Status Code 404
    [Documentation]    Attempts to update the horario de funcionamento of a Federal unit that does not exist,
    ...    expecting HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${payload}=    Build Update Horario De Funcionamento Federal Payload

    ${response}=    PATCH Update Horario De Funcionamento Federal    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Funcionamento Federal - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215. This is the ONLY reachable 400
    ...    for this endpoint, since UnidadeDeSaudeHorarioDeFuncionamentoRequestDto has no field constraints.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Horario De Funcionamento Federal - Raw Body
    ...    Unidade Que Nao Existe De Jeito Nenhum    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Federal - Update Horario De Funcionamento - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Horario De Funcionamento Federal test scenario based on
    ...    the provided HTTP status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Horario De Funcionamento Federal - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'
    ...        PATCH Update Horario De Funcionamento Federal - Status Code 404    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '400'
    ...        PATCH Update Horario De Funcionamento Federal - Status Code 400 - Corpo Ilegivel
    ...    ELSE
    ...        Fail
    ...        HTTP status code '${meuHTTP}' is not supported for Update Horario De Funcionamento Federal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
