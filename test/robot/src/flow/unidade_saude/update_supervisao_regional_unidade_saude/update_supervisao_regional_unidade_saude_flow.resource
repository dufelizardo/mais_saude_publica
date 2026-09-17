*** Settings ***
Resource    ../../../pom/unidade_saude/update_supervisao_regional_unidade_saude/update_supervisao_regional_unidade_saude_pom.resource
Resource    ../../unidade_saude/create_unidade_saude/create_unidade_saude_flow.resource
Resource    ../../regional/create_regional/create_regional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    No Regional equivalent — this operation only exists for Unidade de Saúde. It vincula a lateral, non-
    hierarchical link to a REGIONAL unit (see AbstractHierarquicoService.vincularSupervisaoRegionalSeInformado
    and ADR-0013). Unlike administracaoSuperior, this method is reused as-is by every hierarchy level, but only
    the Unidade de Saúde controller exposes a dedicated endpoint for updating it after creation.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Update Supervisao Regional Unidade Saude Payload
    [Documentation]    Builds a valid UnidadeSaudeSupervisaoRegionalRequestDto payload with the given
    ...    supervisaoRegional (must be the nome of an existing Regional unit).
    [Arguments]    ${supervisaoRegional}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    supervisaoRegional=${supervisaoRegional}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Supervisao Regional Unidade Saude - Status Code 200
    [Documentation]    Seeds a Unidade de Saúde unit and a Regional unit, then links them, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Unidade Saude Unit
    ${nomeSupervisaoRegional}=    Seed A Regional Unit
    ${payload}=    Build Update Supervisao Regional Unidade Saude Payload    ${nomeSupervisaoRegional}

    ${response}=    PATCH Update Supervisao Regional Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Supervisao Regional Unidade Saude - Status Code 404 - Unidade Inexistente
    [Documentation]    Attempts to link the supervisaoRegional of a Unidade de Saúde unit that does not exist,
    ...    expecting HTTP 404. buscarUnidadeDeSaudePorNome runs before the supervisaoRegional lookup, so the
    ...    supervisaoRegional value itself doesn't need to point at a real Regional unit here.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${payload}=    Build Update Supervisao Regional Unidade Saude Payload    Regional Qualquer

    ${response}=    PATCH Update Supervisao Regional Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Supervisao Regional Unidade Saude - Status Code 404 - Supervisao Regional Inexistente
    [Documentation]    Seeds a Unidade de Saúde unit, then attempts to link it to a supervisaoRegional nome that
    ...    doesn't exist anywhere, expecting HTTP 404 (see
    ...    AbstractHierarquicoService.vincularSupervisaoRegionalSeInformado).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Unidade Saude Unit
    ${payload}=    Build Update Supervisao Regional Unidade Saude Payload
    ...    Unidade Supervisao Regional Que Nao Existe De Jeito Nenhum

    ${response}=    PATCH Update Supervisao Regional Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Supervisao Regional Unidade Saude - Status Code 422 - Supervisao Regional Nivel Errado
    [Documentation]    Seeds a Unidade de Saúde unit and a Municipal unit (wrong level — must be REGIONAL),
    ...    then attempts to link the Municipal unit as supervisaoRegional, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Unidade Saude Unit
    ${nomeMunicipalErrado}=    Seed A Municipal Unit
    ${payload}=    Build Update Supervisao Regional Unidade Saude Payload    ${nomeMunicipalErrado}

    ${response}=    PATCH Update Supervisao Regional Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Supervisao Regional Unidade Saude - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to update the supervisaoRegional of a Unidade de Saúde unit with a blank value,
    ...    expecting a field-validation HTTP 400. The @Valid check runs before the not-found lookup, so no
    ...    existing nome needs to be seeded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    supervisaoRegional=${EMPTY}

    ${response}=    PATCH Update Supervisao Regional Unidade Saude
    ...    Unidade Que Nao Existe De Jeito Nenhum    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Supervisao Regional Unidade Saude - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Supervisao Regional Unidade Saude - Raw Body
    ...    Unidade Que Nao Existe De Jeito Nenhum    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Unidade Saude - Update Supervisao Regional - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Supervisao Regional Unidade Saude test scenario based on
    ...    the provided HTTP status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Supervisao Regional Unidade Saude - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'
    ...        PATCH Update Supervisao Regional Unidade Saude - Status Code 404 - Unidade Inexistente    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '422'
    ...        PATCH Update Supervisao Regional Unidade Saude - Status Code 422 - Supervisao Regional Nivel Errado
    ...    ELSE
    ...        Fail
    ...        HTTP status code '${meuHTTP}' is not supported for Update Supervisao Regional Unidade Saude test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
