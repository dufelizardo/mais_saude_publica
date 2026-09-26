*** Settings ***
Library    Collections
Resource    ../../../pom/triagem/create_triagem/create_triagem_pom.resource
Resource    ../../../pom/triagem/get_all_triagens/get_all_triagens_pom.resource
Resource    ../../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Triagem depends on Atendimento and Profissional. Reaproveita "Seed Um Atendimento" (de
    create_atendimento_flow.resource), que já resolve toda a cadeia UnidadeDeSaude/Profissional/Paciente por
    trás. O create endpoint só retorna um SuccessResponseDto, então esta flow lista toda Triagem (GET
    /api/v1/triagem/) e casa pelo (fresh, portanto único) atendimentoUuid.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Triagem Payload
    [Documentation]    Builds a valid TriagemRequestDto payload.
    [Arguments]    ${atendimentoId}    ${profissionalMatricula}    ${classificacaoRisco}=VERDE
    ...    ${dataHora}=2026-01-01T08:10:00
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    atendimentoId=${atendimentoId}
    ...    profissionalMatricula=${profissionalMatricula}
    ...    dataHora=${dataHora}
    ...    pressaoArterial=120/80
    ...    temperatura=${36.5}
    ...    saturacaoOxigenio=${98.0}
    ...    frequenciaCardiaca=${80}
    ...    peso=${70.5}
    ...    classificacaoRisco=${classificacaoRisco}
    ...    observacoes=Paciente consciente e orientado

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Triagem Uuid Por Atendimento
    [Documentation]    Lists every Triagem and returns the uuid of the one matching ${atendimentoId}.
    [Arguments]    ${atendimentoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Triagens
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [t['uuid'] for t in $response.json() if t['atendimentoUuid'] == '${atendimentoId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Triagem
    [Documentation]    Seeds a fresh Atendimento (e toda sua cadeia de dependências) and creates a Triagem
    ...    linked to it, returning the Triagem's uuid, the atendimentoId and profissionalMatricula used.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Triagem Payload    ${atendimentoId}    ${matricula}

    ${response}=    POST Create Triagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${triagemId}=    Buscar Triagem Uuid Por Atendimento    ${atendimentoId}

    RETURN    ${triagemId}    ${atendimentoId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Triagem - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh Triagem, returning the create response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Triagem Payload    ${atendimentoId}    ${matricula}

    ${response}=    POST Create Triagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Triagem - Status Code 404 - Atendimento Inexistente
    [Documentation]    Attempts to create a Triagem pointing at an atendimentoId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${atendimentoIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Triagem Payload    ${atendimentoIdInexistente}    ${matricula}

    ${response}=    POST Create Triagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Triagem - Status Code 404 - Profissional Inexistente
    [Documentation]    Attempts to create a Triagem pointing at a profissionalMatricula that doesn't exist,
    ...    expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Triagem Payload    ${atendimentoId}    00000000000000-00

    ${response}=    POST Create Triagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Triagem - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Triagem with every field omitted, expecting the field-validation
    ...    HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Triagem    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Triagem - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Triagem test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Triagem - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Triagem - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Triagem test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
