*** Settings ***
Library    Collections
Library    String
Resource    ../../../pom/lote/create_lote/create_lote_pom.resource
Resource    ../../../pom/lote/get_all_lotes/get_all_lotes_pom.resource
Resource    ../../medicamento/create_medicamento/create_medicamento_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Lote depends on Medicamento and UnidadeDeSaude. Reaproveita "Seed Um Medicamento" (de
    create_medicamento_flow.resource) e "Seed A Unidade De Saude Uuid" (de create_setor_flow.resource, já
    resolve a cadeia Federal/Estadual/Municipal/UBS por trás). O create endpoint só retorna um
    SuccessResponseDto, então esta flow lista todo Lote (GET /api/v1/lote/) e casa pelo numeroLote (gerado
    com sufixo aleatório, portanto único).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Lote Payload
    [Documentation]    Builds a valid LoteRequestDto payload. Generates a random numeroLote unless one
    ...    is provided, so repeated seeds don't collide.
    [Arguments]    ${medicamentoId}    ${unidadeId}    ${numeroLote}=${EMPTY}    ${validade}=2027-01-01
    ...    ${quantidade}=${100}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${numeroLote}' == '${EMPTY}'
        ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
        ${numeroLote}=    Set Variable    LOTE-${sufixo}
    END

    ${payload}=    Create Dictionary
    ...    medicamentoId=${medicamentoId}
    ...    unidadeId=${unidadeId}
    ...    numeroLote=${numeroLote}
    ...    validade=${validade}
    ...    quantidade=${quantidade}

    RETURN    ${payload}    ${numeroLote}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Lote Uuid Por NumeroLote
    [Documentation]    Lists every Lote and returns the uuid of the one matching ${numeroLote}.
    [Arguments]    ${numeroLote}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Lotes
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [l['uuid'] for l in $response.json() if l['numeroLote'] == '${numeroLote}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Lote
    [Documentation]    Seeds a fresh Medicamento and Unidade de Saúde and creates a Lote linking both,
    ...    returning the Lote's uuid, medicamentoId and unidadeId used.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${medicamentoId}    ${nomeMedicamento}=    Seed Um Medicamento
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}    ${numeroLote}=    Build Lote Payload    ${medicamentoId}    ${unidadeId}

    ${response}=    POST Create Lote    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${loteId}=    Buscar Lote Uuid Por NumeroLote    ${numeroLote}

    RETURN    ${loteId}    ${medicamentoId}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Lote - Status Code 201
    [Documentation]    Seeds every dependency and creates a fresh Lote, returning the create response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${medicamentoId}    ${nomeMedicamento}=    Seed Um Medicamento
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}    ${numeroLote}=    Build Lote Payload    ${medicamentoId}    ${unidadeId}

    ${response}=    POST Create Lote    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Lote - Status Code 404 - Medicamento Inexistente
    [Documentation]    Attempts to create a Lote pointing at a medicamentoId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${medicamentoIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}    ${numeroLote}=    Build Lote Payload    ${medicamentoIdInexistente}    ${unidadeId}

    ${response}=    POST Create Lote    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Lote - Status Code 404 - Unidade Inexistente
    [Documentation]    Attempts to create a Lote pointing at a unidadeId that doesn't exist, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${medicamentoId}    ${nomeMedicamento}=    Seed Um Medicamento
    ${unidadeIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}    ${numeroLote}=    Build Lote Payload    ${medicamentoId}    ${unidadeIdInexistente}

    ${response}=    POST Create Lote    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Lote - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Lote with every field omitted, expecting the field-validation
    ...    HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Lote    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Lote - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Lote test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Lote - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Lote - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Lote test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
