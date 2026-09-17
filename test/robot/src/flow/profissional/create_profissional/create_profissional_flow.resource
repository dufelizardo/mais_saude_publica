*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/profissional/create_profissional/create_profissional_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Profissional is a flat RH domain with no hierarchy chain, no superior and no tipo field (see ADR-0014) —
    same "no dependency" shape as Federal's create flow, just keyed by cpf instead of nome.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Profissional Payload
    [Documentation]    Builds a valid ProfissionalRequestDto payload, with the given cpf and nome.
    [Arguments]    ${cpf}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    cpf=${cpf}
    ...    nome=${nome}
    ...    conselhoClasse=COREN
    ...    numeroConselho=123456-SP
    ...    telefone=1140028922
    ...    email=contato@profissional.saude.sp.gov.br
    ...    dataAdmissao=2026-01-15

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Profissional - Status Code 201
    [Arguments]    ${cpf}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Profissional Payload    ${cpf}    ${nome}
    ${response}=    POST Create Profissional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Profissional Unit
    [Documentation]    Creates a fresh Profissional via the Create Profissional flow, with a random 11-digit cpf
    ...    and a random suffix on the nome so repeated runs never collide with the unique "cpf" constraint.
    ...    Returns the generated cpf.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Generate Random String    11    [NUMBERS]
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Profissional ${suffix}

    POST Create Profissional - Status Code 201    ${cpf}    ${nome}

    RETURN    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Profissional - Status Code 409
    [Documentation]    Seeds a Profissional, then attempts to create another with the same cpf, expecting a
    ...    duplicate-cpf conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Seed A Profissional Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Profissional Payload    ${cpf}    Robot Teste Profissional Duplicado ${suffix}
    ${response}=    POST Create Profissional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Profissional - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Profissional with cpf, nome and email left blank, expecting a
    ...    field-validation 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    cpf=${EMPTY}
    ...    nome=${EMPTY}
    ...    email=${EMPTY}

    ${response}=    POST Create Profissional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Profissional - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Profissional - Raw Body    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Profissional - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Profissional test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Seed A Profissional Unit
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Profissional - Status Code 409
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Profissional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
