*** Settings ***
Library    Collections
Resource    ../../../pom/consulta/create_consulta/create_consulta_pom.resource
Resource    ../../../pom/consulta/get_all_consultas/get_all_consultas_pom.resource
Resource    ../../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Consulta depends on Atendimento and Profissional. Reaproveita "Seed Um Atendimento" (de
    create_atendimento_flow.resource), que já resolve toda a cadeia UnidadeDeSaude/Profissional/Paciente por
    trás. O create endpoint só retorna um SuccessResponseDto, então esta flow lista toda Consulta (GET
    /api/v1/consulta/) e casa pelo (fresh, portanto único) atendimentoUuid.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Consulta Payload
    [Documentation]    Builds a valid ConsultaRequestDto payload.
    [Arguments]    ${atendimentoId}    ${profissionalMatricula}    ${tipoConsulta}=PRIMEIRA
    ...    ${dataHora}=2026-01-01T08:30:00
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    atendimentoId=${atendimentoId}
    ...    profissionalMatricula=${profissionalMatricula}
    ...    dataHora=${dataHora}
    ...    tipoConsulta=${tipoConsulta}
    ...    queixaPrincipal=Dor de cabeça
    ...    diagnostico=Cefaleia tensional
    ...    receituario=Dipirona 500mg
    ...    examesSolicitados=Nenhum
    ...    retorno=2026-02-01

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Consulta Uuid Por Atendimento
    [Documentation]    Lists every Consulta and returns the uuid of the one matching ${atendimentoId}.
    [Arguments]    ${atendimentoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Consultas
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [c['uuid'] for c in $response.json() if c['atendimentoUuid'] == '${atendimentoId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Consulta
    [Documentation]    Seeds a fresh Atendimento (e toda sua cadeia de dependências) and creates a Consulta
    ...    linked to it, returning the Consulta's uuid, the atendimentoId and profissionalMatricula used.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Consulta Payload    ${atendimentoId}    ${matricula}

    ${response}=    POST Create Consulta    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${consultaId}=    Buscar Consulta Uuid Por Atendimento    ${atendimentoId}

    RETURN    ${consultaId}    ${atendimentoId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Consulta - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh Consulta, returning the create response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Consulta Payload    ${atendimentoId}    ${matricula}

    ${response}=    POST Create Consulta    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Consulta - Status Code 404 - Atendimento Inexistente
    [Documentation]    Attempts to create a Consulta pointing at an atendimentoId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${atendimentoIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Consulta Payload    ${atendimentoIdInexistente}    ${matricula}

    ${response}=    POST Create Consulta    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Consulta - Status Code 404 - Profissional Inexistente
    [Documentation]    Attempts to create a Consulta pointing at a profissionalMatricula that doesn't exist,
    ...    expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Consulta Payload    ${atendimentoId}    00000000000000-00

    ${response}=    POST Create Consulta    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Consulta - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Consulta with every field omitted, expecting the field-validation
    ...    HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Consulta    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Consulta - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Consulta test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Consulta - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Consulta - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Consulta test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
