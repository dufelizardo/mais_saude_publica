*** Settings ***
Library    Collections
Library    String
Resource    ../../../pom/medicamento/create_medicamento/create_medicamento_pom.resource
Resource    ../../../pom/medicamento/get_all_medicamentos/get_all_medicamentos_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Medicamento não tem dependências (é raiz do domínio Farmácia, ver ADR-0049) — diferente das demais
    flows desta suíte, não precisa seedar nenhuma outra entidade antes. O create endpoint só retorna um
    SuccessResponseDto, então esta flow lista todo Medicamento (GET /api/v1/medicamento/) e casa pelo
    (fresh, portanto único) nome gerado com um sufixo aleatório.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Medicamento Payload
    [Documentation]    Builds a valid MedicamentoRequestDto payload. Generates a random suffix for
    ...    ${nome} unless one is provided, so repeated seeds don't collide.
    [Arguments]    ${nome}=${EMPTY}    ${principioAtivo}=Dipirona Sódica    ${apresentacao}=Comprimido 500mg
    ...    ${codigo}=COD-001    ${ativo}=${TRUE}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${nome}' == '${EMPTY}'
        ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
        ${nome}=    Set Variable    Medicamento Robot ${sufixo}
    END

    ${payload}=    Create Dictionary
    ...    nome=${nome}
    ...    principioAtivo=${principioAtivo}
    ...    apresentacao=${apresentacao}
    ...    codigo=${codigo}
    ...    ativo=${ativo}

    RETURN    ${payload}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Medicamento Uuid Por Nome
    [Documentation]    Lists every Medicamento and returns the uuid of the one matching ${nome}.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get All Medicamentos
    Should Be Equal As Integers    ${response.status_code}    200
    ${uuid}=    Evaluate    [m['uuid'] for m in $response.json() if m['nome'] == '${nome}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Medicamento
    [Documentation]    Creates a fresh Medicamento, returning its uuid and nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}    ${nome}=    Build Medicamento Payload

    ${response}=    POST Create Medicamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${medicamentoId}=    Buscar Medicamento Uuid Por Nome    ${nome}

    RETURN    ${medicamentoId}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Medicamento - Status Code 201
    [Documentation]    Creates a fresh Medicamento, returning the create response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}    ${nome}=    Build Medicamento Payload

    ${response}=    POST Create Medicamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Medicamento - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Medicamento with every field omitted, expecting the
    ...    field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary

    ${response}=    POST Create Medicamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Medicamento - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Medicamento test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Medicamento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Medicamento - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Medicamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
