*** Settings ***
Library    String
Library    Collections
Resource    ../../../pom/paciente/create_paciente/create_paciente_pom.resource
Resource    ../../../pom/paciente/get_paciente_by_cpf/get_paciente_by_cpf_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Paciente has no FK dependency on any other domain (ver ADR-0039/ADR-0040) — unlike Setor, seeding one
    requires no parent entity. The create endpoint only returns a SuccessResponseDto (same contract as
    Setor/Vaga/Cargo), so to get a freshly created Paciente's uuid back this flow searches by its
    (randomly-suffixed) cpf via GET /api/v1/paciente/cpf/{cpf} — CPF is not unique (ver ADR-0017/DER.md),
    but a fresh random suffix is unique enough within a single test run.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Paciente Payload
    [Documentation]    Builds a valid PacienteRequestDto payload. ${cartaoSus} is optional — when informed,
    ...    adds it to the payload; when left blank, the key is omitted entirely (ver DER.md: nem todo
    ...    paciente tem o CNS em mãos no primeiro contato).
    [Arguments]    ${nome}    ${cpf}    ${cartaoSus}=${EMPTY}    ${ativo}=${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary
    ...    cep=01310-100
    ...    logradouro=Avenida Paulista
    ...    numeroLogradouro=1000
    ...    bairro=Bela Vista
    ...    cidade=São Paulo
    ...    estado=SP

    ${telefones}=    Create List    011-2063-7185

    ${payload}=    Create Dictionary
    ...    nome=${nome}
    ...    cpf=${cpf}
    ...    dataNascimento=1990-05-10
    ...    sexo=FEMININO
    ...    endereco=${endereco}
    ...    telefones=${telefones}
    ...    email=paciente@exemplo.com
    ...    ativo=${ativo}

    IF    '${cartaoSus}' != '${EMPTY}'
        Set To Dictionary    ${payload}    cartaoSus=${cartaoSus}
    END

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Paciente Uuid Por Cpf
    [Documentation]    Fetches every Paciente matching ${cpf} and returns the first match's uuid.
    [Arguments]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Paciente By Cpf    ${cpf}
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Set Variable    ${response.json()[0]['uuid']}

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Paciente - Status Code 201
    [Documentation]    Creates a Paciente with a random-suffixed cpf and returns the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    10    [NUMBERS]
    ${payload}=    Build Paciente Payload    Robot Teste Paciente ${suffix}    ${suffix}

    ${response}=    POST Create Paciente    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Paciente
    [Documentation]    Creates a fresh Paciente with a random-suffixed cpf and CNS, returning its uuid, cpf,
    ...    cartaoSus and nome — useful for GET/PATCH tests that need to operate on a real, freshly created
    ...    Paciente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    10    [NUMBERS]
    ${cpf}=    Set Variable    ${suffix}
    ${cartaoSus}=    Set Variable    7${suffix}
    ${nome}=    Set Variable    Robot Teste Paciente ${suffix}
    ${payload}=    Build Paciente Payload    ${nome}    ${cpf}    ${cartaoSus}

    ${response}=    POST Create Paciente    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${uuid}=    Buscar Paciente Uuid Por Cpf    ${cpf}

    RETURN    ${uuid}    ${cpf}    ${cartaoSus}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Paciente - Status Code 201 - Sem Cartao Sus
    [Documentation]    Creates a Paciente without a cartaoSus (nem todo paciente tem o CNS no primeiro
    ...    contato, ver DER.md), expecting HTTP 201.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    10    [NUMBERS]
    ${payload}=    Build Paciente Payload    Robot Teste Paciente Sem CNS ${suffix}    ${suffix}

    ${response}=    POST Create Paciente    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Paciente - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Paciente with nome/cpf left blank and dataNascimento/sexo/endereco/
    ...    telefones/ativo omitted, expecting the field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    nome=${EMPTY}
    ...    cpf=${EMPTY}

    ${response}=    POST Create Paciente    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Paciente - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Paciente test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Paciente - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Paciente - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Paciente test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
