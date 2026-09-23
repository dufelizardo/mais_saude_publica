*** Settings ***
Library    String
Resource    ../../../pom/capacidade_administrativa/create_capacidade_administrativa/create_capacidade_administrativa_pom.resource
Resource    ../../../pom/capacidade_administrativa/get_all_capacidades_administrativas/get_all_capacidades_administrativas_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    CapacidadeAdministrativa é um catálogo sem FK (diferente de Setor) — a única dependência de seed
    é gerar um código único a cada execução. O create endpoint só retorna SuccessResponseDto (sem
    id), então o uuid é resolvido listando tudo e filtrando pelo código, igual ao Setor.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Capacidade Administrativa Payload
    [Documentation]    Builds a valid CapacidadeAdministrativaRequestDto payload.
    [Arguments]    ${codigo}    ${nome}    ${descricao}=${EMPTY}    ${ativo}=${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    codigo=${codigo}
    ...    nome=${nome}
    ...    descricao=${descricao}
    ...    ativo=${ativo}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Capacidade Administrativa Uuid Por Codigo
    [Documentation]    Lists every Capacidade Administrativa and returns the uuid of the one matching ${codigo}.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Capacidades Administrativas
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [c['uuid'] for c in $response.json() if c['codigo'] == '${codigo}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Capacidade Administrativa - Status Code 201
    [Documentation]    Creates a Capacidade Administrativa with a random-suffixed codigo and returns the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Capacidade Administrativa Payload
    ...    ROBOT-CAP-${suffix}    Robot Teste Capacidade ${suffix}

    ${response}=    POST Create Capacidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Capacidade Administrativa
    [Documentation]    Creates a fresh Capacidade Administrativa with a random-suffixed codigo and returns its uuid
    ...    and codigo — useful for GET-by-id/PATCH tests that need to operate on a real, freshly created row.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${codigo}=    Set Variable    ROBOT-CAP-${suffix}
    ${payload}=    Build Capacidade Administrativa Payload
    ...    ${codigo}    Robot Teste Capacidade ${suffix}

    ${response}=    POST Create Capacidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${uuid}=    Buscar Capacidade Administrativa Uuid Por Codigo    ${codigo}

    RETURN    ${uuid}    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Capacidade Administrativa - Status Code 409
    [Documentation]    Creates a Capacidade Administrativa, then attempts to create another with the same codigo,
    ...    expecting a duplicate-code conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${codigo}=    Seed A Capacidade Administrativa
    ${payload}=    Build Capacidade Administrativa Payload    ${codigo}    Robot Teste Capacidade Duplicada

    ${response}=    POST Create Capacidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Capacidade Administrativa - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Capacidade Administrativa with codigo/nome left blank, expecting the
    ...    field-validation HTTP 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    codigo=${EMPTY}
    ...    nome=${EMPTY}

    ${response}=    POST Create Capacidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Capacidade Administrativa - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Capacidade Administrativa test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Capacidade Administrativa - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Capacidade Administrativa - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Capacidade Administrativa - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Capacidade Administrativa test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
