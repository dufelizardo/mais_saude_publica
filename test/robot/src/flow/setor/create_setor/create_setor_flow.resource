*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/setor/create_setor/create_setor_pom.resource
Resource    ../../../pom/setor/get_all_setores/get_all_setores_pom.resource
Resource    ../../unidade_saude/create_unidade_saude/create_unidade_saude_flow.resource
Resource    ../../unidade_saude/get_unidade_saude_by_nome/get_unidade_saude_by_nome_flow.resource
Resource    ../../profissional/create_profissional/create_profissional_flow.resource
Resource    ../../profissional/get_profissional_by_cpf/get_profissional_by_cpf_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Setor references a Unidade de Saúde by uuid (unidadeId), not by nome — unlike the original 4-level
    hierarchy. The create endpoint only returns a SuccessResponseDto (no id in the body, same contract as
    Vaga/Cargo), so to get a freshly created Setor's uuid back we list every Setor (GET /api/v1/setor/) and
    match by its (randomly-suffixed) codigo — same approach used by the JUnit fixtures in
    SetorControllerTest.
    Setor's optional responsável references a Profissional by matrícula, not uuid — ProfissionalResponseDto
    never exposes uuid (ver ADR-0034). Seeding a matrícula means creating a Profissional (which returns only
    cpf) and then fetching it by cpf to read the auto-generated matricula back out.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Unidade De Saude Uuid
    [Documentation]    Seeds a fresh Unidade de Saúde unit (Federal → Estadual → Municipal → UBS chain) and
    ...    returns its uuid, resolved via GET by nome (the create endpoint only returns a nome-bearing
    ...    SuccessResponseDto).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Unidade Saude Unit
    ${response}=    Call GET Get Unidade Saude By Nome    ${nome}
    ${uuid}=    Set Variable    ${response.json()['uuid']}

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Build Setor Payload
    [Documentation]    Builds a valid SetorRequestDto payload. ${matriculaResponsavel} is optional — when
    ...    informed, adds it to the payload (see ADR-0034); when left blank, the key is omitted entirely.
    [Arguments]    ${unidadeId}    ${nome}    ${codigo}    ${tipo}=ADMINISTRATIVO    ${ativo}=${True}
    ...    ${matriculaResponsavel}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    unidadeId=${unidadeId}
    ...    nome=${nome}
    ...    codigo=${codigo}
    ...    tipo=${tipo}
    ...    ativo=${ativo}

    IF    '${matriculaResponsavel}' != '${EMPTY}'
        Set To Dictionary    ${payload}    matriculaResponsavel=${matriculaResponsavel}
    END

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Profissional Matricula
    [Documentation]    Seeds a fresh Profissional and returns its auto-generated matrícula, resolved via GET by
    ...    cpf (the create endpoint only returns a cpf-bearing SuccessResponseDto, not the matricula).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Seed A Profissional Unit
    ${response}=    Call GET Get Profissional By Cpf    ${cpf}
    ${matricula}=    Set Variable    ${response.json()['matricula']}

    RETURN    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Setor Uuid Por Codigo
    [Documentation]    Lists every Setor and returns the uuid of the one matching ${codigo}.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Setores
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [s['uuid'] for s in $response.json() if s['codigo'] == '${codigo}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Setor - Status Code 201
    [Documentation]    Seeds a fresh Unidade de Saúde, creates a Setor pointing at it with a random suffix on
    ...    nome/codigo, and returns the response.
    [Arguments]    ${tipo}=ADMINISTRATIVO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Setor Payload
    ...    ${unidadeId}    Robot Teste Setor ${suffix}    ROBOT-${suffix}    ${tipo}

    ${response}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Setor
    [Documentation]    Seeds a fresh Unidade de Saúde and a fresh Setor pointing at it, returning the Setor's
    ...    uuid, its codigo and the parent unidade's uuid — useful for GET-by-id/PATCH tests that need to
    ...    operate on a real, freshly created Setor.
    [Arguments]    ${tipo}=ADMINISTRATIVO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${codigo}=    Set Variable    ROBOT-${suffix}
    ${payload}=    Build Setor Payload
    ...    ${unidadeId}    Robot Teste Setor ${suffix}    ${codigo}    ${tipo}

    ${response}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${setorId}=    Buscar Setor Uuid Por Codigo    ${codigo}

    RETURN    ${setorId}    ${codigo}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Setor - Status Code 404 - Unidade Inexistente
    [Documentation]    Attempts to create a Setor pointing at a unidadeId that doesn't exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Setor Payload
    ...    ${unidadeIdInexistente}    Robot Teste Setor ${suffix}    ROBOT-${suffix}

    ${response}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Setor - Status Code 201 - Com Responsavel
    [Documentation]    Seeds a fresh Unidade de Saúde and a fresh Profissional, creates a Setor pointing at both,
    ...    and returns the Setor's uuid alongside the Profissional's matrícula (ver ADR-0034) — useful for the
    ...    scenario keyword to then GET and assert the responsavelMatricula/responsavelNome fields.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matriculaResponsavel}=    Seed A Profissional Matricula
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${codigo}=    Set Variable    ROBOT-${suffix}
    ${payload}=    Build Setor Payload
    ...    ${unidadeId}    Robot Teste Setor ${suffix}    ${codigo}    ADMINISTRATIVO    ${True}
    ...    ${matriculaResponsavel}

    ${response}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${setorId}=    Buscar Setor Uuid Por Codigo    ${codigo}

    RETURN    ${setorId}    ${matriculaResponsavel}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Setor - Status Code 404 - Responsavel Inexistente
    [Documentation]    Attempts to create a Setor pointing at a matriculaResponsavel that doesn't exist,
    ...    expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Setor Payload
    ...    ${unidadeId}    Robot Teste Setor ${suffix}    ROBOT-${suffix}    ADMINISTRATIVO    ${True}
    ...    MATRICULA-INEXISTENTE-${suffix}

    ${response}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Setor - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Setor with nome/codigo left blank and unidadeId/tipo omitted,
    ...    expecting the field-validation HTTP 400 (MethodArgumentNotValidException).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${EMPTY}
    ...    codigo=${EMPTY}

    ${response}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Setor - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Setor test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Setor - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Setor - Status Code 404 - Unidade Inexistente
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Setor - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Setor test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
