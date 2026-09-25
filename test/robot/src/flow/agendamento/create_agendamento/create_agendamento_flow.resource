*** Settings ***
Library    Collections
Resource    ../../../pom/agendamento/create_agendamento/create_agendamento_pom.resource
Resource    ../../../pom/agendamento/get_all_agendamentos/get_all_agendamentos_pom.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Agendamento depends only on Paciente and Profissional (no UnidadeDeSaude/Setor, unlike Atendimento — ver
    ADR-0042). Reaproveita "Seed A Profissional Matricula" e "Seed A Paciente". O create endpoint só retorna
    um SuccessResponseDto, então esta flow lista todo Agendamento (GET /api/v1/agendamento/) e casa pelo
    (fresh, portanto único) pacienteUuid.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Agendamento Payload
    [Documentation]    Builds a valid AgendamentoRequestDto payload.
    [Arguments]    ${pacienteId}    ${profissionalMatricula}    ${status}=AGENDADO    ${tipo}=CONSULTA
    ...    ${dataHora}=2026-01-01T08:00:00    ${observacao}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    pacienteId=${pacienteId}
    ...    profissionalMatricula=${profissionalMatricula}
    ...    dataHora=${dataHora}
    ...    status=${status}
    ...    tipo=${tipo}
    ...    observacao=${observacao}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Agendamento Uuid Por Paciente
    [Documentation]    Lists every Agendamento and returns the uuid of the one matching ${pacienteId}.
    [Arguments]    ${pacienteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Agendamentos
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [a['uuid'] for a in $response.json() if a['pacienteUuid'] == '${pacienteId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Agendamento
    [Documentation]    Seeds a fresh Profissional and Paciente, creates an Agendamento linking both, and
    ...    returns the Agendamento's uuid, the pacienteId and profissionalMatricula used.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Agendamento Payload    ${pacienteId}    ${matricula}

    ${response}=    POST Create Agendamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${agendamentoId}=    Buscar Agendamento Uuid Por Paciente    ${pacienteId}

    RETURN    ${agendamentoId}    ${pacienteId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Agendamento - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh Agendamento, returning the create response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Agendamento Payload    ${pacienteId}    ${matricula}

    ${response}=    POST Create Agendamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Agendamento - Status Code 404 - Paciente Inexistente
    [Documentation]    Attempts to create an Agendamento pointing at a pacienteId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Agendamento Payload    ${pacienteIdInexistente}    ${matricula}

    ${response}=    POST Create Agendamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Agendamento - Status Code 404 - Profissional Inexistente
    [Documentation]    Attempts to create an Agendamento pointing at a profissionalMatricula that doesn't
    ...    exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Agendamento Payload    ${pacienteId}    00000000000000-00

    ${response}=    POST Create Agendamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Agendamento - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create an Agendamento with every field omitted, expecting the
    ...    field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Agendamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Agendamento - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Agendamento test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Agendamento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Agendamento - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Agendamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
