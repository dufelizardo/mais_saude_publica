*** Settings ***
Resource    ../../../pom/unidade_saude/update_responsavel_unidade_saude/update_responsavel_unidade_saude_pom.resource
Resource    ../../unidade_saude/create_unidade_saude/create_unidade_saude_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No Regional equivalent — this operation only exists for Unidade de Saúde. It updates the weak,
    reconciliation-based link to a Profissional by CPF (see ADR-0014). No Profissional needs to exist for the
    happy path: UnidadeSaudeService.updateResponsavel() always persists responsavelCpf, and only resolves
    responsavel (the actual FK) if a Profissional with that CPF already exists — otherwise the link stays
    pending until a matching Profissional is created (ProfissionalService.create) or the scheduled
    reconciliation job runs.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Update Responsavel Unidade Saude Payload
    [Documentation]    Builds a valid UnidadeSaudeResponsavelRequestDto payload with the given responsavelCpf.
    [Arguments]    ${responsavelCpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    responsavelCpf=${responsavelCpf}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Responsavel Unidade Saude - Status Code 200
    [Documentation]    Seeds a Unidade de Saúde unit and links it to an arbitrary CPF, expecting HTTP 200. No
    ...    Profissional needs to exist yet — the link resolves later by reconciliation (see ADR-0014).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Unidade Saude Unit
    ${payload}=    Build Update Responsavel Unidade Saude Payload    52998224725

    ${response}=    PATCH Update Responsavel Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Responsavel Unidade Saude - Status Code 404
    [Documentation]    Attempts to update the responsavelCpf of a Unidade de Saúde unit that does not exist,
    ...    expecting HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${payload}=    Build Update Responsavel Unidade Saude Payload    52998224725

    ${response}=    PATCH Update Responsavel Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Responsavel Unidade Saude - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to update the responsavelCpf of a Unidade de Saúde unit with a blank value,
    ...    expecting a field-validation HTTP 400. The @Valid check runs before the not-found lookup, so no
    ...    existing nome needs to be seeded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    responsavelCpf=${EMPTY}

    ${response}=    PATCH Update Responsavel Unidade Saude    Unidade Que Nao Existe De Jeito Nenhum    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Responsavel Unidade Saude - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Responsavel Unidade Saude - Raw Body
    ...    Unidade Que Nao Existe De Jeito Nenhum    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Unidade Saude - Update Responsavel - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Responsavel Unidade Saude test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Responsavel Unidade Saude - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Responsavel Unidade Saude - Status Code 404    ${meuNome}
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Responsavel Unidade Saude test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
