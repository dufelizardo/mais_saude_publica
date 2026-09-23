*** Settings ***
Library    String
Resource    ../../../pom/perfil_administrativo/create_perfil_administrativo/create_perfil_administrativo_pom.resource
Resource    ../../../pom/perfil_administrativo/get_all_perfis_administrativos/get_all_perfis_administrativos_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    PerfilAdministrativo é um catálogo sem FK (mesmo padrão de CapacidadeAdministrativa). O create
    endpoint só retorna SuccessResponseDto, então o uuid é resolvido listando tudo e filtrando pelo
    código.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Perfil Administrativo Payload
    [Documentation]    Builds a valid PerfilAdministrativoRequestDto payload.
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

Buscar Perfil Administrativo Uuid Por Codigo
    [Documentation]    Lists every Perfil Administrativo and returns the uuid of the one matching ${codigo}.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Perfis Administrativos
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [p['uuid'] for p in $response.json() if p['codigo'] == '${codigo}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Perfil Administrativo - Status Code 201
    [Documentation]    Creates a Perfil Administrativo with a random-suffixed codigo and returns the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Perfil Administrativo Payload
    ...    ROBOT-PERFIL-${suffix}    Robot Teste Perfil ${suffix}

    ${response}=    POST Create Perfil Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Perfil Administrativo
    [Documentation]    Creates a fresh Perfil Administrativo with a random-suffixed codigo and returns its uuid and
    ...    codigo — useful for tests that need a real, freshly created row.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${codigo}=    Set Variable    ROBOT-PERFIL-${suffix}
    ${payload}=    Build Perfil Administrativo Payload
    ...    ${codigo}    Robot Teste Perfil ${suffix}

    ${response}=    POST Create Perfil Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${uuid}=    Buscar Perfil Administrativo Uuid Por Codigo    ${codigo}

    RETURN    ${uuid}    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Perfil Administrativo - Status Code 409
    [Documentation]    Creates a Perfil Administrativo, then attempts to create another with the same codigo,
    ...    expecting a duplicate-code conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${codigo}=    Seed A Perfil Administrativo
    ${payload}=    Build Perfil Administrativo Payload    ${codigo}    Robot Teste Perfil Duplicado

    ${response}=    POST Create Perfil Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Perfil Administrativo - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Perfil Administrativo with codigo/nome left blank, expecting the
    ...    field-validation HTTP 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    codigo=${EMPTY}
    ...    nome=${EMPTY}

    ${response}=    POST Create Perfil Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Perfil Administrativo - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Perfil Administrativo test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Perfil Administrativo - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Perfil Administrativo - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Perfil Administrativo - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Perfil Administrativo test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
