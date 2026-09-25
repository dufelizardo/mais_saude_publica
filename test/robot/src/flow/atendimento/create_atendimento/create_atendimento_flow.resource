*** Settings ***
Library    Collections
Resource    ../../../pom/atendimento/create_atendimento/create_atendimento_pom.resource
Resource    ../../../pom/atendimento/get_all_atendimentos/get_all_atendimentos_pom.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../agendamento/create_agendamento/create_agendamento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Atendimento depends on three other domains (Paciente, Profissional, UnidadeDeSaude) — this flow reuses
    the seeding keywords already established by those domains: "Seed A Unidade De Saude Uuid" (from
    create_setor_flow.resource), "Seed A Profissional Matricula" (from
    get_profissional_by_matricula_flow.resource) and "Seed A Paciente" (from create_paciente_flow.resource).
    The create endpoint only returns a SuccessResponseDto (same contract as Setor/Paciente), so to get a
    freshly created Atendimento's uuid back this flow lists every Atendimento (GET /api/v1/atendimento/) and
    matches by the (freshly seeded, therefore unique) pacienteUuid.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Atendimento Payload
    [Documentation]    Builds a valid AtendimentoRequestDto payload. ${setorId}/${agendamentoId} are
    ...    optional — when left blank, the key is sent as JSON null (ambos são FKs opcionais, ver
    ...    ADR-0041/ADR-0042).
    [Arguments]    ${pacienteId}    ${profissionalMatricula}    ${unidadeId}    ${setorId}=${NONE}
    ...    ${agendamentoId}=${NONE}    ${tipo}=CONSULTA    ${status}=AGENDADO
    ...    ${dataHora}=2026-01-01T08:00:00
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    pacienteId=${pacienteId}
    ...    profissionalMatricula=${profissionalMatricula}
    ...    unidadeId=${unidadeId}
    ...    setorId=${setorId}
    ...    agendamentoId=${agendamentoId}
    ...    tipo=${tipo}
    ...    status=${status}
    ...    dataHora=${dataHora}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Atendimento Uuid Por Paciente
    [Documentation]    Lists every Atendimento and returns the uuid of the one matching ${pacienteId}.
    [Arguments]    ${pacienteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Atendimentos
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [a['uuid'] for a in $response.json() if a['pacienteUuid'] == '${pacienteId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Atendimento
    [Documentation]    Seeds a fresh Unidade de Saúde, Profissional and Paciente, creates an Atendimento
    ...    linking all three, and returns the Atendimento's uuid, the pacienteId, profissionalMatricula and
    ...    unidadeId used — useful for GET-by-id/PATCH tests that need to operate on a real Atendimento.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}

    ${response}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${atendimentoId}=    Buscar Atendimento Uuid Por Paciente    ${pacienteId}

    RETURN    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atendimento - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh Atendimento, returning the create response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}

    ${response}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atendimento - Status Code 201 - Com Agendamento
    [Documentation]    Seeds a fresh Agendamento (usando o mesmo Paciente/Profissional) and creates an
    ...    Atendimento vinculado a ele, returning the Atendimento's uuid and the Agendamento's uuid — útil
    ...    para o cenário confirmar via GET que agendamentoUuid reflete o vínculo (ver ADR-0042).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${agendamentoPayload}=    Build Agendamento Payload    ${pacienteId}    ${matricula}
    ${agendamentoResponse}=    POST Create Agendamento    ${agendamentoPayload}
    Should Be Equal As Integers    ${agendamentoResponse.status_code}    201
    ${agendamentoId}=    Buscar Agendamento Uuid Por Paciente    ${pacienteId}

    ${payload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}
    ...    agendamentoId=${agendamentoId}

    ${response}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${atendimentoId}=    Buscar Atendimento Uuid Por Paciente    ${pacienteId}

    RETURN    ${atendimentoId}    ${agendamentoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atendimento - Status Code 404 - Paciente Inexistente
    [Documentation]    Attempts to create an Atendimento pointing at a pacienteId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Atendimento Payload    ${pacienteIdInexistente}    ${matricula}    ${unidadeId}

    ${response}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atendimento - Status Code 404 - Profissional Inexistente
    [Documentation]    Attempts to create an Atendimento pointing at a profissionalMatricula that doesn't
    ...    exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Atendimento Payload    ${pacienteId}    00000000000000-00    ${unidadeId}

    ${response}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atendimento - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create an Atendimento with every field omitted, expecting the
    ...    field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Atendimento - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Atendimento test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Atendimento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Atendimento - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Atendimento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
