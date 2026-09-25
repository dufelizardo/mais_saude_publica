*** Settings ***
Library    String
Resource    ../../../pom/processo_administrativo/create_processo_administrativo/create_processo_administrativo_pom.resource
Resource    ../../../pom/processo_administrativo/get_all_processos_administrativos/get_all_processos_administrativos_pom.resource
Resource    ../../capacidade_administrativa/create_capacidade_administrativa/create_capacidade_administrativa_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    ProcessoAdministrativo referencia uma CapacidadeAdministrativa por uuid (não por nome) — sem
    constraint de unicidade em codigo (diferente de Setor/Capacidade/Perfil), então não há cenário
    de 409 aqui, igual Vaga/Cargo no RH. O create endpoint só retorna SuccessResponseDto, então o
    uuid é resolvido listando tudo e filtrando pela capacidade vinculada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Processo Administrativo Payload
    [Documentation]    Builds a valid ProcessoAdministrativoRequestDto payload.
    [Arguments]    ${capacidadeId}    ${codigo}    ${nome}    ${descricao}=${EMPTY}    ${ativo}=${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    capacidadeId=${capacidadeId}
    ...    codigo=${codigo}
    ...    nome=${nome}
    ...    descricao=${descricao}
    ...    ativo=${ativo}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Processo Administrativo Uuid Por Capacidade
    [Documentation]    Lists every Processo Administrativo and returns the uuid of the first one linked to
    ...    ${capacidadeId}.
    [Arguments]    ${capacidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Processos Administrativos
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [p['uuid'] for p in $response.json() if p['capacidadeUuid'] == '${capacidadeId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Processo Administrativo - Status Code 201
    [Documentation]    Seeds a fresh Capacidade Administrativa and creates a Processo Administrativo pointing at
    ...    it, returning the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${capacidadeId}    ${codigoCapacidade}=    Seed A Capacidade Administrativa
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Processo Administrativo Payload
    ...    ${capacidadeId}    ROBOT-PROC-${suffix}    Robot Teste Processo ${suffix}

    ${response}=    POST Create Processo Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Processo Administrativo
    [Documentation]    Seeds a fresh Capacidade Administrativa and a fresh Processo Administrativo pointing at
    ...    it, returning the Processo's uuid and the parent capacidade's uuid — useful for GET-by-id/PATCH
    ...    tests that need to operate on a real, freshly created Processo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${capacidadeId}    ${codigoCapacidade}=    Seed A Capacidade Administrativa
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Processo Administrativo Payload
    ...    ${capacidadeId}    ROBOT-PROC-${suffix}    Robot Teste Processo ${suffix}

    ${response}=    POST Create Processo Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${processoId}=    Buscar Processo Administrativo Uuid Por Capacidade    ${capacidadeId}

    RETURN    ${processoId}    ${capacidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Processo Administrativo - Status Code 404 - Capacidade Inexistente
    [Documentation]    Attempts to create a Processo Administrativo pointing at a capacidadeId that doesn't
    ...    exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${capacidadeIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Processo Administrativo Payload
    ...    ${capacidadeIdInexistente}    ROBOT-PROC-${suffix}    Robot Teste Processo ${suffix}

    ${response}=    POST Create Processo Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Processo Administrativo - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Processo Administrativo with capacidadeId/codigo/nome/ativo left
    ...    blank/omitted, expecting the field-validation HTTP 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    codigo=${EMPTY}
    ...    nome=${EMPTY}

    ${response}=    POST Create Processo Administrativo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Processo Administrativo - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Processo Administrativo test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Processo Administrativo - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Processo Administrativo - Status Code 404 - Capacidade Inexistente
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Processo Administrativo - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Processo Administrativo test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
