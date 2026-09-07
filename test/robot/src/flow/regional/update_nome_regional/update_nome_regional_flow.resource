*** Settings ***
Resource    ../../../pom/regional/update_nome_regional/update_nome_regional_pom.resource
Resource    ../../regional/create_regional/create_regional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Update Nome Regional Payload
    [Documentation]    Builds a valid UnidadeDeSaudeNomeUpdateRequestDto payload with the given novoNome.
    [Arguments]    ${novoNome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${novoNome}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Nome Regional - Status Code 200
    [Documentation]    Seeds a Regional unit and renames it, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeOriginal}=    Seed A Regional Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${novoNome}=    Set Variable    Robot Teste Regional Renomeado ${suffix}
    ${payload}=    Build Update Nome Regional Payload    ${novoNome}

    ${response}=    PATCH Update Nome Regional    ${nomeOriginal}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Nome Regional - Status Code 404
    [Documentation]    Attempts to rename a Regional unit that does not exist, expecting HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${payload}=    Build Update Nome Regional Payload    Nome Qualquer

    ${response}=    PATCH Update Nome Regional    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Nome Regional - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to rename a Regional unit to a blank nome, expecting a field-validation HTTP 400.
    ...    The @Valid check runs before the not-found lookup, so no existing nome needs to be seeded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Update Nome Regional Payload    ${EMPTY}

    ${response}=    PATCH Update Nome Regional    Unidade Que Nao Existe De Jeito Nenhum    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Nome Regional - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Nome Regional - Raw Body    Unidade Que Nao Existe De Jeito Nenhum    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Nome Regional - Status Code 409
    [Documentation]    Seeds two Regional units, then renames the second one to the first one's nome, expecting
    ...    a duplicate-name conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeExistente}=    Seed A Regional Unit
    ${nomeASerRenomeado}=    Seed A Regional Unit
    ${payload}=    Build Update Nome Regional Payload    ${nomeExistente}

    ${response}=    PATCH Update Nome Regional    ${nomeASerRenomeado}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regional - Update Nome - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Nome Regional test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Nome Regional - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Nome Regional - Status Code 404    ${meuNome}
    ...    ELSE IF    '${meuHTTP}' == '409'    PATCH Update Nome Regional - Status Code 409
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Nome Regional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
