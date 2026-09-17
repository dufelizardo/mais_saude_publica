*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/unidade_saude/create_unidade_saude/create_unidade_saude_pom.resource
Resource    ../../municipal/create_municipal/create_municipal_flow.resource
Resource    ../../estadual/create_estadual/create_estadual_flow.resource
Resource    ../../regional/create_regional/create_regional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Unidade de Saúde (UBS/HOSPITAL) requires a valid administracaoSuperior (Municipal level) —
    vincularSuperiorSeInformado runs BEFORE the tipo check in salvar(), so any scenario that needs to reach
    salvar() must first seed a real Municipal superior. Unlike the other 4 hierarchy levels, this domain also
    accepts two optional lateral fields: supervisaoRegional (must point at a REGIONAL unit) and
    responsavelCpf (a weak link to a Profissional, resolved immediately if it already exists or later by
    reconciliation — see ADR-0013/ADR-0014).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Unidade Saude Payload
    [Documentation]    Builds a valid UnidadeSaudeRequestDto payload, with the given nome and administracaoSuperior
    ...    (must be the nome of an existing Municipal unit). ${tipo} defaults to UBS (the other accepted value
    ...    is HOSPITAL). ${supervisaoRegional} and ${responsavelCpf} are optional and, when left blank, are
    ...    omitted from the payload entirely.
    [Arguments]    ${nome}    ${administracaoSuperior}    ${tipo}=UBS    ${supervisaoRegional}=${EMPTY}
    ...    ${responsavelCpf}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary
    ...    cep=04038-001
    ...    logradouro=Rua Vergueiro
    ...    numeroLogradouro=1200
    ...    bairro=Vila Mariana
    ...    cidade=São Paulo
    ...    estado=SP

    ${payload}=    Create Dictionary
    ...    nome=${nome}
    ...    tipo=${tipo}
    ...    administracaoSuperior=${administracaoSuperior}
    ...    endereco=${endereco}
    ...    email=contato@ubs.saude.sp.gov.br

    IF    '${supervisaoRegional}' != '${EMPTY}'
        Set To Dictionary    ${payload}    supervisaoRegional=${supervisaoRegional}
    END
    IF    '${responsavelCpf}' != '${EMPTY}'
        Set To Dictionary    ${payload}    responsavelCpf=${responsavelCpf}
    END

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Unidade Saude - Status Code 201
    [Arguments]    ${nome}    ${administracaoSuperior}    ${tipo}=UBS    ${supervisaoRegional}=${EMPTY}
    ...    ${responsavelCpf}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Unidade Saude Payload
    ...    ${nome}    ${administracaoSuperior}    ${tipo}    ${supervisaoRegional}    ${responsavelCpf}
    ${response}=    POST Create Unidade Saude    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Unidade Saude Unit
    [Documentation]    Seeds a fresh Federal → Estadual → Municipal chain as superior, then creates a fresh
    ...    Unidade de Saúde unit pointing to the Municipal unit, with a random suffix on the nome so repeated
    ...    runs never collide with the unique "nome" constraint. ${tipo} defaults to UBS (pass HOSPITAL to seed
    ...    the other accepted level). Returns the generated Unidade de Saúde nome.
    [Arguments]    ${tipo}=UBS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Unidade Saude ${suffix}

    POST Create Unidade Saude - Status Code 201    ${nome}    ${nomeSuperior}    ${tipo}

    RETURN    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Unidade Saude Unit With Supervisao Regional
    [Documentation]    Seeds a fresh Municipal superior AND a fresh Regional unit, then creates a fresh Unidade
    ...    de Saúde unit pointing to both (administracaoSuperior + supervisaoRegional), with a random suffix on
    ...    the nome. ${tipo} defaults to HOSPITAL, so the 201-with-supervisaoRegional scenario also doubles as
    ...    coverage for the HOSPITAL tipo value. Returns the generated Unidade de Saúde nome.
    [Arguments]    ${tipo}=HOSPITAL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${nomeSupervisaoRegional}=    Seed A Regional Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Unidade Saude Com Supervisao ${suffix}

    POST Create Unidade Saude - Status Code 201
    ...    ${nome}    ${nomeSuperior}    ${tipo}    ${nomeSupervisaoRegional}

    RETURN    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Unidade Saude - Status Code 409
    [Documentation]    Seeds a Municipal superior, creates a Unidade de Saúde unit, then attempts to create
    ...    another with the same nome, expecting a duplicate-name conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Unidade Saude Duplicado ${suffix}
    POST Create Unidade Saude - Status Code 201    ${nome}    ${nomeSuperior}

    ${payload}=    Build Unidade Saude Payload    ${nome}    ${nomeSuperior}
    ${response}=    POST Create Unidade Saude    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Unidade Saude - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Unidade de Saúde unit with nome and email left blank, expecting a
    ...    field-validation 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${EMPTY}
    ...    tipo=UBS
    ...    email=${EMPTY}

    ${response}=    POST Create Unidade Saude    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Unidade Saude - Status Code 400 - Corpo Ilegivel
    [Documentation]    Sends a syntactically broken JSON body, expecting the standard error contract (HTTP 400)
    ...    instead of Spring's default ProblemDetail (RFC 7807) — see AQUAQE-215.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Unidade Saude - Raw Body    {corpo-quebrado
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Unidade Saude - Status Code 422 - Tipo Divergente
    [Documentation]    Attempts to create a Unidade de Saúde unit with tipo=MUNICIPAL (a mismatched level for
    ...    the /unidade-saude/ endpoint), expecting HTTP 422 (AQUAQE-214). Still needs a valid Municipal
    ...    superior seeded, since vincularSuperiorSeInformado runs before the tipo check in salvar() — with
    ...    tipo=MUNICIPAL the expected superior level becomes ESTADUAL, so this is actually rejected by the
    ...    superior-level check rather than reaching salvar(), but the observable result is the same HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Unidade Saude Payload
    ...    Robot Teste Unidade Saude Tipo Divergente ${suffix}    ${nomeSuperior}
    Set To Dictionary    ${payload}    tipo=MUNICIPAL

    ${response}=    POST Create Unidade Saude    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Unidade Saude - Status Code 422 - Superior Nivel Errado
    [Documentation]    Points administracaoSuperior at an Estadual unit instead of the expected Municipal
    ...    level, expecting HTTP 422 (AQUAQE-22).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperiorErrado}=    Seed A Estadual Unit
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Unidade Saude Payload
    ...    Robot Teste Unidade Saude Superior Errado ${suffix}    ${nomeSuperiorErrado}

    ${response}=    POST Create Unidade Saude    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Unidade Saude - Status Code 404 - Superior Inexistente
    [Documentation]    Points administracaoSuperior at a nome that doesn't exist anywhere, expecting HTTP 404
    ...    (AQUAQE-13).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Unidade Saude Payload
    ...    Robot Teste Unidade Saude Superior Inexistente ${suffix}    Unidade Superior Que Nao Existe De Jeito Nenhum

    ${response}=    POST Create Unidade Saude    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Unidade Saude - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Unidade Saude test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Seed A Unidade Saude Unit
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Unidade Saude - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Unidade Saude - Status Code 422 - Tipo Divergente
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Unidade Saude - Status Code 404 - Superior Inexistente
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Unidade Saude test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
