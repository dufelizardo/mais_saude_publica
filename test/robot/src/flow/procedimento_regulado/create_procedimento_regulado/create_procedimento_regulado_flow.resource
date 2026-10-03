*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/procedimento_regulado/create_procedimento_regulado/create_procedimento_regulado_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Catálogo da Central de Regulação: nome único, sem exclusão (sai de uso com ativo=false).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Procedimento Regulado
    [Documentation]    Creates a fresh ProcedimentoRegulado and returns its uuid and nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Procedimento Regulado ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    tipo=CONSULTA_ESPECIALIZADA
    ${criado}=    POST Create Procedimento Regulado    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Procedimento Regulado
    ${uuid}=    Evaluate    [p['uuid'] for p in $lista.json() if p['nome'] == $nome][0]
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Procedimento Regulado - Status Code 201
    [Documentation]    Creates an item and validates the success schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Procedimento Regulado
    ${lista}=    GET List Procedimento Regulado    true
    Should Be Equal As Integers    ${lista.status_code}    200
    Validate Array Schema    ${lista.json()}    ${PROCEDIMENTO_REGULADO_SCHEMA}
    ${achou}=    Evaluate    $uuid in [p['uuid'] for p in $lista.json()]
    Should Be True    ${achou}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Procedimento Regulado - Status Code 409 - Nome Repetido
    [Documentation]    Creates the same name twice (ignoring case), expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Procedimento Regulado
    ${maiusculo}=    Convert To Upper Case    ${nome}
    ${payload}=    Create Dictionary    nome=${maiusculo}    tipo=EXAME
    ${response}=    POST Create Procedimento Regulado    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Procedimento Regulado - Status Code 400 - Sem Tipo
    [Documentation]    Sends a payload without tipo, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    nome=Robot Procedimento Sem Tipo
    ${response}=    POST Create Procedimento Regulado    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Regulado - Create - POST - By HTTP Status Code
    [Documentation]    Executes the Procedimento Regulado - Create - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Procedimento Regulado - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Procedimento Regulado - Status Code 409 - Nome Repetido
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Procedimento Regulado - Status Code 400 - Sem Tipo
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Procedimento Regulado - Create - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
