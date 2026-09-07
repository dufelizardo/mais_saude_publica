*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/regional/create_regional/create_regional_pom.resource
Resource    ../../municipal/create_municipal/create_municipal_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Unlike Federal, Regional requires a valid administracaoSuperior (Municipal level) — vincularSuperiorSeInformado
    runs BEFORE the tipo check in salvar(), so any scenario that needs to reach salvar() must first seed a real
    Municipal superior.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Regional Payload
    [Documentation]    Builds a valid RegionalRequestDto payload, with the given nome and administracaoSuperior
    ...    (must be the nome of an existing Municipal unit).
    [Arguments]    ${nome}    ${administracaoSuperior}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary
    ...    cep=02012-040
    ...    logradouro=Rua Padre Marchetti
    ...    numeroLogradouro=557
    ...    bairro=Ipiranga
    ...    cidade=São Paulo
    ...    estado=SP

    ${payload}=    Create Dictionary
    ...    nome=${nome}
    ...    tipo=REGIONAL
    ...    administracaoSuperior=${administracaoSuperior}
    ...    regiao=Sudeste
    ...    endereco=${endereco}
    ...    email=contato@saude.regional.sp.gov.br

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Regional - Status Code 201
    [Arguments]    ${nome}    ${administracaoSuperior}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Regional Payload    ${nome}    ${administracaoSuperior}
    ${response}=    POST Create Regional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Regional Unit
    [Documentation]    Seeds a fresh Federal → Estadual → Municipal chain as superior, then creates a fresh
    ...    Regional unit pointing to the Municipal unit, with a random suffix on the nome so repeated runs
    ...    never collide with the unique "nome" constraint. Returns the generated Regional nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Regional ${suffix}

    POST Create Regional - Status Code 201    ${nome}    ${nomeSuperior}

    RETURN    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Regional - Status Code 409
    [Documentation]    Seeds a Municipal superior, creates a Regional unit, then attempts to create another with
    ...    the same nome, expecting a duplicate-name conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Regional Duplicado ${suffix}
    POST Create Regional - Status Code 201    ${nome}    ${nomeSuperior}

    ${payload}=    Build Regional Payload    ${nome}    ${nomeSuperior}
    ${response}=    POST Create Regional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Regional - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Regional unit with nome and email left blank, expecting a
    ...    field-validation 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${EMPTY}
    ...    tipo=REGIONAL
    ...    email=${EMPTY}

    ${response}=    POST Create Regional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Regional - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Regional - Raw Body    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Regional - Status Code 422 - Tipo Divergente
    [Documentation]    Attempts to create a Regional unit with tipo=UBS (a mismatched level for the /regional/
    ...    endpoint), expecting HTTP 422 (AQUAQE-214). Still needs a valid Municipal superior, since
    ...    vincularSuperiorSeInformado runs before the tipo check in salvar().
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Regional Payload    Robot Teste Regional Tipo Divergente ${suffix}    ${nomeSuperior}
    Set To Dictionary    ${payload}    tipo=UBS

    ${response}=    POST Create Regional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Regional - Status Code 422 - Superior Nivel Errado
    [Documentation]    Points administracaoSuperior at a Federal unit instead of the expected Municipal level,
    ...    expecting HTTP 422 (AQUAQE-22) — a second, distinct cause of 422 that doesn't exist for Federal
    ...    (which has no unidadeSuperior at all).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperiorErrado}=    Seed A Federal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Regional Payload    Robot Teste Regional Superior Errado ${suffix}    ${nomeSuperiorErrado}

    ${response}=    POST Create Regional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Regional - Status Code 404 - Superior Inexistente
    [Documentation]    Points administracaoSuperior at a nome that doesn't exist anywhere, expecting HTTP 404
    ...    (AQUAQE-13) — unique to domains with a superior (Estadual/Municipal/Regional), Federal has no
    ...    equivalent 404 on create.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Regional Payload
    ...    Robot Teste Regional Superior Inexistente ${suffix}    Unidade Superior Que Nao Existe De Jeito Nenhum

    ${response}=    POST Create Regional    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regional - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Regional test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Seed A Regional Unit
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Regional - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Regional - Status Code 422 - Tipo Divergente
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Regional - Status Code 404 - Superior Inexistente
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Regional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
