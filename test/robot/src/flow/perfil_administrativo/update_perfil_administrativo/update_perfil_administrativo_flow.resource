*** Settings ***
Resource    ../../../pom/perfil_administrativo/update_perfil_administrativo/update_perfil_administrativo_pom.resource
Resource    ../create_perfil_administrativo/create_perfil_administrativo_flow.resource
Resource    ../get_perfil_administrativo_by_id/get_perfil_administrativo_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    PATCH substitui os campos editáveis por inteiro (mesmo contrato de Setor/Capacidade
    Administrativa). Marcar ativo=False aqui dobra como o cenário de "desativar".
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Perfil Administrativo
    [Documentation]    Calls the PATCH Update Perfil Administrativo keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Perfil Administrativo    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Perfil Administrativo - Status Code 200
    [Documentation]    Seeds a fresh Perfil Administrativo and updates its nome, marking it inativo (ativo=False)
    ...    — covers both "editar campos" and "desativar" in a single scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${codigo}=    Seed A Perfil Administrativo
    ${payload}=    Build Perfil Administrativo Payload
    ...    ${codigo}    Robot Teste Perfil Atualizado    ${EMPTY}    ${False}

    ${response}=    Call PATCH Update Perfil Administrativo    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Perfil Administrativo By Id    ${uuid}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['nome']}    Robot Teste Perfil Atualizado
    Should Be Equal    ${getResponse.json()['ativo']}    ${False}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Perfil Administrativo - Status Code 404
    [Documentation]    Attempts to update a Perfil Administrativo identified by a random, non-existent uuid,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Perfil Administrativo Payload    ROBOT-PERFIL-INEXISTENTE    Robot Teste Perfil
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Perfil Administrativo    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Perfil Administrativo - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Perfil Administrativo test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Perfil Administrativo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Perfil Administrativo - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Perfil Administrativo test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
