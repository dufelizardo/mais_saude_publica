*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/federal/create_federal/create_federal_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Federal Payload
    [Documentation]    Builds a valid FederalRequestDto payload, with the given nome.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary
    ...    cep=70058-900
    ...    logradouro=Esplanada dos Ministérios
    ...    numeroLogradouro=Bloco G
    ...    bairro=Zona Cívico-Administrativa
    ...    cidade=Brasília
    ...    estado=DF

    ${payload}=    Create Dictionary
    ...    nome=${nome}
    ...    tipo=FEDERAL
    ...    endereco=${endereco}
    ...    email=contato@saude.gov.br

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Federal - Status Code 201
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Federal Payload    ${nome}
    ${response}=    POST Create Federal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Federal Unit
    [Documentation]    Creates a fresh Federal unit via the Create Federal flow, with a random suffix on the nome
    ...    so repeated runs never collide with the unique "nome" constraint. Returns the generated nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Federal ${suffix}

    POST Create Federal - Status Code 201    ${nome}

    RETURN    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Federal - Status Code 409
    [Documentation]    Seeds a Federal unit, then attempts to create another with the same nome, expecting a
    ...    duplicate-name conflict (HTTP 409). Duplicate-key violations are translated to 409 by
    ...    GlobalExceptionHandler (either via an explicit controller catch, for creation, or via the global
    ...    DataIntegrityViolationException handler, for every other mutation path).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Federal Unit
    ${payload}=    Build Federal Payload    ${nome}
    ${response}=    POST Create Federal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Federal - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Federal unit with nome and email left blank, expecting a
    ...    field-validation 400 (MethodArgumentNotValidException, distinct from the duplicate-name 400 above).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${EMPTY}
    ...    tipo=FEDERAL
    ...    email=${EMPTY}

    ${response}=    POST Create Federal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Federal - Status Code 422 - Tipo Divergente
    [Documentation]    Attempts to create a Federal unit with tipo=UBS (a mismatched level for the /federal/
    ...    endpoint), expecting HTTP 422. AbstractHierarquicoService.salvar() rejects any tipo that doesn't
    ...    match the domain's own TipoUnidadeDeSaude (see AQUAQE-214) — without this check the unit would be
    ...    silently created but orphaned (invisible to every endpoint, since UBS has no controller).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Federal Payload    Robot Teste Federal Tipo Divergente ${suffix}
    Set To Dictionary    ${payload}    tipo=UBS

    ${response}=    POST Create Federal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Federal - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Federal - Raw Body    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Federal - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Federal test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Seed A Federal Unit
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Federal - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Federal - Status Code 422 - Tipo Divergente
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Federal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
