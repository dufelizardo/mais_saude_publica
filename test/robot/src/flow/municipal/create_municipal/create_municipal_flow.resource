*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/municipal/create_municipal/create_municipal_pom.resource
Resource    ../../estadual/create_estadual/create_estadual_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Municipal requires a valid administracaoSuperior (Estadual level) — vincularSuperiorSeInformado runs BEFORE
    the tipo check in salvar(), so any scenario that needs to reach salvar() must first seed a real Estadual
    superior.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Municipal Payload
    [Documentation]    Builds a valid MunicipalRequestDto payload, with the given nome and administracaoSuperior
    ...    (must be the nome of an existing Estadual unit).
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
    ...    tipo=MUNICIPAL
    ...    administracaoSuperior=${administracaoSuperior}
    ...    municipio=São Paulo
    ...    endereco=${endereco}
    ...    email=contato@prefeitura.sp.gov.br

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Municipal - Status Code 201
    [Arguments]    ${nome}    ${administracaoSuperior}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Municipal Payload    ${nome}    ${administracaoSuperior}
    ${response}=    POST Create Municipal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Municipal Unit
    [Documentation]    Seeds a fresh Federal → Estadual chain as superior, then creates a fresh Municipal unit
    ...    pointing to the Estadual unit, with a random suffix on the nome so repeated runs never collide with
    ...    the unique "nome" constraint. Returns the generated Municipal nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Estadual Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Municipal ${suffix}

    POST Create Municipal - Status Code 201    ${nome}    ${nomeSuperior}

    RETURN    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Municipal - Status Code 409
    [Documentation]    Seeds an Estadual superior, creates a Municipal unit, then attempts to create another
    ...    with the same nome, expecting a duplicate-name conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Estadual Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Municipal Duplicado ${suffix}
    POST Create Municipal - Status Code 201    ${nome}    ${nomeSuperior}

    ${payload}=    Build Municipal Payload    ${nome}    ${nomeSuperior}
    ${response}=    POST Create Municipal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Municipal - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Municipal unit with nome and email left blank, expecting a
    ...    field-validation 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${EMPTY}
    ...    tipo=MUNICIPAL
    ...    email=${EMPTY}

    ${response}=    POST Create Municipal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Municipal - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Municipal - Raw Body    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Municipal - Status Code 422 - Tipo Divergente
    [Documentation]    Attempts to create a Municipal unit with tipo=UBS (a mismatched level for the
    ...    /municipal/ endpoint), expecting HTTP 422 (AQUAQE-214). Still needs a valid Estadual superior,
    ...    since vincularSuperiorSeInformado runs before the tipo check in salvar().
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Estadual Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Municipal Payload    Robot Teste Municipal Tipo Divergente ${suffix}    ${nomeSuperior}
    Set To Dictionary    ${payload}    tipo=UBS

    ${response}=    POST Create Municipal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Municipal - Status Code 422 - Superior Nivel Errado
    [Documentation]    Points administracaoSuperior at a Municipal unit (same level) instead of the expected
    ...    Estadual level, expecting HTTP 422 (AQUAQE-22) — a second, distinct cause of 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperiorErrado}=    Seed A Municipal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Municipal Payload    Robot Teste Municipal Superior Errado ${suffix}    ${nomeSuperiorErrado}

    ${response}=    POST Create Municipal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Municipal - Status Code 404 - Superior Inexistente
    [Documentation]    Points administracaoSuperior at a nome that doesn't exist anywhere, expecting HTTP 404
    ...    (AQUAQE-13).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Municipal Payload
    ...    Robot Teste Municipal Superior Inexistente ${suffix}    Unidade Superior Que Nao Existe De Jeito Nenhum

    ${response}=    POST Create Municipal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Municipal - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Municipal test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Seed A Municipal Unit
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Municipal - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Municipal - Status Code 422 - Tipo Divergente
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Municipal - Status Code 404 - Superior Inexistente
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Municipal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
