*** Settings ***
Library    Collections
Resource    ../../../pom/evolucao_enfermagem/create_evolucao_enfermagem/create_evolucao_enfermagem_pom.resource
Resource    ../../../pom/evolucao_enfermagem/get_all_evolucoes_enfermagem/get_all_evolucoes_enfermagem_pom.resource
Resource    ../../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    EvolucaoEnfermagem depends on Atendimento and Profissional. Reaproveita "Seed Um Atendimento" (de
    create_atendimento_flow.resource), que já resolve toda a cadeia UnidadeDeSaude/Profissional/Paciente por
    trás. O create endpoint só retorna um SuccessResponseDto, então esta flow lista toda EvolucaoEnfermagem
    (GET /api/v1/evolucao-enfermagem/) e casa pelo (fresh, portanto único) atendimentoUuid.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Evolucao Enfermagem Payload
    [Documentation]    Builds a valid EvolucaoEnfermagemRequestDto payload.
    [Arguments]    ${atendimentoId}    ${profissionalMatricula}    ${descricao}=Paciente estável, sem queixas
    ...    ${dataHora}=2026-01-01T10:00:00
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    atendimentoId=${atendimentoId}
    ...    profissionalMatricula=${profissionalMatricula}
    ...    dataHora=${dataHora}
    ...    descricao=${descricao}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Evolucao Enfermagem Uuid Por Atendimento
    [Documentation]    Lists every EvolucaoEnfermagem and returns the uuid of the one matching
    ...    ${atendimentoId}.
    [Arguments]    ${atendimentoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Evolucoes Enfermagem
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [e['uuid'] for e in $response.json() if e['atendimentoUuid'] == '${atendimentoId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Evolucao Enfermagem
    [Documentation]    Seeds a fresh Atendimento (e toda sua cadeia de dependências) and creates an
    ...    EvolucaoEnfermagem linked to it, returning the EvolucaoEnfermagem's uuid, the atendimentoId and
    ...    profissionalMatricula used.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    ${matricula}

    ${response}=    POST Create Evolucao Enfermagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${evolucaoId}=    Buscar Evolucao Enfermagem Uuid Por Atendimento    ${atendimentoId}

    RETURN    ${evolucaoId}    ${atendimentoId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Evolucao Enfermagem - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh EvolucaoEnfermagem, returning the create
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    ${matricula}

    ${response}=    POST Create Evolucao Enfermagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Evolucao Enfermagem - Status Code 404 - Atendimento Inexistente
    [Documentation]    Attempts to create an EvolucaoEnfermagem pointing at an atendimentoId that doesn't
    ...    exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${atendimentoIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoIdInexistente}    ${matricula}

    ${response}=    POST Create Evolucao Enfermagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Evolucao Enfermagem - Status Code 404 - Profissional Inexistente
    [Documentation]    Attempts to create an EvolucaoEnfermagem pointing at a profissionalMatricula that
    ...    doesn't exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    00000000000000-00

    ${response}=    POST Create Evolucao Enfermagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Evolucao Enfermagem - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create an EvolucaoEnfermagem with every field omitted, expecting the
    ...    field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Evolucao Enfermagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Evolucao Enfermagem - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create EvolucaoEnfermagem test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Evolucao Enfermagem - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Evolucao Enfermagem - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Evolucao Enfermagem test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
