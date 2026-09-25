*** Settings ***
Resource    ../../../pom/processo_administrativo/update_processo_administrativo/update_processo_administrativo_pom.resource
Resource    ../create_processo_administrativo/create_processo_administrativo_flow.resource
Resource    ../get_processo_administrativo_by_id/get_processo_administrativo_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    PATCH substitui os campos editáveis por inteiro (mesmo contrato de Setor/Capacidade/Perfil).
    Marcar ativo=False aqui dobra como o cenário de "desativar" — não existe endpoint dedicado.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Processo Administrativo
    [Documentation]    Calls the PATCH Update Processo Administrativo keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Processo Administrativo    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Processo Administrativo - Status Code 200
    [Documentation]    Seeds a fresh Processo Administrativo and updates its nome, marking it inativo
    ...    (ativo=False) — covers both "editar campos" and "desativar" in a single scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${processoId}    ${capacidadeId}=    Seed A Processo Administrativo
    ${payload}=    Build Processo Administrativo Payload
    ...    ${capacidadeId}    ROBOT-PROC-ATUALIZADO    Robot Teste Processo Atualizado    ${EMPTY}    ${False}

    ${response}=    Call PATCH Update Processo Administrativo    ${processoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Processo Administrativo By Id    ${processoId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['nome']}    Robot Teste Processo Atualizado
    Should Be Equal    ${getResponse.json()['ativo']}    ${False}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Processo Administrativo - Status Code 404
    [Documentation]    Attempts to update a Processo Administrativo identified by a random, non-existent uuid,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${capacidadeId}    ${codigoCapacidade}=    Seed A Capacidade Administrativa
    ${payload}=    Build Processo Administrativo Payload
    ...    ${capacidadeId}    ROBOT-PROC-INEXISTENTE    Robot Teste Processo
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Processo Administrativo    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Processo Administrativo - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Processo Administrativo test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Processo Administrativo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Processo Administrativo - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Processo Administrativo test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
