*** Settings ***
Resource    ../../../pom/estadual/update_horario_atendimento_estadual/update_horario_atendimento_estadual_pom.resource
Resource    ../../estadual/create_estadual/create_estadual_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Update Horario De Atendimento Estadual Payload
    [Documentation]    Builds a valid UnidadeDeSaudeHorarioDeAtendimentoRequestDto payload.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${horarioAtendimento}=    Create Dictionary
    ...    MONDAY=08:00-17:00
    ...    TUESDAY=08:00-17:00

    ${payload}=    Create Dictionary
    ...    horarioAtendimento=${horarioAtendimento}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Atendimento Estadual - Status Code 200
    [Documentation]    Seeds an Estadual unit and updates its horario de atendimento, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Estadual Unit
    ${payload}=    Build Update Horario De Atendimento Estadual Payload

    ${response}=    PATCH Update Horario De Atendimento Estadual    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Atendimento Estadual - Status Code 404
    [Documentation]    Attempts to update the horario de atendimento of an Estadual unit that does not exist,
    ...    expecting HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${payload}=    Build Update Horario De Atendimento Estadual Payload

    ${response}=    PATCH Update Horario De Atendimento Estadual    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Atendimento Estadual - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215. This is the ONLY reachable 400
    ...    for this endpoint, since UnidadeDeSaudeHorarioDeAtendimentoRequestDto has no field constraints.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Horario De Atendimento Estadual - Raw Body
    ...    Unidade Que Nao Existe De Jeito Nenhum    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estadual - Update Horario De Atendimento - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Horario De Atendimento Estadual test scenario based on
    ...    the provided HTTP status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Horario De Atendimento Estadual - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'
    ...        PATCH Update Horario De Atendimento Estadual - Status Code 404    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '400'
    ...        PATCH Update Horario De Atendimento Estadual - Status Code 400 - Corpo Ilegivel
    ...    ELSE
    ...        Fail
    ...        HTTP status code '${meuHTTP}' is not supported for Update Horario De Atendimento Estadual test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
