*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/estadual/create_estadual/create_estadual_pom.resource
Resource    ../../federal/create_federal/create_federal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Estadual requires a valid administracaoSuperior (Federal level) — vincularSuperiorSeInformado runs BEFORE
    the tipo check in salvar(), so any scenario that needs to reach salvar() must first seed a real Federal
    superior.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Estadual Payload
    [Documentation]    Builds a valid EstadualRequestDto payload, with the given nome and administracaoSuperior
    ...    (must be the nome of an existing Federal unit).
    [Arguments]    ${nome}    ${administracaoSuperior}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary
    ...    cep=01310-100
    ...    logradouro=Avenida Paulista
    ...    numeroLogradouro=1000
    ...    bairro=Bela Vista
    ...    cidade=São Paulo
    ...    estado=SP

    ${payload}=    Create Dictionary
    ...    nome=${nome}
    ...    tipo=ESTADUAL
    ...    administracaoSuperior=${administracaoSuperior}
    ...    estado=SP
    ...    endereco=${endereco}
    ...    email=contato@saude.sp.gov.br

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Estadual - Status Code 201
    [Arguments]    ${nome}    ${administracaoSuperior}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Estadual Payload    ${nome}    ${administracaoSuperior}
    ${response}=    POST Create Estadual    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Estadual Unit
    [Documentation]    Seeds a fresh Federal unit as superior, then creates a fresh Estadual unit pointing to
    ...    it, with a random suffix on the nome so repeated runs never collide with the unique "nome"
    ...    constraint. Returns the generated Estadual nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Federal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Estadual ${suffix}

    POST Create Estadual - Status Code 201    ${nome}    ${nomeSuperior}

    RETURN    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Estadual - Status Code 409
    [Documentation]    Seeds a Federal superior, creates an Estadual unit, then attempts to create another with
    ...    the same nome, expecting a duplicate-name conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Federal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Estadual Duplicado ${suffix}
    POST Create Estadual - Status Code 201    ${nome}    ${nomeSuperior}

    ${payload}=    Build Estadual Payload    ${nome}    ${nomeSuperior}
    ${response}=    POST Create Estadual    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Estadual - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create an Estadual unit with nome and email left blank, expecting a
    ...    field-validation 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${EMPTY}
    ...    tipo=ESTADUAL
    ...    email=${EMPTY}

    ${response}=    POST Create Estadual    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Estadual - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Estadual - Raw Body    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Estadual - Status Code 422 - Tipo Divergente
    [Documentation]    Attempts to create an Estadual unit with tipo=UBS (a mismatched level for the /estadual/
    ...    endpoint), expecting HTTP 422 (AQUAQE-214). Still needs a valid Federal superior, since
    ...    vincularSuperiorSeInformado runs before the tipo check in salvar().
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Federal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Estadual Payload    Robot Teste Estadual Tipo Divergente ${suffix}    ${nomeSuperior}
    Set To Dictionary    ${payload}    tipo=UBS

    ${response}=    POST Create Estadual    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Estadual - Status Code 422 - Superior Nivel Errado
    [Documentation]    Points administracaoSuperior at an Estadual unit (same level) instead of the expected
    ...    Federal level, expecting HTTP 422 (AQUAQE-22) — a second, distinct cause of 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperiorErrado}=    Seed A Estadual Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Estadual Payload    Robot Teste Estadual Superior Errado ${suffix}    ${nomeSuperiorErrado}

    ${response}=    POST Create Estadual    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Estadual - Status Code 404 - Superior Inexistente
    [Documentation]    Points administracaoSuperior at a nome that doesn't exist anywhere, expecting HTTP 404
    ...    (AQUAQE-13).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Estadual Payload
    ...    Robot Teste Estadual Superior Inexistente ${suffix}    Unidade Superior Que Nao Existe De Jeito Nenhum

    ${response}=    POST Create Estadual    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estadual - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Estadual test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Seed A Estadual Unit
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Estadual - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Estadual - Status Code 422 - Tipo Divergente
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Estadual - Status Code 404 - Superior Inexistente
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Estadual test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
