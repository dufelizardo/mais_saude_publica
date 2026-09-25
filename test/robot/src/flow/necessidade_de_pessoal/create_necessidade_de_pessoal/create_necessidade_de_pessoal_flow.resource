*** Settings ***
Resource    ../../../pom/necessidade_de_pessoal/create_necessidade_de_pessoal/create_necessidade_de_pessoal_pom.resource
Resource    ../../../pom/necessidade_de_pessoal/get_all_necessidades_de_pessoal/get_all_necessidades_de_pessoal_pom.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../cargo/create_cargo/create_cargo_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    NecessidadeDePessoal referencia UnidadeDeSaude/Setor/Cargo por uuid, sem constraint de unicidade
    (ver ADR-0036) — mesmo contrato de ProcessoAdministrativo, sem cenário de 409. O create endpoint só
    retorna SuccessResponseDto, então o uuid é resolvido listando tudo e filtrando pela unidade
    vinculada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Necessidade De Pessoal Payload
    [Documentation]    Builds a valid NecessidadeDePessoalRequestDto payload. ${setorId} is optional (a Setor
    ...    nullable per ADR-0036) — when left blank, the key is omitted entirely.
    [Arguments]    ${unidadeId}    ${cargoId}    ${quantidade}=2    ${jornadaSemanalHoras}=40
    ...    ${setorId}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    unidadeId=${unidadeId}
    ...    cargoId=${cargoId}
    ...    quantidade=${quantidade}
    ...    jornadaSemanalHoras=${jornadaSemanalHoras}

    IF    '${setorId}' != '${EMPTY}'
        Set To Dictionary    ${payload}    setorId=${setorId}
    END

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Necessidade De Pessoal Uuid Por Unidade
    [Documentation]    Lists every Necessidade De Pessoal and returns the uuid of the first one linked to
    ...    ${unidadeId}.
    [Arguments]    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Necessidades De Pessoal
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [n['uuid'] for n in $response.json() if n['unidadeUuid'] == '${unidadeId}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Necessidade De Pessoal - Status Code 201
    [Documentation]    Seeds a fresh Unidade de Saúde and Cargo, creates a Necessidade De Pessoal pointing at
    ...    both (sem Setor), and returns the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${cargoId}=    Seed A Cargo Uuid
    ${payload}=    Build Necessidade De Pessoal Payload    ${unidadeId}    ${cargoId}

    ${response}=    POST Create Necessidade De Pessoal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Necessidade De Pessoal
    [Documentation]    Seeds a fresh Unidade de Saúde, Cargo and Necessidade De Pessoal pointing at both (sem
    ...    Setor), returning the necessidade's uuid, a unidade's uuid and o cargo's uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${cargoId}=    Seed A Cargo Uuid
    ${payload}=    Build Necessidade De Pessoal Payload    ${unidadeId}    ${cargoId}

    ${response}=    POST Create Necessidade De Pessoal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${necessidadeId}=    Buscar Necessidade De Pessoal Uuid Por Unidade    ${unidadeId}

    RETURN    ${necessidadeId}    ${unidadeId}    ${cargoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Necessidade De Pessoal - Status Code 404 - Unidade Inexistente
    [Documentation]    Attempts to create a Necessidade De Pessoal pointing at a unidadeId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${cargoId}=    Seed A Cargo Uuid
    ${payload}=    Build Necessidade De Pessoal Payload    ${unidadeIdInexistente}    ${cargoId}

    ${response}=    POST Create Necessidade De Pessoal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Necessidade De Pessoal - Status Code 404 - Cargo Inexistente
    [Documentation]    Attempts to create a Necessidade De Pessoal pointing at a cargoId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${cargoIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Necessidade De Pessoal Payload    ${unidadeId}    ${cargoIdInexistente}

    ${response}=    POST Create Necessidade De Pessoal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Necessidade De Pessoal - Status Code 404 - Setor Inexistente
    [Documentation]    Attempts to create a Necessidade De Pessoal pointing at a setorId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${cargoId}=    Seed A Cargo Uuid
    ${setorIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Necessidade De Pessoal Payload    ${unidadeId}    ${cargoId}    setorId=${setorIdInexistente}

    ${response}=    POST Create Necessidade De Pessoal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Necessidade De Pessoal - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Necessidade De Pessoal with unidadeId/cargoId/quantidade/
    ...    jornadaSemanalHoras omitted, expecting the field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    competenciasNecessarias=${EMPTY}

    ${response}=    POST Create Necessidade De Pessoal    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Necessidade De Pessoal - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Necessidade De Pessoal test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Necessidade De Pessoal - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Necessidade De Pessoal - Status Code 404 - Unidade Inexistente
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Necessidade De Pessoal - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Necessidade De Pessoal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
