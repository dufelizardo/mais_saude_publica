*** Settings ***
Resource    ../../../pom/municipal/update_contato_municipal/update_contato_municipal_pom.resource
Resource    ../../municipal/create_municipal/create_municipal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Update Contato Municipal Payload
    [Documentation]    Builds a valid UnidadeDeSaudeEnderecoRequestDto payload.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary
    ...    cep=02012-040
    ...    logradouro=Rua Padre Marchetti
    ...    numeroLogradouro=557
    ...    bairro=Ipiranga
    ...    cidade=São Paulo
    ...    estado=SP

    ${telefones}=    Create List    1120637185

    ${payload}=    Create Dictionary
    ...    endereco=${endereco}
    ...    telefones=${telefones}
    ...    email=novo-contato@prefeitura.sp.gov.br

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Municipal - Status Code 200
    [Documentation]    Seeds a Municipal unit and updates its contato, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Municipal Unit
    ${payload}=    Build Update Contato Municipal Payload

    ${response}=    PATCH Update Contato Municipal    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Municipal - Status Code 404
    [Documentation]    Attempts to update the contato of a Municipal unit that does not exist, expecting
    ...    HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${payload}=    Build Update Contato Municipal Payload

    ${response}=    PATCH Update Contato Municipal    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Municipal - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to update the contato of a Municipal unit with a blank email, expecting a
    ...    field-validation HTTP 400. The @Valid check runs before the not-found lookup, so no existing nome
    ...    needs to be seeded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    email=${EMPTY}

    ${response}=    PATCH Update Contato Municipal    Unidade Que Nao Existe De Jeito Nenhum    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Municipal - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Contato Municipal - Raw Body
    ...    Unidade Que Nao Existe De Jeito Nenhum    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Municipal - Update Contato - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Contato Municipal test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Contato Municipal - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Contato Municipal - Status Code 404    ${meuNome}
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Contato Municipal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
