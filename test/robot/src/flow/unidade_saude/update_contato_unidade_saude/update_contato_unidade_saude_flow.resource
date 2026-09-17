*** Settings ***
Resource    ../../../pom/unidade_saude/update_contato_unidade_saude/update_contato_unidade_saude_pom.resource
Resource    ../../unidade_saude/create_unidade_saude/create_unidade_saude_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Update Contato Unidade Saude Payload
    [Documentation]    Builds a valid UnidadeDeSaudeEnderecoRequestDto payload.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary
    ...    cep=05403-000
    ...    logradouro=Avenida Doutor Arnaldo
    ...    numeroLogradouro=455
    ...    bairro=Cerqueira César
    ...    cidade=São Paulo
    ...    estado=SP

    ${telefones}=    Create List    1130618000

    ${payload}=    Create Dictionary
    ...    endereco=${endereco}
    ...    telefones=${telefones}
    ...    email=novo-contato@ubs.saude.sp.gov.br

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Unidade Saude - Status Code 200
    [Documentation]    Seeds a Unidade de Saúde unit and updates its contato, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Unidade Saude Unit
    ${payload}=    Build Update Contato Unidade Saude Payload

    ${response}=    PATCH Update Contato Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Unidade Saude - Status Code 404
    [Documentation]    Attempts to update the contato of a Unidade de Saúde unit that does not exist, expecting
    ...    HTTP 404.
    [Arguments]    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${nome}=    Set Variable    Unidade Que Nao Existe De Jeito Nenhum
    END
    ${payload}=    Build Update Contato Unidade Saude Payload

    ${response}=    PATCH Update Contato Unidade Saude    ${nome}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Unidade Saude - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to update the contato of a Unidade de Saúde unit with a blank email, expecting
    ...    a field-validation HTTP 400. The @Valid check runs before the not-found lookup, so no existing nome
    ...    needs to be seeded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    email=${EMPTY}

    ${response}=    PATCH Update Contato Unidade Saude    Unidade Que Nao Existe De Jeito Nenhum    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Contato Unidade Saude - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Contato Unidade Saude - Raw Body
    ...    Unidade Que Nao Existe De Jeito Nenhum    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Unidade Saude - Update Contato - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Contato Unidade Saude test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Contato Unidade Saude - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Contato Unidade Saude - Status Code 404    ${meuNome}
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Contato Unidade Saude test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
