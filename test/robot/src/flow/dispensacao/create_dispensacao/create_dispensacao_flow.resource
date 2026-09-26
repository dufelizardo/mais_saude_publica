*** Settings ***
Library    Collections
Resource    ../../../pom/dispensacao/create_dispensacao/create_dispensacao_pom.resource
Resource    ../../../pom/dispensacao/get_all_dispensacoes/get_all_dispensacoes_pom.resource
Resource    ../../lote/create_lote/create_lote_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Dispensacao depends on Lote and Paciente. Reaproveita "Seed Um Lote" (de create_lote_flow.resource,
    que cria o lote com quantidade padrão 100) e "Seed A Paciente" (de create_paciente_flow.resource). O
    create endpoint só retorna um SuccessResponseDto, então esta flow lista toda Dispensacao (GET
    /api/v1/dispensacao/) e casa pelo (fresh, portanto único) loteUuid. Dispensacao não tem endpoint de
    update (ver ADR-0051), então não há flow de update pra esta entidade.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Dispensacao Payload
    [Documentation]    Builds a valid DispensacaoRequestDto payload.
    [Arguments]    ${loteId}    ${pacienteId}    ${profissionalMatricula}    ${quantidade}=${10}
    ...    ${dataHora}=2026-01-01T14:00:00
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    loteId=${loteId}
    ...    pacienteId=${pacienteId}
    ...    profissionalMatricula=${profissionalMatricula}
    ...    quantidade=${quantidade}
    ...    dataHora=${dataHora}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Dispensacao Uuid Por Lote
    [Documentation]    Lists every Dispensacao and returns the uuid of the one matching ${loteId}.
    [Arguments]    ${loteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Dispensacoes
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [d['uuid'] for d in $response.json() if d['loteUuid'] == '${loteId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Dispensacao
    [Documentation]    Seeds a fresh Lote (quantidade 100) and Paciente and creates a Dispensacao linking
    ...    both, returning the Dispensacao's uuid and the loteId used.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Dispensacao Payload    ${loteId}    ${pacienteId}    ${matricula}

    ${response}=    POST Create Dispensacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${dispensacaoId}=    Buscar Dispensacao Uuid Por Lote    ${loteId}

    RETURN    ${dispensacaoId}    ${loteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Dispensacao - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh Dispensacao, returning the create
    ...    response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Dispensacao Payload    ${loteId}    ${pacienteId}    ${matricula}

    ${response}=    POST Create Dispensacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Dispensacao - Status Code 404 - Lote Inexistente
    [Documentation]    Attempts to create a Dispensacao pointing at a loteId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${loteIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Dispensacao Payload    ${loteIdInexistente}    ${pacienteId}    ${matricula}

    ${response}=    POST Create Dispensacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Dispensacao - Status Code 422 - Estoque Insuficiente
    [Documentation]    Attempts to create a Dispensacao requesting more than the Lote's available
    ...    quantidade, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Dispensacao Payload    ${loteId}    ${pacienteId}    ${matricula}    quantidade=${999999}

    ${response}=    POST Create Dispensacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Dispensacao - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Dispensacao with every field omitted, expecting the
    ...    field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Dispensacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Dispensacao - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Dispensacao test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Dispensacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Dispensacao - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Dispensacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
