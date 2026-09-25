*** Settings ***
Library    Collections
Resource    ../../../pom/procedimento/create_procedimento/create_procedimento_pom.resource
Resource    ../../../pom/procedimento/get_all_procedimentos/get_all_procedimentos_pom.resource
Resource    ../../consulta/create_consulta/create_consulta_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Procedimento depends on Consulta and Profissional. Reaproveita "Seed Uma Consulta" (de
    create_consulta_flow.resource), que já resolve toda a cadeia UnidadeDeSaude/Profissional/Paciente/
    Atendimento por trás. O create endpoint só retorna um SuccessResponseDto, então esta flow lista todo
    Procedimento (GET /api/v1/procedimento/) e casa pelo (fresh, portanto único) consultaUuid.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Procedimento Payload
    [Documentation]    Builds a valid ProcedimentoRequestDto payload. ${tipo} é texto livre (ver ADR-0044).
    [Arguments]    ${consultaId}    ${profissionalMatricula}    ${status}=AGENDADO    ${tipo}=EXAME
    ...    ${dataRealizacao}=2026-01-01T09:00:00
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    consultaId=${consultaId}
    ...    profissionalMatricula=${profissionalMatricula}
    ...    tipo=${tipo}
    ...    descricao=Hemograma completo
    ...    dataRealizacao=${dataRealizacao}
    ...    status=${status}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Procedimento Uuid Por Consulta
    [Documentation]    Lists every Procedimento and returns the uuid of the one matching ${consultaId}.
    [Arguments]    ${consultaId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Procedimentos
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [p['uuid'] for p in $response.json() if p['consultaUuid'] == '${consultaId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Procedimento
    [Documentation]    Seeds a fresh Consulta (e toda sua cadeia de dependências) and creates a Procedimento
    ...    linked to it, returning the Procedimento's uuid, the consultaId and profissionalMatricula used.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${consultaId}    ${atendimentoId}    ${matricula}=    Seed Uma Consulta
    ${payload}=    Build Procedimento Payload    ${consultaId}    ${matricula}

    ${response}=    POST Create Procedimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${procedimentoId}=    Buscar Procedimento Uuid Por Consulta    ${consultaId}

    RETURN    ${procedimentoId}    ${consultaId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Procedimento - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh Procedimento, returning the create
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${consultaId}    ${atendimentoId}    ${matricula}=    Seed Uma Consulta
    ${payload}=    Build Procedimento Payload    ${consultaId}    ${matricula}

    ${response}=    POST Create Procedimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Procedimento - Status Code 404 - Consulta Inexistente
    [Documentation]    Attempts to create a Procedimento pointing at a consultaId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${consultaIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Procedimento Payload    ${consultaIdInexistente}    ${matricula}

    ${response}=    POST Create Procedimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Procedimento - Status Code 404 - Profissional Inexistente
    [Documentation]    Attempts to create a Procedimento pointing at a profissionalMatricula that doesn't
    ...    exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${consultaId}    ${atendimentoId}    ${matricula}=    Seed Uma Consulta
    ${payload}=    Build Procedimento Payload    ${consultaId}    00000000000000-00

    ${response}=    POST Create Procedimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Procedimento - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Procedimento with every field omitted, expecting the
    ...    field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Procedimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Procedimento test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Procedimento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Procedimento - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Procedimento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
