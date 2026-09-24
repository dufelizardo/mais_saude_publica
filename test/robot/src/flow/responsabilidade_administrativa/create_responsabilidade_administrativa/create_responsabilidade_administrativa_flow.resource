*** Settings ***
Library    String
Resource    ../../../pom/responsabilidade_administrativa/create_responsabilidade_administrativa/create_responsabilidade_administrativa_pom.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    ResponsabilidadeAdministrativa referencia Profissional por matrícula (ADR-0034) e Setor por uuid
    (ADR-0030) — reaproveita "Seed A Setor" e "Seed A Profissional Matricula" já definidos no domínio
    Setor. Um profissional pode ter várias responsabilidades vigentes ao mesmo tempo (ADR-0035), sem
    constraint de unicidade — igual Vaga/Cargo, não há cenário de 409 no create.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Responsabilidade Administrativa Payload
    [Documentation]    Builds a valid ResponsabilidadeAdministrativaRequestDto payload.
    [Arguments]    ${matriculaProfissional}    ${setorId}    ${tipo}=Fiscal de Contrato    ${dataInicio}=2026-01-01
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    matriculaProfissional=${matriculaProfissional}
    ...    setorId=${setorId}
    ...    tipo=${tipo}
    ...    dataInicio=${dataInicio}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Responsabilidade Administrativa - Status Code 201
    [Documentation]    Seeds a fresh Setor and Profissional, creates a Responsabilidade Administrativa linking
    ...    them, and returns the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Responsabilidade Administrativa Payload    ${matricula}    ${setorId}

    ${response}=    POST Create Responsabilidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Responsabilidade Administrativa
    [Documentation]    Seeds a fresh Setor, Profissional and Responsabilidade Administrativa linking them.
    ...    Returns the profissional's matrícula and the setor's uuid — útil para GET histórico/encerrar.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Responsabilidade Administrativa Payload    ${matricula}    ${setorId}

    ${response}=    POST Create Responsabilidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${matricula}    ${setorId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Responsabilidade Administrativa - Status Code 404 - Profissional Inexistente
    [Documentation]    Attempts to create a Responsabilidade Administrativa pointing at a matriculaProfissional
    ...    that doesn't exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Responsabilidade Administrativa Payload    MATRICULA-INEXISTENTE-${suffix}    ${setorId}

    ${response}=    POST Create Responsabilidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Responsabilidade Administrativa - Status Code 404 - Setor Inexistente
    [Documentation]    Attempts to create a Responsabilidade Administrativa pointing at a setorId that doesn't
    ...    exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${setorIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Responsabilidade Administrativa Payload    ${matricula}    ${setorIdInexistente}

    ${response}=    POST Create Responsabilidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Responsabilidade Administrativa - Status Code 400 - Campos Obrigatorios Em Branco
    [Documentation]    Attempts to create a Responsabilidade Administrativa with matriculaProfissional/tipo left
    ...    blank and setorId/dataInicio omitted, expecting the field-validation HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    matriculaProfissional=${EMPTY}
    ...    tipo=${EMPTY}

    ${response}=    POST Create Responsabilidade Administrativa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Responsabilidade Administrativa - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Responsabilidade Administrativa test scenario based on
    ...    the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Responsabilidade Administrativa - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Responsabilidade Administrativa - Status Code 404 - Profissional Inexistente
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Responsabilidade Administrativa - Status Code 400 - Campos Obrigatorios Em Branco
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Responsabilidade Administrativa test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
