*** Settings ***
Resource    ../../../pom/setor/update_setor/update_setor_pom.resource
Resource    ../create_setor/create_setor_flow.resource
Resource    ../get_setor_by_id/get_setor_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Setor's PATCH replaces every editable field at once (nome/codigo/tipo/ativo) — same contract as
    Vaga/Cargo — rather than a single-field PATCH like the original hierarchy's update_nome/update_contato.
    Setting ativo=False here doubles as the domain's "desativar" scenario (there's no dedicated
    des-habilitar endpoint for Setor, see ADR-0030).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Setor
    [Documentation]    Calls the PATCH Update Setor keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Setor    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Setor - Status Code 200
    [Documentation]    Seeds a fresh Setor and updates its nome, marking it inativo (ativo=False) — covers both
    ...    "editar campos" and "desativar" in a single scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor
    ${payload}=    Build Setor Payload
    ...    ${unidadeId}    Robot Teste Setor Atualizado    ${codigo}    ADMINISTRATIVO    ${False}

    ${response}=    Call PATCH Update Setor    ${setorId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Setor By Id    ${setorId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['nome']}    Robot Teste Setor Atualizado
    Should Be Equal    ${getResponse.json()['ativo']}    ${False}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Setor - Status Code 404
    [Documentation]    Attempts to update a Setor identified by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}=    Build Setor Payload    ${unidadeId}    Robot Teste Setor    ROBOT-INEXISTENTE
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Setor    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Setor - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Setor test scenario based on the provided HTTP status
    ...    code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Setor - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Setor - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Setor test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
